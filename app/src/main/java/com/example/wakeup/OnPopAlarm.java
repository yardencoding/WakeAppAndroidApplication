package com.example.wakeup;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import java.time.LocalDateTime;
import java.util.List;


public class OnPopAlarm extends Fragment implements View.OnClickListener{

    private Button stopAlarmServiceBtn;
    private TextView cancel_alarm_textView, show_time_tv;

    private Handler timeHandler;


    private Alarm poppedAlarm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_on_pop_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stopAlarmServiceBtn = view.findViewById(R.id.stop_alarm_Service_btn);
        cancel_alarm_textView = view.findViewById(R.id.cancel_alarm_textView);
        show_time_tv = view.findViewById(R.id.show_time_tv);
        stopAlarmServiceBtn.setOnClickListener(this);

        poppedAlarm = getActivity().getIntent().getParcelableExtra("alarmToPopScreen");

       String name = poppedAlarm.getName();

        if(!name.isEmpty())
            cancel_alarm_textView.setText(name);

        if(poppedAlarm.hasMission())
           cancel_alarm_textView.setText("התחל משימה");

        timeHandler = new Handler();

        //start alarm service.
        Intent startAlarmService = new Intent(requireContext(), AlarmService.class);
        startAlarmService.putExtra("alarmToServiceFromPoppedScreen", poppedAlarm);
        getActivity().startForegroundService(startAlarmService);


   }



   private Runnable updateTimeTask = new Runnable() {
       @Override
       public void run() {
           show_time_tv.setText(getCurrentTime());
           // schedule the task to run again after 1 minute
           timeHandler.postDelayed(this, 60_000);
       }
   };


    private String getCurrentTime(){
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        return String.format("%02d:%02d", hour, minute);
   }


    @Override
    public void onStart() {
        super.onStart();
        timeHandler.post(updateTimeTask);
    }

    @Override
    public void onStop() {
        super.onStop();
        timeHandler.removeCallbacks(updateTimeTask);
    }


    @Override
    public void onClick(View view) {
        //Go to checkSmile fragment
        if (poppedAlarm.hasMission()){
            Navigation.findNavController(view).navigate(R.id.action_onPopAlarm_to_checkSmile);

        } else{
            //Stop alarm service
            getActivity().stopService(new Intent(getContext(), AlarmService.class));
            getActivity().finishAndRemoveTask();

        }
    }



}