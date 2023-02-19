package com.example.wakeup;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private Alarm alarm;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //reschedule alarms. Because AlarmManger loses all of his alarms after reboot
            for (Alarm alarm: DataBaseHelper.database.getAllAlarmsFromDataBase())
                if(alarm.isActive())
                    alarm.schedule(context);


            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    (int) System.currentTimeMillis(),
                    new Intent(context, MainScreen.class),
                    PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(context, MainScreen.STATUS_BAR_AND_RESCHEDULE_CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

    } else{
            //Start Alarm service
            alarm = intent.getParcelableExtra("alarmToBroadcastReceiver");
            Intent startAlarmService = new Intent(context, AlarmService.class);
            startAlarmService.putExtra("alarmToService", alarm);

            //If the alarm is recurring schedule the next day.
            if (alarm.isRecurring()) {
                alarm.schedule(context);
            } else {
                //stop the StatusBarNotification service if the alarm is not recurring because there are no more alarms.
              //  context.stopService(new Intent(context, StatusBarNotificationService.class));
            }

            //make alarm inactive when it pops

            int firingAlarmIndex = MainScreen.alarmList.indexOf(alarm);

            //change alarm active state to false, from alarmList because the recyclerView loads from alarmList.
            MainScreen.alarmList.get(firingAlarmIndex).setActive(false);

            //update alarm active state in sqlite database
            DataBaseHelper.database.changeAlarmActiveState(false, alarm);

            //update alarm adapter so that it call onBindViewHolder()
            MainScreen.adapter.notifyItemChanged(firingAlarmIndex);

            context.startForegroundService(startAlarmService);
        }
    }

}
