package com.example.wakeup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "alarmNotificationId";
    private Alarm alarm;
    @Override
    public void onReceive(Context context, Intent intent) {


        alarm = intent.getParcelableExtra("runningAlarm");
        createNotification(context, intent.getStringExtra(alarm.getName()));

        playSound(intent.getIntExtra("soundId", 0), context);



    }


    public  void createNotification(Context context, String title) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification channel, needed for sdk 26 and above
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Alarm ring", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

        //Notification body
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("התראה..");
        builder.setContentText(title);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), new Intent(context, HoldFragmentsActivity.class), PendingIntent.FLAG_IMMUTABLE);
        builder.setFullScreenIntent(pendingIntent, true);

        builder.setCategory(NotificationCompat.CATEGORY_CALL); //To prevent the notification from disappearing after a few seconds


        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

    }

    private void playSound(int soundId, Context context){
       MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());

        try {
            //Set mediaPlayer sound
            mediaPlayer.setDataSource(context, Uri.parse("android.resource://com.example.wakeup/" + soundId));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }


}
