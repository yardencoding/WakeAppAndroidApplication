package com.example.wakeup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CreateAlarm extends AppCompatActivity implements View.OnClickListener {


    private Button timeButton;
    private int hour = 6, minute = 0;
    private ToggleButton sunday, monday, tuesday, wednesday, thursday,
            friday, saturday;
    private EditText alarmName;
    private TextView alarmMissionName, alarmSoundName;
    ;
    private ImageButton saveImageButton;
    private Switch soundSwitch, vibrateSwitch, missionSwitch, useMyContactsSwitch;
    private Button chooseSoundButton, missionButton, useContactsButton;

    private boolean hasSelectedTime = false;


    private int maxVolume;

    private ArrayList<Alarm> alarmList_FromIntent;

    //To get the sound name and volume from ChooseSound Activity.
    private SharedPreferences chooseSoundPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_alarm);


        timeButton = findViewById(R.id.choose_time_btn);
        timeButton.setOnClickListener(this);

        saveImageButton = findViewById(R.id.save_alarm_ImageButton);
        saveImageButton.setOnClickListener(this);

        chooseSoundButton = findViewById(R.id.alarm_sound_btn);
        chooseSoundButton.setOnClickListener(this);

        missionButton = findViewById(R.id.alarm_mission_btn);
        missionButton.setOnClickListener(this);

        useContactsButton = findViewById(R.id.alarm_contacts_btn);
        useContactsButton.setOnClickListener(this);

        sunday = findViewById(R.id.sunday_tb);
        monday = findViewById(R.id.monday_tb);
        tuesday = findViewById(R.id.tuesday_tb);
        wednesday = findViewById(R.id.wednesday_tb);
        thursday = findViewById(R.id.thursday_tb);
        friday = findViewById(R.id.friday_tb);
        saturday = findViewById(R.id.saturday_tb);
        alarmName = findViewById(R.id.alarm_name_editText);
        alarmMissionName = findViewById(R.id.mission_name_tv);
        alarmSoundName = findViewById(R.id.song_name_tv);
        soundSwitch = findViewById(R.id.alarm_sound_switch);
        vibrateSwitch = findViewById(R.id.alarm_vibrate_switch);
        missionSwitch = findViewById(R.id.alarm_mission_switch);
        useMyContactsSwitch = findViewById(R.id.alarm_contacts_switch);

        chooseSoundPreferences = getSharedPreferences(ChooseSound.SHARED_PREFS, MODE_PRIVATE);
        //Sound name.
        alarmSoundName.setText(chooseSoundPreferences.getString(ChooseSound.SOUND_NAME, "Homecoming"));

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

        //If we opened this activity through an alarm click. change the fields to match alarm fields.
        if (getClickedAlarm() != null)
            alarmWasClicked();


        //To request camera permission when the hasMission switch is checked
        missionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ContextCompat.checkSelfPermission(CreateAlarm.this,
                            Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(CreateAlarm.this, new String[]{Manifest.permission.CAMERA}, MainScreen.CAMERA_REQUEST_CODE);
                    }
                }
            }
        });

    }


    // crates a timePicker dialog when "בחר שעה" btn is clicked
    private void popTimePicker() {
        // when the user has finished choosing the time
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                hasSelectedTime = true;
                hour = selectedHour;
                minute = selectedMinute;
                timeButton.setTextSize(35);
                timeButton.setText(String.format("%02d:%02d", hour, minute));

            }
        };

        final int SPINNER_MODE = 2;
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                SPINNER_MODE,
                onTimeSetListener,
                hour, minute,
                true);

        timePickerDialog.show();

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.choose_time_btn:
                popTimePicker();
                break;
            case R.id.alarm_sound_btn:
                startActivity(new Intent(CreateAlarm.this, ChooseSound.class));
                break;

            case R.id.alarm_contacts_btn:
                startActivity(new Intent(CreateAlarm.this, Contact.class));
                break;

            case R.id.save_alarm_ImageButton:
                createAlarm();
        }

    }

    private void createAlarm() {

        if (postNotificationWasGranted()
                && timeWasChosen()
                && makeSureThatContactsWereAdded()
                && makeSureThatCameraPermissionWasGranted()
                && hasDisplayOverOtherAppsPermission()) {

            // Start the statusBarService that will show a consistent icon as long as there are active alarms.
            Intent statusBarService = new Intent(this, StatusBarNotificationService.class);
            startForegroundService(statusBarService);


            //Create Alarm fields
            String name = alarmName.getText().toString();
            String mission = alarmMissionName.getText().toString();
            String soundName = alarmSoundName.getText().toString();
            Alarm newAlarm = new Alarm(
                    true,
                    hour,
                    minute,
                    name,
                    mission,
                    soundName,
                    sunday.isChecked(),
                    monday.isChecked(),
                    tuesday.isChecked(),
                    wednesday.isChecked(),
                    thursday.isChecked(),
                    friday.isChecked(),
                    saturday.isChecked(),
                    soundSwitch.isChecked(),
                    vibrateSwitch.isChecked(),
                    missionSwitch.isChecked(),
                    useMyContactsSwitch.isChecked()
            );

            if (newAlarm.hasNoChosenDay())
                newAlarm.whenNoDay_WasChosen();

            addAlarmToDataBase_ifNotAlreadyExist(newAlarm);


            //set the id of the new alarm the AlarmList.
            int newAlarmId;
            if (getClickedAlarm() != null) { //If we want to update this alarm, delete the previous one.
                getClickedAlarm().cancel(this);
                newAlarmId = alarmList_FromIntent.size();
            } else {
                newAlarmId = alarmList_FromIntent.size() + 1;
            }

            // schedule the alarm
            newAlarm.setVolume(chooseSoundPreferences.getInt(ChooseSound.SOUND_VOLUME, 60) / (100 / maxVolume));
            newAlarm.setId(newAlarmId);
            newAlarm.schedule(this);


            // go to the first activity
            Intent goToMainScreen = new Intent(this, MainScreen.class);
            startActivity(goToMainScreen);

        }
    }

    private boolean hasDisplayOverOtherAppsPermission() {
        if (!Settings.canDrawOverlays(this)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("אפשר הרשאה של הצג מעל אפליקציות אחרות");
            builder.setPositiveButton("לך להגדרות", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                activityResultLauncher.launch(intent);
            });
            builder.setNegativeButton("סגור", (dialog, which) -> {
                Toast.makeText(CreateAlarm.this, "בלי הרשאה זו, לא תוכל ליצור התראה.", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            });

            builder.show();

            return false;
        }
        return true;
    }

    //If the "useMyContacts" switch is checked make  sure that contacts were added.
    private boolean makeSureThatContactsWereAdded() {
        if (useMyContactsSwitch.isChecked()) {
            SharedPreferences preferences = getSharedPreferences(Contact.SHARED_PREFS, MODE_PRIVATE);
            String contact1 = preferences.getString(Contact.PHONE_NUMBER_1, "");
            String contact2 = preferences.getString(Contact.PHONE_NUMBER_2, "");
            String contact3 = preferences.getString(Contact.PHONE_NUMBER_3, "");
            if (contact1.isEmpty() && contact2.isEmpty() && contact3.isEmpty()) {
                Toast.makeText(this, "לא הוספת אנשי קשר", Toast.LENGTH_SHORT).show();
                return false;
            } else
                return true;
        }
        return true;
    }

    private boolean makeSureThatCameraPermissionWasGranted() {
        if (missionSwitch.isChecked())
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "יש צורך בגישה למצלמה על מנת להפעיל צילום חיוך", Toast.LENGTH_LONG).show();
                return false;
            }
        return true;
    }

    private boolean timeWasChosen() {
        if (getClickedAlarm() == null && hasSelectedTime == false) {
            // Check if time was selected, When we opened this activity through add button.
            Toast.makeText(this, "לא הוגדרה שעה", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean postNotificationWasGranted() {
        //Check if POST_NOTIFICATION permission was granted, if not Return.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "על מנת ליצור התראה יש צורך בהרשאה להצגת התראות", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private Alarm getClickedAlarm() {
        Intent intent = getIntent();
        Alarm clickedAlarm = intent.getParcelableExtra("getClickedAlarm");
        return clickedAlarm;
    }

    //Set CreateAlarm fields to the corresponding clickedAlarm fields.
    private void alarmWasClicked() {
        Alarm clickedAlarm = getClickedAlarm();
        if (clickedAlarm != null) {

            hour = clickedAlarm.getHour();
            minute = clickedAlarm.getMinute();
            timeButton.setText(String.format("%02d:%02d", hour, minute));
            alarmName.setText(clickedAlarm.getName());
            alarmMissionName.setText(clickedAlarm.getMission());
            alarmSoundName.setText(clickedAlarm.getSoundName());
            sunday.setChecked(clickedAlarm.isSunday());
            monday.setChecked(clickedAlarm.isMonday());
            tuesday.setChecked(clickedAlarm.isTuesday());
            wednesday.setChecked(clickedAlarm.isWednesday());
            thursday.setChecked(clickedAlarm.isThursday());
            friday.setChecked(clickedAlarm.isFriday());
            saturday.setChecked(clickedAlarm.isSaturday());
            soundSwitch.setChecked(clickedAlarm.hasSound());
            vibrateSwitch.setChecked(clickedAlarm.hasVibrate());
            missionSwitch.setChecked(clickedAlarm.hasMission());
            useMyContactsSwitch.setChecked(clickedAlarm.hasUseMyContacts());

        }

    }

    private void addAlarmToDataBase_ifNotAlreadyExist(Alarm newAlarm) {
        //To check if the newAlarm dose not already exists.
        alarmList_FromIntent = getIntent().getParcelableArrayListExtra("AlarmList");
        if (!(newAlarm.alreadyExist(alarmList_FromIntent))) {


            if (getClickedAlarm() == null) {
                //Add alarm to database. When we opened this activity through add button.
                DataBaseHelper.database.addAlarmToDataBase(newAlarm);


            } else {
                //Change clicked alarm settings. When we opened this activity through an alarm click.
                DataBaseHelper.database.changeAlarmSettings(getClickedAlarm().getId(), newAlarm);
            }
            // Toast message with the remaining time until the alarm
            Toast.makeText(CreateAlarm.this, newAlarm.getHowMuchTimeTillAlarm(), Toast.LENGTH_SHORT).show();

        } else {
            //Toast message that says alarm already exist
            Toast.makeText(CreateAlarm.this, newAlarm.toString() + " כבר הוגדרה", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackIconCreateAlarm(View view) {
        super.onBackPressed();
    }


    //Needed in order to launch the MANAGE_OVERLAY_PERMISSION screen. and to handle the result.
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!Settings.canDrawOverlays(CreateAlarm.this)) {
            Toast.makeText(CreateAlarm.this, "בלי הרשאה זו, לא תוכל ליצור התראה.", Toast.LENGTH_SHORT).show();
        }
    });

    //When I come back from ChooseSound or Contacts
    @Override
    protected void onResume() {
        super.onResume();
        //Change alarm sound name to the last chosen one. If there is no chosen song put "Homecoming"
        alarmSoundName.setText(chooseSoundPreferences.getString(ChooseSound.SOUND_NAME, "Homecoming"));

    }
}

