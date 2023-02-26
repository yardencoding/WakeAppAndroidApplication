package com.example.wakeup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

/*
The purpose of this class is to show a consistent status bar notification, so that the user
will know he has an alarm. And also to keep some part of the app constantly running so that BroadcastReceiver will get notified
even when the app is closed.
 */

public class StatusBarNotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //To open the app when I click the notification
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(),
                new Intent(this, MainScreen.class),
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, MainScreen.ALARM_RING_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(2, notification);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}