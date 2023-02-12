package com.example.wakeup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    private  Alarm alarm;
    @Override
    public void onReceive(Context context, Intent intent) {

        //When the device shuts down all the alarms scheduled by AlarmManger will be lost, so we need to reschedule them
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Intent RescheduleIntentService= new Intent(context, RescheduleAlarmsService.class);
            //context.startForegroundService(RescheduleIntentService);


        } else{

            //Start Alarm service
             alarm = intent.getParcelableExtra("alarmToBroadcastReceiver");
            Intent StartIntentIntent = new Intent(context, AlarmService.class);
            StartIntentIntent.putExtra("alarmToService", alarm);

            //If the alarm is recurring schedule the next day.
            if(alarm.isRecurring()) {
                alarm.schedule(context);
            }

            //make alarm inactive when it pops

            int firingAlarmIndex = MainScreen.alarmList.indexOf(alarm);

            //change alarm active state to false, from alarmList because the recyclerView loads from alarmList.
            MainScreen.alarmList.get(firingAlarmIndex).setActive(false);

            //update alarm active state in sqlite database
            DataBaseHelper.database.changeAlarmActiveState(false, alarm);

            //update alarm adapter so that it call onBindViewHolder()
            MainScreen.adapter.notifyItemChanged(firingAlarmIndex);

            context.startForegroundService(StartIntentIntent);
        }

    }


}
