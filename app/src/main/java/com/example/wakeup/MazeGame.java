package com.example.wakeup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class MazeGame extends Fragment implements View.OnClickListener{

    private ImageButton moveUpButton, moveDownButton, moveRightButton, moveLeftButton;
    private MazeView mazeView;
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maze_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mazeView = view.findViewById(R.id.maze_view);

        moveUpButton = view.findViewById(R.id.move_up_button);
        moveDownButton = view.findViewById(R.id.move_down_button);
        moveRightButton = view.findViewById(R.id.move_right_button);
        moveLeftButton = view.findViewById(R.id.move_left_button);

        moveUpButton.setOnClickListener(this);
        moveDownButton.setOnClickListener(this);
        moveRightButton.setOnClickListener(this);
        moveLeftButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View clickedButton) {
        switch (clickedButton.getId()){
            case R.id.move_up_button:
                mazeView.moveUp();
                break;
            case R.id.move_down_button:
                mazeView.moveDown();
                break;

            case R.id.move_left_button:
                mazeView.moveLeft();
                break;

            case R.id.move_right_button:
                mazeView.moveRight();
                break;
        }

        //Exit the app. if the player has reached the exist
        if(mazeView.hasReachedExist()) {
            //stop  alarm service.
            getActivity().stopService(new Intent(getContext(), AlarmService.class));
            //Go back to the Main Screen.
            startActivity(new Intent(getContext(), MainScreen.class));

        }

    }
}
