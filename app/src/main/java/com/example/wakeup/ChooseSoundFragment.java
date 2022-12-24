package com.example.wakeup;

import static android.content.Context.AUDIO_SERVICE;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChooseSoundFragment extends Fragment implements View.OnClickListener, RecyclerViewInterface {

    private ImageButton play, pause, volume;
    private Slider soundSlider;
    private RecyclerView recyclerView;
    private RadioButton clickedRadioButton;
    private LinkedHashMap<String, Integer> soundNameMap;
    private ArrayList<RadioButton> radioButtonList;
    private ChooseSoundAdapter adapter;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private int maxAlarmStreamVolume;
    private boolean isMediaPlayerPaused;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_sound, container, false);
        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //ImageButtons
        play = view.findViewById(R.id.play_audio_button);
        pause = view.findViewById(R.id.pause_audio_button);
        volume = view.findViewById(R.id.volume_image_button);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        volume.setOnClickListener(this);


        //Slider
        soundSlider = view.findViewById(R.id.sound_slider);
        soundSlider.setValue(60);
        initializeSliderListeners();

        //AudioManager
        audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        maxAlarmStreamVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 60 / (100 / maxAlarmStreamVolume), 0);


        //Create a map with the corresponding song and song name
        soundNameMap = createSoundNameMap();
        radioButtonList = getRadioButtonList();


        //Recyclerview
        recyclerView = view.findViewById(R.id.sound_names_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChooseSoundAdapter(radioButtonList, this);
        recyclerView.setAdapter(adapter);

        //Add space to recyclerview items.
        DividerItemDecoration itemDecorationSpace = new DividerItemDecoration(recyclerView.getContext()
                , DividerItemDecoration.VERTICAL);
        itemDecorationSpace.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider_space_choose_sound)
        );
        //Add 7dp space between recyclerview items.
        recyclerView.addItemDecoration(itemDecorationSpace);

        //Initialize a MediaPlayer obj with audio attributes.
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());

    }


    private LinkedHashMap<String, Integer> createSoundNameMap() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("Door knock", R.raw.door_knock);
        map.put("Heaven", R.raw.heaven);
        map.put("Homecoming", R.raw.homecoming);
        map.put("Inspiration", R.raw.inspiration);
        map.put("Israel siren", R.raw.israel_siren);
        map.put("Kokuriko", R.raw.kokuriko);
        map.put("Landscape", R.raw.land_scape);
        map.put("Minion wake up", R.raw.minion_wakeup);
        map.put("Piano", R.raw.piano);
        map.put("Powerful", R.raw.powerful);
        map.put("Sad trombone", R.raw.sad_trombone_);
        map.put("Scary", R.raw.scary);
        map.put("Super spiffy", R.raw.super_spiffy);
        map.put("The pirats", R.raw.the_pirats);
        return map;
    }

    private ArrayList<RadioButton> getRadioButtonList() {

        ArrayList<RadioButton> list = new ArrayList<>();

        //Iterate over soundNameMap to create a new RadioButton with the corresponding sound name.
        for (int i = 0; i < soundNameMap.size(); i++) {

            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setText(soundNameMap.keySet().toArray()[i].toString());
            list.add(radioButton);
        }

        return list;
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

                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (int) newValue / (100 / maxAlarmStreamVolume), 0);
            }
        });
    }


    @Override
    public void onClick(View view) {

        if (view.getId() == play.getId() || view.getId() == pause.getId())
            if (clickedRadioButton == null) {
                Toast.makeText(requireContext(), "לא נבחר צלצול", Toast.LENGTH_SHORT).show();
                return; //Exit. radio button is not checked.
            }


        //Clicked play button
        if (view.getId() == play.getId()) {

            //If I play the song after pausing it.
            if (isMediaPlayerPaused == true) {
                mediaPlayer.start();
                isMediaPlayerPaused = false;
                return;
            }

            //If I change the song name when the song is playing, Stop the previous song.
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            String key = clickedRadioButton.getText().toString();
            try {
                //Set mediaPlayer sound
                mediaPlayer.setDataSource(requireContext(), Uri.parse("android.resource://com.example.wakeup/" + soundNameMap.get(key)));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.start();


            //Pause button is clicked
        } else if (view.getId() == pause.getId()) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isMediaPlayerPaused = true;
            }

            //Volume button is clicked.
        } else {
            // Change slider value to the minimum device volume(6).
            soundSlider.setValue(6);
        }
    }


    @Override
    public void onItemClick(int position) {
        adapter.notifyItemChanged(adapter.copyLastCheckedPosition);
        adapter.notifyItemChanged(adapter.lastCheckedPosition);

        clickedRadioButton = radioButtonList.get(position);

    }

    //Don't need
    @Override
    public void onItemLongClick(int position) {
    }




    //Recyclerview Adapter + ViewHolder
    private class ChooseSoundAdapter extends RecyclerView.Adapter<ChooseSoundAdapter.ChooseSoundViewHolder> {

        private RecyclerViewInterface recyclerViewInterface;
        private ArrayList<RadioButton> radioButtonList;
        private int lastCheckedPosition = -1;
        private int copyLastCheckedPosition;


        public ChooseSoundAdapter(ArrayList<RadioButton> radioButtonList, RecyclerViewInterface recyclerViewInterface) {
            this.radioButtonList = radioButtonList;
            this.recyclerViewInterface = recyclerViewInterface;
        }


        class ChooseSoundViewHolder extends RecyclerView.ViewHolder {
            RadioButton radioButton;

            public ChooseSoundViewHolder(View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.choose_sound_radioButton);

                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        if (recyclerViewInterface != null && position != RecyclerView.NO_POSITION) {

                            copyLastCheckedPosition = lastCheckedPosition;
                            lastCheckedPosition = position;

                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                });

            }
        }

        @NonNull
        @Override
        public ChooseSoundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.choose_sound_reyclerview_item, parent, false);
            return new ChooseSoundViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChooseSoundViewHolder holder, int position) {
            RadioButton currentRadioButton = radioButtonList.get(position);
            holder.radioButton.setText(currentRadioButton.getText());
            holder.radioButton.setChecked(position == lastCheckedPosition);
        }

        @Override
        public int getItemCount() {
            return radioButtonList.size();
        }


    }


    @Override
    public void onDestroy() {
        mediaPlayer.release();
        super.onDestroy();
    }
}
