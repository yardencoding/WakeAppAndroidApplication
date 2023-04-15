package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class HoldFragmentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_fragments_activity);

        //To be able to display the screen over lock screen and wake up the device.
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    }


    //To disable the back button from working. Because I do not want to be able to go to the previous screen.
    @Override
    public void onBackPressed() {
    }

    //To notify the user what will happened if he closes the app.
    @Override
    protected void onPause() {
        Toast.makeText(this, "אל תסגור את האפליקציה! לא תוכל לכבות את ההתראה לאחר מכן", Toast.LENGTH_SHORT).show();
        super.onPause();
    }




    // To hide hardware volume buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
            return true; // Meaning I handled that event

        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
            return true;// Meaning I handled that event

        return true;
    }
}