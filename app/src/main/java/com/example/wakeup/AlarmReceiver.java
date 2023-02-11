package com.example.wakeup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //When the device shuts down all the alarms scheduled by AlarmManger will be lost, so we need to reschedule them
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Intent RescheduleIntentService= new Intent(context, RescheduleAlarmsService.class);
            //context.startForegroundService(RescheduleIntentService);


        } else{

            //Start Alarm service
            Alarm alarm = intent.getParcelableExtra("alarmToBroadcastReceiver");
            Intent StartIntentIntent = new Intent(context, AlarmService.class);
            StartIntentIntent.putExtra("alarmToService", alarm);

            //If the alarm is recurring schedule the next day.
            if(alarm.isRecurring()) {
                alarm.schedule(context);
            }
            context.startForegroundService(StartIntentIntent);
        }

    }

    private void disableAlarm(){
        //make the alarm inactive after it pops

    }


}
