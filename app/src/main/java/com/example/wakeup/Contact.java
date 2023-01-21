package com.example.wakeup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Contact extends AppCompatActivity implements View.OnClickListener {

    private EditText phoneNumber1, phoneNumber2, phoneNumber3;
    private EditText sendTextMessage;
    private ImageButton saveContactsImageButton;
    private int SEND_SMS_REQUEST_CODE = 1;

    //To store the phones and message in SharedPreferences(file in our device)
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String MESSAGE = "MESSAGE";
    private static final String PHONE_NUMBER_1 = "PHONE_NUMBER_1";
    private static final String PHONE_NUMBER_2 = "PHONE_NUMBER_2";
    private static final String PHONE_NUMBER_3 = "PHONE_NUMBER_3";

    private String loadedMessage, loadedPhoneNumber1, loadedPhoneNumber2, loadedPhoneNumber3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Initialize EditTexts
        phoneNumber1 = findViewById(R.id.editTextPhone1);
        phoneNumber2 = findViewById(R.id.editTextPhone2);
        phoneNumber3 = findViewById(R.id.editTextPhone3);
        sendTextMessage = findViewById(R.id.editTextMessage);

        saveContactsImageButton = findViewById(R.id.save_contactsImageButton);
        saveContactsImageButton.setOnClickListener(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_DENIED) {
            requestSendSmsPermission();
        }

        //Retrieve the message and numbers from shared preference
        loadDataFromSharedPref();
        sendTextMessage.setText(loadedMessage);
        phoneNumber1.setText(loadedPhoneNumber1);
        phoneNumber2.setText(loadedPhoneNumber2);
        phoneNumber3.setText(loadedPhoneNumber3);


    }



    private void sendSms() {


        String message = sendTextMessage.getText().toString();

        //Check if permission was granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED) {

            SmsManager sms = SmsManager.getDefault();

            //Sends to the first number isn't empty.
            if(phoneNumber1.getText().toString().isEmpty()==false) {
                sms.sendTextMessage(
                        phoneNumber1.getText().toString(),
                        null,
                        message,
                        null,
                        null);
            }

            //Sends to the second number isn't empty.
            if(phoneNumber2.getText().toString().isEmpty()==false) {
                sms.sendTextMessage(
                        phoneNumber2.getText().toString(),
                        null,
                        message,
                        null,
                        null);
            }

            //Sends to the third number isn't empty
            if((phoneNumber3.getText().toString().isEmpty()==false)) {
                sms.sendTextMessage(
                        phoneNumber3.getText().toString(),
                        null,
                        message,
                        null,
                        null);
            }
        }
        //Save the phone numbers and message to shared preference
        saveDataToSharedPref();
        //Returns to CreateAlarm activity.
        super.onBackPressed();
    }

    private void requestSendSmsPermission(){
        ActivityCompat.requestPermissions(Contact.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ההרשאה אושרה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "יש צורך בהרשאה לשליחת SMS כדי שההודעה תשלח" , Toast.LENGTH_SHORT).show();
            }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        @Override
        public void onClick (View v){
        //Save button

            //Exit if all the numbers are empty
            if(allNumbersAreEmpty()){
                Toast.makeText(this, "לא בחרת איש קשר" , Toast.LENGTH_SHORT).show();
                return;
            }

            if(checkIfAllNone_EmptyNumbersAreValid() == true){
                if(sendTextMessage.getText().toString().isEmpty() == false)
                  sendSms();
                else
                    Toast.makeText(this, "הודעה ריקה" , Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "מספר אחד או יותר לא תקינים" , Toast.LENGTH_SHORT).show();


        }


        private boolean allNumbersAreEmpty(){
        return phoneNumber1.getText().toString().isEmpty() &&
                phoneNumber2.getText().toString().isEmpty() && phoneNumber3.getText().toString().isEmpty();
        }

    private boolean checkIfAllNone_EmptyNumbersAreValid() {


        //Israeli number structure
        String regex = "^\\+?(972|0)(\\-)?0?(([23489]{1}\\d{7})|[5]{1}\\d{8})$";


        //To change the color of the EditText to red if the number isn't empty and isn't israeli.
        if(phoneNumber1.getText().toString().isEmpty() == false){

            if(phoneNumber1.getText().toString().matches(regex) == false) {
                return false;
            }
        }

        if(phoneNumber2.getText().toString().isEmpty() == false){

            if(phoneNumber2.getText().toString().matches(regex) == false) {
                return false;
            }
        }

        if(phoneNumber3.getText().toString().isEmpty() == false){

            if(phoneNumber3.getText().toString().matches(regex) == false) {
                return false;
            }
        }

        return true;

    }

    // save the numbers and the message so that it won't disappear when we reopen the app
    private void saveDataToSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MESSAGE, sendTextMessage.getText().toString());
        editor.putString(PHONE_NUMBER_1, phoneNumber1.getText().toString());
        editor.putString(PHONE_NUMBER_2, phoneNumber2.getText().toString());
        editor.putString(PHONE_NUMBER_3, phoneNumber3.getText().toString());
        editor.apply();
    }

    //To retrieve the message and phone numbers from shared preference.
    private void  loadDataFromSharedPref(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        loadedMessage = sharedPreferences.getString(MESSAGE, "");
        loadedPhoneNumber1 = sharedPreferences.getString(PHONE_NUMBER_1, "");
        loadedPhoneNumber2 = sharedPreferences.getString(PHONE_NUMBER_2, "");
        loadedPhoneNumber3 = sharedPreferences.getString(PHONE_NUMBER_3, "");

    }

    public void onBackIconContact(View view) {
        super.onBackPressed();
    }
}

