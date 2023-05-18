package com.example.wakeup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.IOException;

public class AlarmService extends Service {

    private Alarm alarm;
    public static MediaPlayer mediaPlayer;

    public static Vibrator vibrator;

    public static boolean isRunning;

    //Used to save alarm settings when we reopen the activity.
    public static Alarm alarmToReOpen;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());

        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        alarm = intent.getParcelableExtra("alarmToServiceFromPoppedScreen");

        alarmToReOpen = alarm;

        Notification notification = new NotificationCompat.Builder(this, MainScreen.ALARM_RING_CHANNEL_ID)
                .setContentTitle("התראה..")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setSmallIcon(R.drawable.notification_icon)
                .build();

        //Play sound
        if(alarm.hasSound()) {

            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://com.example.wakeup/" + ChooseSound.getSoundID(alarm.getSoundName())));
                mediaPlayer.prepare();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //to set the volume to be 60% if the device max volume.
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int sixtyPercentVolume = (int)(0.6 * maxVolume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sixtyPercentVolume, 0);

            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        //Vibrate
        if(alarm.hasVibrate()){
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {0, 500, 1000};
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1));
        }

        //Send sms
        if(alarm.hasUseMyContacts()){
            Contact.sendSms(this);
        }

        isRunning = true;

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

        isRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}