package com.example.wakeup;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MazeGame extends Fragment implements View.OnClickListener{

    private ImageButton up_btn, down_btn, right_btn, left_btn;
    private MazeView mazeView;
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maze_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mazeView = view.findViewById(R.id.mazeView);

        up_btn = view.findViewById(R.id.up_btn);
        down_btn = view.findViewById(R.id.down_btn);
        right_btn = view.findViewById(R.id.right_btn);
        left_btn = view.findViewById(R.id.left_btn);

        up_btn.setOnClickListener(this);
        down_btn.setOnClickListener(this);
        right_btn.setOnClickListener(this);
        left_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View clickedButton) {
        switch (clickedButton.getId()){
            case R.id.up_btn:
                mazeView.moveUp();
                break;
            case R.id.down_btn:
                mazeView.moveDown();

                break;

            case R.id.left_btn:
                mazeView.moveLeft();

                break;

            case R.id.right_btn:
                mazeView.moveRight();
                break;
        }

        //Exit the app. if the player has reached the exist
        if(mazeView.hasReachedExist()) {
            //stop  alarm service.
            getActivity().stopService(new Intent(getContext(), AlarmService.class));

            Toast.makeText(getContext(), "כל הכבוד! פתרת את המבוך", Toast.LENGTH_LONG).show();
            getActivity().finishAndRemoveTask();


        }



    }
}
