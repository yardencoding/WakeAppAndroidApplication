package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Contact extends AppCompatActivity implements View.OnClickListener {

    private EditText phoneNumber1, phoneNumber2, phoneNumber3, phoneNumber4;
    private ImageButton saveContactsImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Initialize EditTexts
        phoneNumber1 = findViewById(R.id.editTextPhone1);
        phoneNumber2 = findViewById(R.id.editTextPhone2);
        phoneNumber3 = findViewById(R.id.editTextPhone3);
        phoneNumber4 = findViewById(R.id.editTextPhone4);

        saveContactsImageButton = findViewById(R.id.save_contactsImageButton);
        saveContactsImageButton.setOnClickListener(this);

    }

    private boolean isValidIsraelPhoneNumber() {
        //Checks if the phone is a valid israeli number.
        String invalidPhoneNumber = "";
        String regex = "^\\+?(972|0)(\\-)?0?(([23489]{1}\\d{7})|[5]{1}\\d{8})$";

        if (phoneNumber1.getText().toString().isEmpty() == false) {
            if (phoneNumber1.getText().toString().matches(regex) == false)
                invalidPhoneNumber += "אישר קשר 1 לא תקין";
        }

        if (phoneNumber2.getText().toString().isEmpty() == false) {
            if (phoneNumber2.getText().toString().matches(regex) == false)
                invalidPhoneNumber += " , אישר קשר 2 לא תקין";
        }

        if (phoneNumber3.getText().toString().isEmpty() == false) {
            if (phoneNumber3.getText().toString().matches(regex) == false)
                invalidPhoneNumber += " , אישר קשר 3 לא תקין";
        }

        if (phoneNumber4.getText().toString().isEmpty() == false) {
            if (phoneNumber4.getText().toString().matches(regex) == false)
                invalidPhoneNumber += " , אישר קשר 4 לא תקין";
        }

        if(invalidPhoneNumber.isEmpty())
            return true;
        else {
            Toast.makeText(this, invalidPhoneNumber, Toast.LENGTH_SHORT).show();
            return false;
        }

    }


    @Override
    public void onClick(View v) {
        if (isValidIsraelPhoneNumber())
            super.onBackPressed(); //Go back to CreateAlarm activity.
    }
}

