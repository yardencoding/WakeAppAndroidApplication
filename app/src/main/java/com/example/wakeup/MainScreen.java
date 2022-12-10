package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainScreen extends AppCompatActivity implements RecyclerViewInterface {

    private RecyclerView recyclerView;
    private TextView firstMessage;
    private ArrayList<Alarm> alarmList;
    private AlarmAdapter adapter;
    private Thread updateAlarmTime_thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


        // "התראות" text
        firstMessage = findViewById(R.id.first_message);

        // contains data from Firebase
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


    // when add button is clicked
    public void createAlarm(View view) {
        Intent intent = new Intent(this, CreateAlarm.class);
        intent.putParcelableArrayListExtra("AlarmList", alarmList);
        startActivity(intent);
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

        //Delete clicked alarm from list, because the values might be changed.
        alarmList.remove(clickedAlarm);

        intent.putParcelableArrayListExtra("AlarmList", alarmList);

        startActivity(intent);
    }

    private void changeFirstMessageToAlarmTime(){
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


}

