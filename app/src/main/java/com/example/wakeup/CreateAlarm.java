package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
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

import java.util.ArrayList;

public class CreateAlarm extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


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
    private  SharedPreferences chooseSoundPreferences;

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
        if(getClickedAlarm() != null)
            alarmWasClicked();

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

        if (postNotificationWasGranted() && timeWasChosen()) {

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


            // schedule the alarm
            if (getClickedAlarm() != null) //If we want to update this alarm, delete the previous one.
                getClickedAlarm().cancel(this);

            newAlarm.setVolume(chooseSoundPreferences.getInt(ChooseSound.SOUND_VOLUME, 60) / (100 / maxVolume));
            newAlarm.setId(alarmList_FromIntent.size() + 1);
            newAlarm.schedule(this);


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
            Toast.makeText(CreateAlarm.this, newAlarm.getHowMuchTimeTillAlarm(), Toast.LENGTH_LONG).show();

        } else {
            //Toast message that says alarm already exist
            Toast.makeText(CreateAlarm.this, newAlarm.toString() + " כבר הוגדרה", Toast.LENGTH_LONG).show();
        }
    }

    public void onBackIconCreateAlarm(View view) {
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        //If useContacts is checked and there are no contacts added, then go to the Contact activity to add
        if (buttonView.getId() == useMyContactsSwitch.getId()) {
            SharedPreferences preferences = getSharedPreferences(Contact.SHARED_PREFS, MODE_PRIVATE);
            String contact1 = preferences.getString(Contact.PHONE_NUMBER_1, "");
            String contact2 = preferences.getString(Contact.PHONE_NUMBER_2, "");
            String contact3 = preferences.getString(Contact.PHONE_NUMBER_3, "");
            if (isChecked)
                if (contact1.isEmpty() && contact2.isEmpty() && contact3.isEmpty()) {
                    Toast.makeText(CreateAlarm.this, "על מנת להשתמש באופציה הזאת יש להוסיף אנשי קשר", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateAlarm.this, Contact.class));
                }

        } else if(buttonView.getId() == missionSwitch.getId()){
            //Choose mission is pressed
        }
    }



    //When I come back from ChooseSound or Contacts
    @Override
    protected void onResume() {
        super.onResume();
        //Change alarm sound name to the last chosen one. If there is no chosen song put "Homecoming"
        alarmSoundName.setText(chooseSoundPreferences.getString(ChooseSound.SOUND_NAME, "Homecoming"));

    }
}

