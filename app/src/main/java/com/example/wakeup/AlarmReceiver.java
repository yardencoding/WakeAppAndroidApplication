package com.example.wakeup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalDateTime;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarmNotificationId";

    @Override
    public void onReceive(Context context, Intent intent) {

        createNotification(context);
    }


    public  void createNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification channel
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Alarm ring", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        //Notification body
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("התראה");
        builder.setContentText(getCurrentHourAndMinute());
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);

        Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

    private String getCurrentHourAndMinute(){
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        return String.format("%02d:%02d", hour, minute);
    }

}
