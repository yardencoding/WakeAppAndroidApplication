package com.example.wakeup;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AlarmReceiver extends BroadcastReceiver {

    private Alarm alarm;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //reschedule alarms. Because AlarmManger loses all of his alarms after reboot
            for (Alarm alarm: DataBaseHelper.database.getAllAlarmsFromDataBase())
                if(alarm.isActive())
                    alarm.schedule(context);


    } else{
            //Start Alarm service
            alarm = intent.getParcelableExtra("alarmToBroadcastReceiver");
           // Intent startAlarmService = new Intent(context, AlarmService.class);
            Intent openOnPopAlarm = new Intent(context, HoldFragmentsActivity.class);
            //To create a new stack and remove all the previous activities from the stack.
            openOnPopAlarm.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
            openOnPopAlarm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            openOnPopAlarm.putExtra("alarmToPopScreen", alarm);


            //If the alarm is recurring schedule the next day.
            if (alarm.isRecurring()) {
                alarm.schedule(context);
            } else {
                //stop the StatusBarNotification service if the alarm is not recurring because there are no more alarms.
               context.stopService(new Intent(context, StatusBarNotificationService.class));
            }

            //make alarm inactive when it pops

            int firingAlarmIndex = MainScreen.alarmList.indexOf(alarm);

            //change alarm active state to false, from alarmList because the recyclerView loads from alarmList.
            MainScreen.alarmList.get(firingAlarmIndex).setActive(false);

            //update alarm active state in sqlite database
            DataBaseHelper.database.changeAlarmActiveState(false, alarm);

            //update alarm adapter so that it call onBindViewHolder()
            MainScreen.adapter.notifyItemChanged(firingAlarmIndex);

            context.startActivity(openOnPopAlarm);
        }
    }

}
