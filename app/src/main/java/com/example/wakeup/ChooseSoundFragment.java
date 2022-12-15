package com.example.wakeup;

import static android.content.Context.AUDIO_SERVICE;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
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

    private int maxDeviceVolume, currentDeviceVolume;


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


        audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        maxDeviceVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentDeviceVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);



        //Slider
        soundSlider = view.findViewById(R.id.sound_slider);
        soundSlider.setValueFrom(0);
        soundSlider.setValueTo(100);
        //soundSlider.setValue(currentDeviceVolume);
        initializeSliderListeners();


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
        itemDecorationSpace.setDrawable(    ContextCompat.getDrawable(getContext(), R.drawable.divider_space_choose_sound)
        );
        //Add 7dp space between recyclerview items.
        recyclerView.addItemDecoration(itemDecorationSpace);

        //Initialize a MediaPlayer obj.
        mediaPlayer = new MediaPlayer();


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

    private void initializeSliderListeners(){


        soundSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

                // Responds when the slider is being start
                soundSlider.setTrackHeight(20);
            }

            int defaultTrackHeight = soundSlider.getTrackHeight();
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Responds when the slider is being stopped
                soundSlider.setTrackHeight(defaultTrackHeight);
            }
        });



        soundSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return (int) value +"";
            }
        });




        //When slider value changed
        soundSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float newValue, boolean fromUser) {

                if(mediaPlayer != null) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) newValue / (100 / maxDeviceVolume), 0);
                }

            }
        });
    }



    @Override
    public void onClick(View view) {

        if(view.getId() == play.getId() || view.getId() == pause.getId())
            if(clickedRadioButton == null){
                Toast.makeText(requireContext(), "לא נבחר צלצול", Toast.LENGTH_SHORT).show();
                return; //Exit. radio button is not checked.
            }


        //To check if the RadioButton is checked,and if not display a Toast message.
        if (view.getId() == play.getId()) {

                //If I change song name when the song is playing. Stop the previous song.
                if(mediaPlayer.isPlaying()) mediaPlayer.stop();

                //Initialize a MediaPlayer
                String key = clickedRadioButton.getText().toString();
                mediaPlayer = MediaPlayer.create(requireContext(), soundNameMap.get(key));
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }

        else if (view.getId() == pause.getId()) {
             if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();

        } else {
            //Volume button is clicked. Change slider value to the minimum value(5).
            soundSlider.setValue(5);
        }
    }


        @Override
        public void onItemClick ( int position){
            adapter.notifyItemChanged(adapter.copyLastCheckedPosition);
            adapter.notifyItemChanged(adapter.lastCheckedPosition);

            clickedRadioButton = radioButtonList.get(position);

        }

        //Don't need
        @Override
        public void onItemLongClick ( int position){
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
