package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private boolean hasSelectedTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        timeButton = findViewById(R.id.choose_time_btn);
        saveImageButton = findViewById(R.id.alarm_save_imageButton);
        saveImageButton.setOnClickListener(this);


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


        //If we opened this activity through an alarm click,
        // change fields values to the corresponding clickedAlarm values.
        alarmWasClicked();
    }

    // crates a timePicker dialog when "בחר שעה" btn is clicked
    public void popTimePicker(View view) {


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

        if(getClickedAlarm() == null) //if We didn't came through an alarm click.
        if(hasSelectedTime == false) {
            Toast.makeText(this, "לא הוגדרה שעה", Toast.LENGTH_SHORT).show();
            return; //To not create an alarm
        }

        String name = alarmName.getText().toString();
        String mission = alarmMissionName.getText().toString();

        //If we opened this activity through add button.

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


        //To check if the newAlarm dose not already exists.
        ArrayList<Alarm> alarmList_FromIntent = getIntent().getParcelableArrayListExtra("AlarmList");
        if (!(newAlarm.alreadyExist(alarmList_FromIntent))) {


            if(getClickedAlarm() == null) {
                //Add alarm to database. When we opened this activity through add button.
                DataBaseHelper.database.addAlarmToDataBase(newAlarm);
                Log.d("TAG", "add button click ");

            } else{
                //Change clicked alarm settings. When we opened this activity through an alarm click.
                DataBaseHelper.database.changeAlarmSettings(getClickedAlarm().getId(), newAlarm);
                Log.d("TAG", "alarm  click ");

            }
            // Toast message with the remaining time until the alarm
            Toast.makeText(CreateAlarm.this, newAlarm.getHowMuchTimeTillAlarm(), Toast.LENGTH_LONG).show();

        } else {
            //Toast message that says alarm already exist
            Toast.makeText(CreateAlarm.this, newAlarm.toString() + " כבר הוגדרה", Toast.LENGTH_LONG).show();
        }




        // go to the first activity
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);

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



}

