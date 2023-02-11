package com.example.wakeup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmService extends Service {

    private static final String CHANNEL_ID = "alarmNotificationId";
    private Alarm alarm;
    private MediaPlayer mediaPlayer;

    private Vibrator vibrator;

    private  Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        alarm = intent.getParcelableExtra("alarmToService");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification channel, needed for sdk 26 and above
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Alarm ring", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(),
                new Intent(this, HoldFragmentsActivity.class),
                PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("התראה..")
                .setContentText(alarm.getName())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setFullScreenIntent(pendingIntent,true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();


        //Play sound
        if(alarm.hasSound()) {
            try {
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://com.example.wakeup/" + ChooseSound.getSoundID(alarm.getSoundName())));
                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            mediaPlayer.setLooping(true);
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, alarm.getVolume(), 0);
            mediaPlayer.start();
        }

        //Vibrate
        if(alarm.hasVibrate()){
            long[] pattern = {0, 500, 1000};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1));
        }

        //Send sms
        if(alarm.hasUseMyContacts()){
            //Wait one minute and if the user does not wake up Send sms to his contacts
            Handler handler = new Handler();
           runnable =  new Runnable(){
                @Override
                public void run() {
                    Contact.sendSms(AlarmService.this);
                }
            };
           handler.postDelayed(runnable, 60_000);
        }

        startForeground(1, notification);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        if(alarm.hasSound()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if(alarm.hasVibrate()) {
            vibrator.cancel();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}