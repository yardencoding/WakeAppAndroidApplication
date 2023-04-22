package com.example.wakeup;


import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ChooseSound extends AppCompatActivity implements View.OnClickListener {

    private ImageButton playAudioButton, saveSoundImageButton;
    private RadioGroup soundNamesRadioGroup;

    private RadioButton clickedRadioButton;
    private MediaPlayer mediaPlayer;

    //To store the sound name in SharedPreferences

    public static final String SHARED_PREFS = "CHOOSE_SOUND_SHARED_PREF";
    public static final String SOUND_NAME = "SOUND_NAME";

    private String currentSoundName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_sound);

        //RadioGroup
        soundNamesRadioGroup = findViewById(R.id.sound_names_radio_group);
        //Get the id of the clicked radioButton
        soundNamesRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                clickedRadioButton = findViewById(checkedId);
                mediaPlayer.reset();
            }
        });

        //Initialize a MediaPlayer obj with audio attributes.
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());
        mediaPlayer.setLooping(true);


        //ImageButtons
        playAudioButton = findViewById(R.id.play_audio_button);
        saveSoundImageButton = findViewById(R.id.save_sound_image_button);

        playAudioButton.setOnClickListener(this);
        saveSoundImageButton.setOnClickListener(this);


        loadSoundName();

        searchForRadioButton_WithSameSoundName();
    }


    private void searchForRadioButton_WithSameSoundName() {
        /*
        Searching through RadioGroup to find the RadioButton with the same sound name
        as CreateAlarm sound name and set him to checked.
         */
        for (int i = 0; i < soundNamesRadioGroup.getChildCount(); i++) {
            clickedRadioButton = (RadioButton) soundNamesRadioGroup.getChildAt(i);
            if (clickedRadioButton.getText().toString().equals(currentSoundName)) {
                clickedRadioButton.setChecked(true);
                break;
            }
        }


    }

    public static int getSoundID(String soundName) {
        switch (soundName) {
            case "Door knock":
                return R.raw.door_knock;
            case "Heaven":
                return R.raw.heaven;
            case "Homecoming":
                return R.raw.homecoming;
            case "Kokuriko":
                return R.raw.kokuriko;
            case "Landscape":
                return R.raw.land_scape;
            case "Minion wake up":
                return R.raw.minion_wakeup;
            case "Piano":
                return R.raw.piano;
            case "Powerful":
                return R.raw.powerful;
            case "Scary":
                return R.raw.scary;
            case "Super spiffy":
                return R.raw.super_spiffy;
            default: //Invalid sound name
                return -1;
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.play_audio_button:
                playAudio();
                break;

            case R.id.save_sound_image_button:
                saveSoundName();
                super.onBackPressed();
                break;

        }
    }


    private void playAudio() {

        try {
            //Set mediaPlayer sound
            if (mediaPlayer.isPlaying())
                mediaPlayer.reset();
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://com.example.wakeup/" + getSoundID(clickedRadioButton.getText().toString())));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //to set the volume to be 60% if the device max volume.
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int sixtyPercentVolume = (int) (0.6 * maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sixtyPercentVolume, 0);


        mediaPlayer.setLooping(true);
        mediaPlayer.start();

    }


    //sharedPreferences setup
    private void saveSoundName() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SOUND_NAME, (String) clickedRadioButton.getText());
        editor.apply();
    }

    private void loadSoundName() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        currentSoundName = sharedPreferences.getString(SOUND_NAME, "Homecoming");
    }


    // To hide hardware volume buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {


        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            return true; // Meaning I handled that event
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;// Meaning I handled that event
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void onDestroy() {
        mediaPlayer.release();
        super.onDestroy();
    }


}
