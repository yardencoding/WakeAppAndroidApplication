package com.example.wakeup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Contact extends AppCompatActivity implements View.OnClickListener {

    private EditText phoneNumber1, phoneNumber2, phoneNumber3, phoneNumber4;
    private EditText sendTextMessage;
    private ImageButton saveContactsImageButton;
    private int SEND_SMS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Initialize EditTexts
        phoneNumber1 = findViewById(R.id.editTextPhone1);
        phoneNumber2 = findViewById(R.id.editTextPhone2);
        phoneNumber3 = findViewById(R.id.editTextPhone3);
        phoneNumber4 = findViewById(R.id.editTextPhone4);
        sendTextMessage = findViewById(R.id.editTextMessage);

        saveContactsImageButton = findViewById(R.id.save_contactsImageButton);
        saveContactsImageButton.setOnClickListener(this);

        requestSendSmsPermission();

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

        if (invalidPhoneNumber.isEmpty())
            return true;
        else {
            Toast.makeText(this, invalidPhoneNumber, Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private void sendSms() {
        String message = sendTextMessage.getText().toString();

        //Check if permission was granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED) {

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(
                    phoneNumber1.getText().toString(),
                    null,
                    message,
                    null,
                    null);

        } else {
            requestSendSmsPermission();
        }

    }

    private void requestSendSmsPermission() {
        //Check if we have showed the reason for this permission if we already did we request the permission.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Permission needed");
            dialog.setMessage("יש צורך בהרשאה זו על מנת לשלוח הודעות SMS");
            dialog.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(Contact.this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
                }
            });
            dialog.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(Contact.this, "לא תוכל לשלוח הודעות SMS", Toast.LENGTH_SHORT).show();
                    Contact.super.onBackPressed();
                }
            });
            dialog.create();
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(Contact.this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SEND_SMS_REQUEST_CODE)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ההרשאה אושרה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "לא תוכל לשלוח הודעות SMS", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        @Override
        public void onClick (View v){
            if (isValidIsraelPhoneNumber()) {
                sendSms();
            }

            super.onBackPressed();
        }
    }

