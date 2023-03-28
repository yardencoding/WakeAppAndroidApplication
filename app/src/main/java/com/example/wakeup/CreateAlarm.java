package com.example.wakeup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;

public class CreateAlarm extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private Button chooseTimeButton;
    private int hour = 6, minute = 0;
    private ToggleButton sundayToggleButton, mondayToggleButton, tuesdayToggleButton, wednesdayToggleButton, thursdayToggleButton,
            fridayToggleButton, saturdayToggleButton;
    private EditText alarmNameEditText;
    private TextView alarmMissionNameTextView, alarmSoundNameTextView;

    private ImageButton saveAlarmImageButton;
    private Switch alarmSoundSwitch, alarmVibrateSwitch, alarmMissionSwitch, alarmContactsSwitch;
    private Button alarmSoundButton, alarmMissionButton, alarmContactsButton;

    private boolean hasSelectedTime = false;


    private ArrayList<Alarm> alarms;

    //To get the sound name from ChooseSound Activity.
    private SharedPreferences chooseSoundPreferences;


    private Thread changeShowAlarmDaysThread;

    private TextView showAlarmDaysTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_alarm);


        chooseTimeButton = findViewById(R.id.choose_time_button);
        chooseTimeButton.setOnClickListener(this);

        saveAlarmImageButton = findViewById(R.id.save_alarm_image_button);
        saveAlarmImageButton.setOnClickListener(this);

        alarmSoundButton = findViewById(R.id.alarm_sound_button);
        alarmSoundButton.setOnClickListener(this);

        alarmMissionButton = findViewById(R.id.alarm_mission_button);
        alarmMissionButton.setOnClickListener(this);

        alarmContactsButton = findViewById(R.id.alarm_contacts_button);
        alarmContactsButton.setOnClickListener(this);

        showAlarmDaysTextView = findViewById(R.id.show_alarm_days_text_view);


        sundayToggleButton = findViewById(R.id.sunday_toggle_button);
        mondayToggleButton = findViewById(R.id.monday_toggle_button);
        tuesdayToggleButton = findViewById(R.id.tuesday_toggle_button);
        wednesdayToggleButton = findViewById(R.id.wednesday_toggle_button);
        thursdayToggleButton = findViewById(R.id.thursday_toggle_button);
        fridayToggleButton = findViewById(R.id.friday_toggle_button);
        saturdayToggleButton = findViewById(R.id.saturday_toggle_button);
        alarmNameEditText = findViewById(R.id.alarm_name_edit_text);
        alarmMissionNameTextView = findViewById(R.id.alarm_mission_name_text_view);
        alarmSoundNameTextView = findViewById(R.id.alarm_sound_name_text_view);
        alarmSoundSwitch = findViewById(R.id.alarm_sound_switch);
        alarmVibrateSwitch = findViewById(R.id.alarm_vibrate_switch);
        alarmMissionSwitch = findViewById(R.id.alarm_mission_switch);
        alarmContactsSwitch = findViewById(R.id.alarm_contacts_switch);


        chooseSoundPreferences = getSharedPreferences(ChooseSound.SHARED_PREFS, MODE_PRIVATE);
        alarmSoundNameTextView.setText(chooseSoundPreferences.getString(ChooseSound.SOUND_NAME, "Homecoming"));


        //If we opened this activity through an alarm click. change the fields to match alarm fields.
        if (getClickedAlarm() != null)
            alarmWasClicked();


        //To request SEND SMS permission when the contacts switch is checked.
        alarmContactsSwitch.setOnCheckedChangeListener(this);
        //If it is checked and I didn't chose a mission yet, open the mission dialog.
        alarmMissionSwitch.setOnCheckedChangeListener(this);


        // start the thread that updates show_alarm_days_text_view
        updateThread();
    }


    // Creates a timePicker dialog when "בחר שעה" btn is clicked.
    private void popTimePicker() {

        MaterialTimePicker materialTimePicker = new
                MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(hour).setMinute(minute).setTheme(R.style.ThemeOverlay_MaterialComponents_TimePicker).build();
        materialTimePicker.addOnPositiveButtonClickListener(view -> {
            hasSelectedTime = true;

            hour = materialTimePicker.getHour();
            minute = materialTimePicker.getMinute();
            chooseTimeButton.setTextSize(38);
            chooseTimeButton.setText(String.format("%02d:%02d", hour, minute));

        });
        materialTimePicker.show(getSupportFragmentManager(), null);

    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.choose_time_button:
                popTimePicker();
                break;
            case R.id.alarm_sound_button:
                startActivity(new Intent(CreateAlarm.this, ChooseSound.class));
                break;

            case R.id.alarm_contacts_button:
                if (hasSendSmsPermission())
                    startActivity(new Intent(CreateAlarm.this, Contact.class));
                else
                    Toast.makeText(CreateAlarm.this, "יש לאשר את הרשאת שליחת SMS", Toast.LENGTH_SHORT).show();
                break;

            case R.id.alarm_mission_button:
                showMissionDialog();
                break;

            case R.id.save_alarm_image_button:
                createAlarm();
        }

    }

    private void showMissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.mission_dialog, null);

        builder.setView(dialogView)
                .setPositiveButton("שמור", (dialog, which) -> {
                    // Handle save button click
                    RadioGroup radioGroup = dialogView.findViewById(R.id.mission_dialog_radio_group);
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    switch (selectedId) {
                        case R.id.mission_dialog_smile_radio_button:
                            requestCameraPermission();
                            alarmMissionNameTextView.setText(" צילום חיוך");
                            break;
                        case R.id.mission_dialog_maze_radio_button:
                            alarmMissionNameTextView.setText(" פתירת מבוך");
                            break;
                        case R.id.mission_dialog_water_from_stream_radio_button:
                            requestCameraPermission();
                            alarmMissionNameTextView.setText("הלקטת ברז פתוח");
                            break;
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("בטל", (dialog, which) -> {

                    //So the user won't be able to have mission switch checked without choosing a mission.
                    if(alarmMissionNameTextView.getText().toString().isEmpty())
                        alarmMissionSwitch.setChecked(false);

                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createAlarm() {

        if (postNotificationWasGranted()
                && timeWasChosen()
                && makeSureThatContactsWereAdded()
                && hasDisplayOverOtherAppsPermission()
                && makeSureTheCameraPermission_WasGranted_IfNeeded()
        ) {

            //Create Alarm fields
            String name = alarmNameEditText.getText().toString();
            String mission = alarmMissionNameTextView.getText().toString();
            String soundName = alarmSoundNameTextView.getText().toString();
            Alarm newAlarm = new Alarm(
                    true,
                    hour,
                    minute,
                    name,
                    mission,
                    soundName,
                    sundayToggleButton.isChecked(),
                    mondayToggleButton.isChecked(),
                    tuesdayToggleButton.isChecked(),
                    wednesdayToggleButton.isChecked(),
                    thursdayToggleButton.isChecked(),
                    fridayToggleButton.isChecked(),
                    saturdayToggleButton.isChecked(),
                    alarmSoundSwitch.isChecked(),
                    alarmVibrateSwitch.isChecked(),
                    alarmMissionSwitch.isChecked(),
                    alarmContactsSwitch.isChecked(),
                    hasSelectedDay()
            );

            if (!newAlarm.isRecurring()) {
                newAlarm.whenNoDay_WasChosen();
            }

            if (addAlarmToDataBase_ifNotAlreadyExist(newAlarm)) {

                //set the id of the new alarm based on the AlarmList size.
                int newAlarmId;
                if (getClickedAlarm() != null) { //If we want to update this alarm, delete the previous one.
                    getClickedAlarm().cancel(this);
                    newAlarmId = alarms.size();
                } else {
                    newAlarmId = alarms.size() + 1;
                }

                // schedule the alarm
                newAlarm.setId(newAlarmId);

                newAlarm.schedule(this);

            }

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
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
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
        if (alarmContactsSwitch.isChecked()) {
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
            chooseTimeButton.setText(String.format("%02d:%02d", hour, minute));
            alarmNameEditText.setText(clickedAlarm.getName());
            alarmMissionNameTextView.setText(clickedAlarm.getMission());
            alarmSoundNameTextView.setText(clickedAlarm.getSoundName());

            //only mark the days if the alarm is recurring.
            if (clickedAlarm.isRecurring()) {
                sundayToggleButton.setChecked(clickedAlarm.isSunday());
                mondayToggleButton.setChecked(clickedAlarm.isMonday());
                tuesdayToggleButton.setChecked(clickedAlarm.isTuesday());
                wednesdayToggleButton.setChecked(clickedAlarm.isWednesday());
                thursdayToggleButton.setChecked(clickedAlarm.isThursday());
                fridayToggleButton.setChecked(clickedAlarm.isFriday());
                saturdayToggleButton.setChecked(clickedAlarm.isSaturday());
            }

            alarmSoundSwitch.setChecked(clickedAlarm.hasSound());
            alarmVibrateSwitch.setChecked(clickedAlarm.hasVibrate());
            alarmMissionSwitch.setChecked(clickedAlarm.hasMission());
            alarmContactsSwitch.setChecked(clickedAlarm.hasUseMyContacts());

        }
    }

    private boolean addAlarmToDataBase_ifNotAlreadyExist(Alarm newAlarm) {


        //To check if the newAlarm dose not already exists.
        alarms = DataBaseHelper.database.getAllAlarmsFromDataBase();

        //When we clicked an alarm and don't change the time or days, only other fields. We have to check this because otherwise the alarm will be equal to itself.
        if (getClickedAlarm() != null) {
            if (getClickedAlarm().equals(newAlarm)) {
                DataBaseHelper.database.changeAlarmSettings(getClickedAlarm().getId(), newAlarm);
                return true;
            }
        }

        if (!newAlarm.alreadyExist(alarms)) {


            if (getClickedAlarm() == null) {
                //Add alarm to database. When we opened this activity through add button.
                DataBaseHelper.database.addAlarmToDataBase(newAlarm);


            } else {
                //Change clicked alarm settings. When we opened this activity through an alarm click.
                newAlarm.cancel(this); //Cancel the previous alarm.So that we are able to override the settings.
                DataBaseHelper.database.changeAlarmSettings(getClickedAlarm().getId(), newAlarm);
            }
            // Toast message with the remaining time until the alarm
            Toast.makeText(CreateAlarm.this, newAlarm.getHowMuchTimeTillAlarm(), Toast.LENGTH_SHORT).show();

            return true; //added successfully

        } else {
            //Toast message that says alarm already exist
            Toast.makeText(CreateAlarm.this,
                    "לא ניתן ליצור את ההתראה, משום שהיא מתנגשת עם התראה קיימת ",
                    Toast.LENGTH_SHORT).show();

            return false; //wasn't added
        }
    }



    //Needed in order to launch the MANAGE_OVERLAY_PERMISSION screen. and to handle the result.
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!Settings.canDrawOverlays(CreateAlarm.this)) {
            Toast.makeText(CreateAlarm.this, "בלי הרשאה זו, לא תוכל ליצור התראה.", Toast.LENGTH_SHORT).show();
        }
    });

    //When I come back from ChooseSound
    @Override
    protected void onResume() {
        super.onResume();
        //Change alarm sound name to the last chosen one. If there is no chosen song put "Homecoming"
        alarmSoundNameTextView.setText(chooseSoundPreferences.getString(ChooseSound.SOUND_NAME, "Homecoming"));


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {

            // If I the alarmMissionSwitch is checked and I didn't choose a mission yet, open the mission dialog.
            if (buttonView.getId() == alarmMissionSwitch.getId()) {
                if (alarmMissionNameTextView.getText().toString().isEmpty())
                    showMissionDialog();

            } else {
                //alarmContactsSwitch
                requestSendSmsPermission();
            }
        }
    }

    private void requestCameraPermission() {
        if (!hasCameraPermission()) {
            Toast.makeText(this,
                    "יש צורך בגישה למצלמה על מנת להשתמש במשימה שבחרת", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(CreateAlarm.this,
                    new String[]{Manifest.permission.CAMERA},
                    MainScreen.CAMERA_REQUEST_CODE);
        }
    }

    private boolean hasCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }


    //To make sure the user is not able to use צילום חיוך or צילום מים מהברז missions without allowing camera permission.
    private boolean makeSureTheCameraPermission_WasGranted_IfNeeded() {
        if (alarmMissionSwitch.isChecked()) {
            if (alarmMissionNameTextView.getText().equals(" צילום חיוך") || alarmMissionNameTextView.getText().equals(" צילום מים מהברז"))
                if (!hasCameraPermission()) {
                    requestCameraPermission();
                    return false;
                }
        }
        return true;
    }


    private boolean hasSendSmsPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }

    private void requestSendSmsPermission() {

        if (!hasSendSmsPermission())
            ActivityCompat.requestPermissions(CreateAlarm.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MainScreen.SEND_SMS_REQUEST_CODE);

    }

    //if One of days was chosen then the alarm is recurring.
    private boolean hasSelectedDay() {

        return sundayToggleButton.isChecked() ||
                mondayToggleButton.isChecked() ||
                tuesdayToggleButton.isChecked() ||
                wednesdayToggleButton.isChecked() ||
                thursdayToggleButton.isChecked() ||
                fridayToggleButton.isChecked() ||
                saturdayToggleButton.isChecked();
    }


    //Is used in order to update the show_alarm_days_text_view with the current days that were chosen. So that the user has a better understanding of the app.
    private void updateThread() {
        final int MILLISECONDS_TO_SLEEP = 200; //So it does not freeze the UI
        changeShowAlarmDaysThread = new Thread() {
            public void run() {
                while (Thread.currentThread().isInterrupted() == false) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //If a time was selected or it was open through an alarm click
                            if (hasSelectedTime || getClickedAlarm() != null) {

                                LocalDateTime selectedTime = LocalDateTime.now();
                                selectedTime = selectedTime.withHour(hour);
                                selectedTime = selectedTime.withMinute(minute);
                                LocalDateTime currentTime = LocalDateTime.now();

                                //if the user didn't select a day
                                if (!hasSelectedDay()) {

                                    // if the time had passed
                                    if (selectedTime.isBefore(currentTime)) {

                                        selectedTime = selectedTime.plusDays(1);
                                        showAlarmDaysTextView.setText("מחר- "
                                                + selectedTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("he-IL"))
                                                + ", " + selectedTime.getDayOfMonth() + "/" + selectedTime.getMonthValue()
                                        );

                                        //if the time didn't pass yet
                                    } else {
                                        // היום-יום שישי, 17 במרץ
                                        showAlarmDaysTextView.setText("היום- "
                                                + selectedTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("he-IL"))
                                                + ", " + selectedTime.getDayOfMonth() + "/" + selectedTime.getMonthValue()
                                        );

                                    }

                                } else {
                                    //if a day was chosen

                                    // More efficient because it dose not create a new String in memory each time we change the text
                                    StringBuilder daysStringBuilder = new StringBuilder();
                                    if (sundayToggleButton.isChecked())
                                        daysStringBuilder.append("א', ");
                                    if (mondayToggleButton.isChecked())
                                        daysStringBuilder.append("ב', ");
                                    if (tuesdayToggleButton.isChecked())
                                        daysStringBuilder.append("ג', ");
                                    if (wednesdayToggleButton.isChecked())
                                        daysStringBuilder.append("ד', ");
                                    if (thursdayToggleButton.isChecked())
                                        daysStringBuilder.append("ה', ");
                                    if (fridayToggleButton.isChecked())
                                        daysStringBuilder.append("ו', ");
                                    if (saturdayToggleButton.isChecked())
                                        daysStringBuilder.append("ש'");
                                    showAlarmDaysTextView.setText("מדי " + daysStringBuilder);

                                }
                            }


                        }
                    });

                    try {
                        Thread.sleep(MILLISECONDS_TO_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        changeShowAlarmDaysThread.start();
    }

    @Override
    protected void onDestroy() {
        changeShowAlarmDaysThread.interrupt();
        super.onDestroy();
    }
}
