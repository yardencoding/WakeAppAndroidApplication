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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainScreen extends AppCompatActivity implements RecyclerViewInterface, View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView firstMessage;
    public static ArrayList<Alarm> alarmList;
    public static AlarmAdapter adapter;
    private Thread updateAlarmTime_thread;
    private Button addAlarmButton;

    public static final String ALARM_RING_CHANNEL_ID = "alarmRingNotification_Id";

    public static final int POST_NOTIFICATION_REQUEST_CODE = 2;
    public static final int SEND_SMS_REQUEST_CODE = 3;

    public static final int CAMERA_REQUEST_CODE = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Add alarm button
        addAlarmButton = findViewById(R.id.add_btn);
        addAlarmButton.setOnClickListener(this);


        // "התראות" text
        firstMessage = findViewById(R.id.first_message);


        // Contains data from SQLite Database
        alarmList = new ArrayList<Alarm>();

        // build RecyclerView
        recyclerView = findViewById(R.id.alarms_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new AlarmAdapter(alarmList, this);
        recyclerView.setAdapter(adapter);


        //Download alarms from Database and display on recyclerView
        DataBaseHelper.database = new DataBaseHelper(MainScreen.this);
        alarmList.addAll(DataBaseHelper.database.getAllAlarmsFromDataBase());

        //Sorting them so the closest one will be on top
        Alarm.sortAlarms(alarmList);
        adapter.notifyDataSetChanged();

        updateFirstMessage_thread();

        createNotificationChannel();

        //Request showing notification permission.
        requestNotificationPermission();


    }


    // Updates the text with the remaining time until the alarm.
    // must be a thread because otherwise the application will be frozen.

    public void updateFirstMessage_thread() {
        final int MILLISECONDS_TO_SLEEP = 100; //So it does not freeze the UI
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

        Alarm deletedAlarm = alarmList.get(position);

        //Cancel AlarmManger
        deletedAlarm.cancel(MainScreen.this);

        // Remove alarm from database
        DataBaseHelper.database.deleteAlarm(deletedAlarm);

        // Remove alarm from list
        alarmList.remove(deletedAlarm);


        // Update adapter
        adapter.notifyItemRemoved(position);

        Toast.makeText(this, deletedAlarm.toString() + " נמחקה", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onItemClick(int position) {

        Intent intent = new Intent(this, CreateAlarm.class);
        Alarm clickedAlarm = alarmList.get(position);
        intent.putExtra("getClickedAlarm", clickedAlarm);

        //To check that I don't change alarm settings to an already existing alarm.
        intent.putParcelableArrayListExtra("AlarmList", alarmList);

        startActivity(intent);
    }

    private void changeFirstMessageToAlarmTime() {

        Intent statusBarService = new Intent(this, StatusBarNotificationService.class);

        Alarm alarm = Alarm.getFirstActiveAlarm(alarmList);
        if (alarm != null) {
            firstMessage.setText(alarm.getHowMuchTimeTillAlarm());
            startForegroundService(statusBarService);
        } else {
            stopService(statusBarService);
            //Check if the list is not empty because if it is empty there are no alarms to be inactive.
            if (alarmList.isEmpty() == false)
                firstMessage.setText("כל ההתראות כבויות");
            else
                firstMessage.setText("התראות");
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
        intent.putParcelableArrayListExtra("AlarmList", alarmList);
        startActivity(intent);
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATION_REQUEST_CODE);
      }


    //Notification channel, needed for sdk 26 and above
    private void createNotificationChannel(){
          NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          NotificationChannel notificationChannel = new NotificationChannel(ALARM_RING_CHANNEL_ID, "התראות שעון מעורר", NotificationManager.IMPORTANCE_MIN);
          notificationManager.createNotificationChannel(notificationChannel);
      }

    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Toast.makeText(MainScreen.this, "כעת האפליקציה תוכל להציג התראות מעל אפליקציות אחרות", Toast.LENGTH_SHORT).show();
            } else{
               Toast.makeText(MainScreen.this, "האפליקציה לא תוכל להציג התראות מעל אפליקציות אחרות", Toast.LENGTH_SHORT).show();
            }
        }
    });



}



