package com.example.wakeup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class StatusBarNotificationService extends Service {


    public static  boolean IS_STATUS_BAR_SERVICE_RUNNING = false;

    private  final int NOTIFICATION_ID = 112382;
    private Notification notification;
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent activityToStart = new Intent(this, MainScreen.class);

        pendingIntent = PendingIntent.getActivity(this, 0, activityToStart, PendingIntent.FLAG_IMMUTABLE);


        notification = new NotificationCompat.Builder(this, MainScreen.ALARM_RING_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        IS_STATUS_BAR_SERVICE_RUNNING = true;

        startForeground(NOTIFICATION_ID, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent activityToStart = new Intent(this, MainScreen.class);

        pendingIntent = PendingIntent.getActivity(this, 0, activityToStart, PendingIntent.FLAG_IMMUTABLE);


        notification = new NotificationCompat.Builder(this, MainScreen.ALARM_RING_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();


        IS_STATUS_BAR_SERVICE_RUNNING = true;

        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        IS_STATUS_BAR_SERVICE_RUNNING = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}