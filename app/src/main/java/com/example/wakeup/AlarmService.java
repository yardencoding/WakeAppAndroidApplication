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
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.IOException;

public class AlarmService extends Service {

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

        alarm = intent.getParcelableExtra("alarmToServiceFromPoppedScreen");

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(),
                new Intent(this, MainScreen.class),
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, MainScreen.ALARM_RING_CHANNEL_ID)
                .setContentTitle("התראה..")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
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

            //to set the volume to be 50% if the device max volume.
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int thirtyPercentVolume = (int)(0.5 * maxVolume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, thirtyPercentVolume, 0);


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