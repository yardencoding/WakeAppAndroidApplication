package com.example.wakeup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainScreen extends AppCompatActivity implements RecyclerViewInterface, View.OnClickListener {

    private RecyclerView alarmsRecyclerView;
    private TextView firstMessageTextView;
    private ArrayList<Alarm> alarms;
    private AlarmAdapter adapter;
    private Thread updateAlarmTime_thread;
    private Button addAlarmButton;

    public static final String ALARM_RING_CHANNEL_ID = "alarmRingNotification_Id";

    public static final int POST_NOTIFICATION_REQUEST_CODE = 2;
    public static final int SEND_SMS_REQUEST_CODE = 3;

    public static final int CAMERA_REQUEST_CODE = 4;

    public static final int RECORD_AUDIO_REQUEST_CODE = 5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Add alarm button
        addAlarmButton = findViewById(R.id.add_alarm_button);
        addAlarmButton.setOnClickListener(this);


        // "התראות" text
        firstMessageTextView = findViewById(R.id.first_message_text_view);

        // Contains data from SQLite Database
        alarms = new ArrayList<Alarm>();

        //Download alarms from Database and display on recyclerView
        DataBaseHelper.database = new DataBaseHelper(MainScreen.this);
        alarms.addAll(DataBaseHelper.database.getAllAlarmsFromDataBase());

        //Sorting them so the closest one will always be on top
        Alarm.sortAlarms(alarms);

        // build RecyclerView
        alarmsRecyclerView = findViewById(R.id.alarms_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        alarmsRecyclerView.setLayoutManager(layoutManager);
        adapter = new AlarmAdapter(alarms, this);
        alarmsRecyclerView.setAdapter(adapter);


        updateFirstMessage_thread();

        createNotificationChannel();

        //Request showing notification permission.
        requestNotificationPermission();


    }


    // Updates the text with the remaining time until the alarm.
    // must be a thread because otherwise the application will be frozen.

    public void updateFirstMessage_thread() {
        final int MILLISECONDS_TO_SLEEP = 300; //So it does not freeze the UI
        updateAlarmTime_thread = new Thread() {
            public void run() {
                while (Thread.currentThread().isInterrupted() == false) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeFirstMessageToAlarmTime();

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
        updateAlarmTime_thread.start();
    }


    @Override
    public void onItemLongClick(int position) {

        Alarm deletedAlarm = alarms.get(position);

        //Cancel AlarmManger
        deletedAlarm.cancel(MainScreen.this);

        // Remove alarm from database
        DataBaseHelper.database.deleteAlarm(deletedAlarm);

        // Remove alarm from list
        alarms.remove(deletedAlarm);


        // Update adapter
        adapter.notifyItemRemoved(position);

        Toast.makeText(this, deletedAlarm.toString() + " נמחקה", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(int position) {

        Intent intent = new Intent(this, CreateAlarm.class);
        Alarm clickedAlarm = alarms.get(position);
        intent.putExtra("getClickedAlarm", clickedAlarm);
        startActivity(intent);
    }

    private void changeFirstMessageToAlarmTime() {

        Intent statusBarNotificationService = new Intent(this, StatusBarNotificationService.class);

        Alarm alarm = Alarm.getFirstActiveAlarm();
        if (alarm != null) {
            firstMessageTextView.setText(alarm.getHowMuchTimeTillAlarm());
            if (!StatusBarNotificationService.IS_STATUS_BAR_SERVICE_RUNNING)
                startForegroundService(statusBarNotificationService);
        } else {
            //Check if the list is not empty because if it is empty there are no alarms to be inactive.
            if (!alarms.isEmpty()) {
                firstMessageTextView.setText("כל ההתראות כבויות");

            }
            else {
                firstMessageTextView.setText("התראות");
            }

            if (StatusBarNotificationService.IS_STATUS_BAR_SERVICE_RUNNING)
                stopService(statusBarNotificationService);


        }
    }


    @Override
    protected void onDestroy() {
        updateAlarmTime_thread.interrupt();
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {

        //When add button is clicked
        Intent intent = new Intent(this, CreateAlarm.class);
        startActivity(intent);
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    POST_NOTIFICATION_REQUEST_CODE);
    }


    //Notification channel, needed for sdk 26 and above
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(ALARM_RING_CHANNEL_ID, "התראות שעון מעורר", NotificationManager.IMPORTANCE_MIN);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Toast.makeText(MainScreen.this, "כעת האפליקציה תוכל להציג התראות מעל אפליקציות אחרות", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainScreen.this, "האפליקציה לא תוכל להציג התראות מעל אפליקציות אחרות", Toast.LENGTH_SHORT).show();
            }
        }
    });


    //There is no need for the back button to work on this Screen. Because in order to create a new alarm it makes more sense to press the add button.
    @Override
    public void onBackPressed() {
    }
}



