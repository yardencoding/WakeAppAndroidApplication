package com.example.wakeup;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.HashMap;

public class ChooseSound extends AppCompatActivity implements View.OnClickListener {

    private ImageButton play, volume, saveSound;
    private Slider soundSlider;
    private RadioGroup radioGroup;

    private RadioButton clickedRadioButton;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int maxAlarmStreamVolume;

    //To store the sound name in SharedPreferences

    public static final String SHARED_PREFS = "CHOOSE_SOUND_SHARED_PREF";
    public static final String SOUND_NAME = "SOUND_NAME";
    public static final String SOUND_VOLUME = "SOUND_VOlUME";

    private String currentSoundName;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_sound);

        //RadioGroup
        radioGroup = findViewById(R.id.sound_names_radioGroup);
        //Get the id of the clicked radioButton
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    clickedRadioButton = findViewById(checkedId);
                    mediaPlayer.reset();
            }
        });


        //ImageButtons
        play = findViewById(R.id.play_audio_button);
        volume = findViewById(R.id.volume_image_button);
        saveSound = findViewById(R.id.save_sound_image_button);

        play.setOnClickListener(this);
        volume.setOnClickListener(this);
        saveSound.setOnClickListener(this);


        //Slider
        soundSlider = findViewById(R.id.sound_slider);
        soundSlider.setValue(getIntent().getIntExtra("soundVolume_fromCreateAlarm", 60));
        initializeSliderListeners();

        //AudioManager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxAlarmStreamVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (int)soundSlider.getValue() / (100 / maxAlarmStreamVolume), 0);


        //Initialize a MediaPlayer obj with audio attributes.
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());
        mediaPlayer.setLooping(true);


        loadDataFromSharedPreferences();

        searchForRadioButton_WithSameSoundName();
    }


    private void searchForRadioButton_WithSameSoundName() {
        /*
        Searching through RadioGroup to find the RadioButton with the same sound name
        as CreateAlarm sound name and set him to checked.
         */
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
             clickedRadioButton = (RadioButton) radioGroup.getChildAt(i);
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


    private void initializeSliderListeners() {


        soundSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

                // Responds when the slider is being start
                soundSlider.setTrackHeight(20);
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Responds when the slider is being stopped
                soundSlider.setTrackHeight(12);
            }
        });


        //Cast slider label values from decimals to (int).
        soundSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "%";
            }
        });


        //When slider value changed
        soundSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float newValue, boolean fromUser) {
                // checks if the newValue is smaller than the minimum stream volume.
                if (newValue < 6) {
                    newValue = 6;
                    soundSlider.setValue(newValue);
                }
                // checks if the newValue is greater than the maximum slider value.
                if (newValue > 100) {
                    newValue = 100;
                    soundSlider.setValue(newValue);
                }


                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (int) newValue / (100 / maxAlarmStreamVolume), 0);
            }
        });
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.play_audio_button:
                playAudio();
                break;

            case R.id.volume_image_button:
                soundSlider.setValue(6);
                break;
            case R.id.save_sound_image_button:
                saveSoundNameAndVolume();
                super.onBackPressed();
                break;

        }
    }


    private void playAudio(){
        try {
            //Set mediaPlayer sound
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://com.example.wakeup/" + getSoundID(clickedRadioButton.getText().toString())));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

    }



    //sharedPreferences setup
    private void saveSoundNameAndVolume() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SOUND_NAME, (String) clickedRadioButton.getText());
        editor.putInt(SOUND_VOLUME, (int) soundSlider.getValue());
        editor.apply();
    }

    private void loadDataFromSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        soundSlider.setValue(sharedPreferences.getInt(SOUND_VOLUME, 60));
        currentSoundName = sharedPreferences.getString(SOUND_NAME, "Homecoming");
    }


    // To hide hardware volume buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int steps = 5;
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            soundSlider.setValue(soundSlider.getValue() + steps);
            return true; // Meaning I handled that event
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            soundSlider.setValue(soundSlider.getValue() - steps);
            return true;// Meaning I handled that event
        }

        return super.dispatchKeyEvent(event);
    }

    //If back button was pressed update soundId in database.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        mediaPlayer.release();
        super.onDestroy();
    }


    public void onBackIconChooseSound(View view) {
        super.onBackPressed();
    }
}
