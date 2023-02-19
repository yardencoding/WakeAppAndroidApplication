package com.example.wakeup;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDateTime;


public class OnPopAlarm extends Fragment implements View.OnClickListener{

    private Button stopAlarmServiceBtn;
    private TextView cancel_alarm_textView, show_time_tv;
    private Thread showTimeThread;
    private boolean hasMission;

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
        String name = getActivity().getIntent().getStringExtra("alarmNameFromService");
        hasMission = getActivity().getIntent().getBooleanExtra("hasMissionFromService", false);
        if(!name.isEmpty())
        cancel_alarm_textView.setText(name);
        updateTimeConsistently();
   }

    public void updateTimeConsistently() {
        showTimeThread = new Thread() {
            public void run() {
                while (Thread.currentThread().isInterrupted() == false) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            show_time_tv.setText(getCurrentTime());
                        }
                    });

                    try {
                        Thread.sleep(60_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        showTimeThread.start();
    }

   private String getCurrentTime(){
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        return String.format("%02d:%02d", hour, minute);
   }

    @Override
    public void onDestroy() {
        showTimeThread.interrupt();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        //Stop alarm service
        getActivity().stopService(new Intent(getContext(), AlarmService.class));

        //Go to checkSmile fragment
        if (hasMission){
        }
    }
}