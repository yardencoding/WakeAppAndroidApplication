package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.TimePickerDialog;
import android.content.Intent;
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

import java.util.ArrayList;

public class CreateAlarm extends AppCompatActivity implements View.OnClickListener {

    private Button timeButton;
    private int hour = 6, minute = 0;
    private ToggleButton sunday, monday, tuesday, wednesday, thursday,
            friday, saturday;
    private EditText alarmName;
    private TextView alarmMissionName;
    private ImageButton saveImageButton;
    private Switch soundSwitch, vibrateSwitch, missionSwitch, useMyContactsSwitch;
    private Button chooseSoundButton, missionButton, useContactsButton;
    private ConstraintLayout createAlarmLayout;


    private boolean hasSelectedTime = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        timeButton = findViewById(R.id.choose_time_btn);
        timeButton.setOnClickListener(this);

        saveImageButton = findViewById(R.id.alarm_save_imageButton);
        saveImageButton.setOnClickListener(this);

        chooseSoundButton = findViewById(R.id.alarm_song_btn);
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
        soundSwitch = findViewById(R.id.alarm_sound_switch);
        vibrateSwitch = findViewById(R.id.alarm_vibrate_switch);
        missionSwitch = findViewById(R.id.alarm_mission_switch);
        useMyContactsSwitch = findViewById(R.id.alarm_contacts_switch);

        createAlarmLayout = findViewById(R.id.f1);

        //If we opened this activity through an alarm click,
        // change fields values to the corresponding clickedAlarm values.
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
    public boolean dispatchKeyEvent(KeyEvent event) {

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == timeButton.getId())
            popTimePicker();

        else if (view.getId() == chooseSoundButton.getId()) {
            Intent goToChooseSound = new Intent(this, ChooseSound.class);
            startActivity(goToChooseSound);
        }

       else if (getClickedAlarm() == null && hasSelectedTime == false) {
            // Check if time was selected, When we opened this activity through add button.
            Toast.makeText(this, "לא הוגדרה שעה", Toast.LENGTH_SHORT).show();
            return; //To not create an alarm

        } else {


            String name = alarmName.getText().toString();
            String mission = alarmMissionName.getText().toString();
            Alarm newAlarm = new Alarm(
                    true,
                    hour,
                    minute,
                    name,
                    mission,
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

            // go to the first activity
            Intent goToNainScreen = new Intent(this, MainScreen.class);
            startActivity(goToNainScreen);
        }
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


}

