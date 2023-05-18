package com.example.wakeup;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.audiofx.NoiseSuppressor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetectWater extends Fragment implements View.OnClickListener {

    private Button recordAudioButton;
    private boolean isRecording;

    private final String tensorFlowModelPath = "lite-model_yamnet_classification_tflite_1.tflite";
    private final float MIN_PROBABILITY = 0.02f;

    private AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;

    private AudioRecord record;

    private boolean detectedWater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detect_water, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recordAudioButton = view.findViewById(R.id.record_audio_button);
        recordAudioButton.setOnClickListener(this);

        try {
            audioClassifier = AudioClassifier.createFromFile(getContext(), tensorFlowModelPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tensorAudio = audioClassifier.createInputTensorAudio();


    }


    private void stopRecording() {

        //Resume sound if it didn't detect water
        if(AlarmService.mediaPlayer != null && !detectedWater) {
            AlarmService.mediaPlayer.start();
        }

        //Resume vibration if it didn't detect water
        if(AlarmService.vibrator != null && !detectedWater){
            long[] pattern = {0, 500, 1000};
            AlarmService.vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1));
        }

        record.stop();
        isRecording = false;
        recordAudioButton.setText("הקלט");

        classifyModel();
    }


    private void startRecording() {

        //Stop the sound when recording.
        if(AlarmService.mediaPlayer != null) {
            AlarmService.mediaPlayer.pause();
        }

        //Stop the vibration when recording.
        if(AlarmService.vibrator != null){
            AlarmService.vibrator.cancel();
        }

        //Initialize the media recorder
        record = audioClassifier.createAudioRecord();

        record.startRecording();
        isRecording = true;

        recordAudioButton.setText("עצור הקלטה");

    }


    private void classifyModel() {

        tensorAudio.load(record);
        List<Classifications> output = audioClassifier.classify(tensorAudio);

        for (Classifications classifications : output) {
            for (Category category : classifications.getCategories()) {
                if (category.getLabel().equals("Water")) {
                    if (category.getScore() > MIN_PROBABILITY)
                        detectedWater = true;
                    break;
                }
            }
        }

        if (detectedWater) {
            //Stop alarm service
            getActivity().stopService(new Intent(getContext(), AlarmService.class));

            //Go back to the Main Screen.
            startActivity(new Intent(getContext(), MainScreen.class));

            record.release();

        } else {
            Toast.makeText(getContext(), "לא זיהינו צליל של מים, נסה שוב עם עוצמה חזקה יותר.", Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    public void onClick(View v) {
        if (isRecording)
            stopRecording();
        else
            startRecording();
    }




}