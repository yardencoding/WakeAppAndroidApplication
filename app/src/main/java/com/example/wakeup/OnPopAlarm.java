package com.example.wakeup;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class OnPopAlarm extends Fragment implements View.OnClickListener{

    private ConstraintLayout constraintLayout;
    private Button stopAlarmServiceBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_on_pop_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        constraintLayout = view.findViewById(R.id.cancelAlarmFragment);
        stopAlarmServiceBtn = view.findViewById(R.id.stop_alarm_Service_btn);
        stopAlarmServiceBtn.setOnClickListener(this);


        //Start background animation
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }


    @Override
    public void onClick(View v) {
        //Stop alarm service
        getActivity().stopService(new Intent(getContext(), AlarmService.class));
    }
}