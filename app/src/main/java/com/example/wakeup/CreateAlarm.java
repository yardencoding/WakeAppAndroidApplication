package com.example.wakeup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
    private ActivityResultLauncher activityResultLauncher;
    private boolean hasSelectedTime = false;

    //To store the sound name in SharedPreferences
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String SOUND_NAME = "SOUND_NAME";



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


        //If we opened this activity through an alarm click,
        // change fields values to the corresponding clickedAlarm values.
        alarmWasClicked();

        //Retrieve the sound name from ChooseSound activity.
        initializeResultActivity();

        if(getClickedAlarm() == null){
            loadDataFromSharedPreferences();
            alarmSoundName.setText(loadDataFromSharedPreferences());
        }

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

    private void initializeResultActivity() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //This function gets called when we open this activity through ChooseSound activity.
                        if (result.getResultCode() == 1) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                //Extract data from ChooseSound activity
                                String soundName = intent.getStringExtra("resultText");
                                int soundId = intent.getIntExtra("resultId", 0); //Will be used when creating alarm.
                                alarmSoundName.setText(soundName);
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.choose_time_btn:
                popTimePicker();
                break;
            case R.id.alarm_sound_btn:
                Intent goToChooseSound = new Intent(this, ChooseSound.class);
                goToChooseSound.putExtra("soundName", alarmSoundName.getText().toString());
                activityResultLauncher.launch(goToChooseSound);
                break;

            case R.id.alarm_contacts_btn:
                Intent goToContact = new Intent(this, Contact.class);
                startActivity(goToContact);
                break;

            case R.id.save_alarm_ImageButton:
                createAlarm();
        }

    }

    private void createAlarm() {

        if(postNotificationWasGranted() && timeWasChosen()){

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

            //Save sound name to SharedPreferences
            saveSoundName();

            // schedule the alarm
            scheduleAlarm(newAlarm);

            // go to the first activity
            Intent goToMainScreen = new Intent(this, MainScreen.class);
            startActivity(goToMainScreen);

        }
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

    private Alarm getClickedAlarm() {
        Intent intent = getIntent();
        Alarm clickedAlarm = intent.getParcelableExtra("getClickedAlarm");
        return clickedAlarm;
    }

    private void addAlarmToDataBase_ifNotAlreadyExist(Alarm newAlarm) {
        //To check if the newAlarm dose not already exists.
        ArrayList<Alarm> alarmList_FromIntent = getIntent().getParcelableArrayListExtra("AlarmList");
        if (!(newAlarm.alreadyExist(alarmList_FromIntent))) {


            if (getClickedAlarm() == null) {
                //Add alarm to database. When we opened this activity through add button.
                DataBaseHelper.database.addAlarmToDataBase(newAlarm);

            } else {
                //Change clicked alarm settings. When we opened this activity through an alarm click.
                DataBaseHelper.database.changeAlarmSettings(getClickedAlarm().getId(), newAlarm);
            }
            // Toast message with the remaining time until the alarm
            Toast.makeText(CreateAlarm.this, newAlarm.getHowMuchTimeTillAlarm(), Toast.LENGTH_LONG).show();

        } else {
            //Toast message that says alarm already exist
            Toast.makeText(CreateAlarm.this, newAlarm.toString() + " כבר הוגדרה", Toast.LENGTH_LONG).show();
        }
    }

    //sharedPreferences setup
    private void saveSoundName() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SOUND_NAME, alarmSoundName.getText().toString());
        editor.apply();
    }

    private String loadDataFromSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String defaultStringValue = "Homecoming";
        return sharedPreferences.getString(SOUND_NAME, defaultStringValue);
    }

    public void scheduleAlarm(Alarm alarm){


        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("HAS_SOUND_ACTIVE", alarm.hasSound());
        intent.putExtra("HAS_VIBRATE_ACTIVE", alarm.hasVibrate());
        intent.putExtra("HAS_USE_CONTACTS_ACTIVE", alarm.hasUseMyContacts());
        intent.putExtra("HAS_MISSION_ACTIVE", alarm.hasMission());
        intent.putExtra("TITLE", alarm.getName());

        long milliseconds = alarm.getAlarmLocalDateTime().withSecond(0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int)milliseconds, intent, PendingIntent.FLAG_IMMUTABLE);

        if(alarm.isRecurring() == false)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, milliseconds, pendingIntent);
        /*
        else
        handle recurring alarms
         */

    }

    public void onBackIconCreateAlarm(View view) {
        super.onBackPressed();
    }


}

