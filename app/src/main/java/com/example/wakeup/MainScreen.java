package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainScreen extends AppCompatActivity implements RecyclerViewInterface, View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView firstMessage;
    private ArrayList<Alarm> alarmList;
    private AlarmAdapter adapter;
    private Thread updateAlarmTime_thread;
    private Button addAlarmButton;


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
        Alarm alarm = Alarm.getFirstActiveAlarm(alarmList);
        if (alarm != null) {
            firstMessage.setText(alarm.getHowMuchTimeTillAlarm());
        } else {
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2);
        }
    }

