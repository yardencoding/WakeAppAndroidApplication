package com.example.wakeup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
            openOnPopAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openOnPopAlarm.putExtra("alarmToPopScreen", alarm);



            //If the alarm is recurring schedule the next day.
            if (alarm.isRecurring()) {
                alarm.schedule(context);
            } else {
                //delete the alarm if the alarm isn't recurring. Because the alarm is supposed to only trigger once.
                alarm.cancel(context); // To be able to create a new alarm with the same id as this alarm.
                DataBaseHelper.database.deleteAlarm(alarm);
            }


            context.startActivity(openOnPopAlarm);
        }


    }

}
