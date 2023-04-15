package com.example.wakeup;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //reschedule alarms. Because AlarmManger loses all of his alarms after reboot
            for (Alarm alarm : DataBaseHelper.database.getAllAlarmsFromDataBase())
                if (alarm.isActive())
                    alarm.schedule(context);

        } else {

            //Open onPopAlarm activity
            Alarm alarm = intent.getParcelableExtra("alarmToBroadcastReceiver");

            Intent openOnPopAlarm = new Intent(context, HoldFragmentsActivity.class);
            openOnPopAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openOnPopAlarm.putExtra("alarmToPopScreen", alarm);

            //If the alarm is recurring schedule the next day.
            if (alarm.isRecurring()) {
                alarm.schedule(context);
            } else {

                //Schedule the alarm to the next day
                alarm.removeDays();
                alarm.whenNoDay_WasChosen();

                //Update the alarm so that it will have the updated day
                DataBaseHelper.database.changeAlarmSettings(alarm.getId(), alarm);
            }

            //Make the alarm inactive
                DataBaseHelper.database.changeAlarmActiveState(false, alarm);


            context.startActivity(openOnPopAlarm);
        }


    }

}
