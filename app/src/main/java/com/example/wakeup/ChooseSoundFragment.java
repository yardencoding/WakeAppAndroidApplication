package com.example.wakeup;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.HashMap;

public class ChooseSoundFragment extends Fragment implements View.OnClickListener {

   private ImageButton play, pause, volume;
   private Slider soundSlider;
   private ListView listView;
   private RadioButton clickedRadioButton;
   private HashMap<String, Integer> soundNameMap;



    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
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

         soundNameMap = createSoundNameMap();

        //Listview Initialization + Adapter
        listView = view.findViewById(R.id.sound_names_listView);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                requireContext(),
                R.layout.choose_sound_list_view_item,
                R.id.choose_sound_radioButton,
                new ArrayList<>(soundNameMap.keySet())
        );
        listView.setAdapter(arrayAdapter);

        //Get the radio button that was clicked
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent , View view, int position, long id) {
            }
        });

    }

    private HashMap<String, Integer> createSoundNameMap(){
        HashMap<String, Integer> map = new HashMap<>();
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

    @Override
    public void onClick(View view) {

        MediaPlayer mediaPlayer = null;

        //To check if the RadioButton is checked,and if not display a Toast message.
        if(view.getId() == play.getId() || view.getId() == pause.getId())
            if(clickedRadioButton == null){
                Toast.makeText(requireContext(), "לא נבחר צלצול", Toast.LENGTH_SHORT).show();
                return; //Exit. radio button is not checked or null.

            } else{
                //Initialize a MediaPlayer
                String key = clickedRadioButton.getText().toString();
                mediaPlayer = MediaPlayer.create(requireContext(), soundNameMap.get(key));
            }

        if(view.getId() == play.getId()){
                mediaPlayer.setLooping(true);
                mediaPlayer.start();

        } else if(view.getId() == pause.getId()){
            if(mediaPlayer.isPlaying())
                mediaPlayer.pause();
        }
    }

}
