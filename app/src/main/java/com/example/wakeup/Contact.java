package com.example.wakeup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Contact extends AppCompatActivity implements View.OnClickListener {

    private static EditText phoneNumberOneEditText;
    private static EditText phoneNumberTwoEditText;
    private static EditText phoneNumberThreeEditText;
    private static EditText textMessageEditText;
    private ImageButton saveContactsImageButton;

    //To store the phones and message in SharedPreferences(file in our device)
    public static final String SHARED_PREFS = "CONTACT_SHARED_PREF";
    public static final String MESSAGE = "MESSAGE";
    public static final String PHONE_NUMBER_1 = "PHONE_NUMBER_1";
    public static final String PHONE_NUMBER_2 = "PHONE_NUMBER_2";
    public static final String PHONE_NUMBER_3 = "PHONE_NUMBER_3";

    private  String loadedMessage ="", loadedPhoneNumber1="", loadedPhoneNumber2="", loadedPhoneNumber3="";


    public  Contact (){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        //Initialize EditTexts
        phoneNumberOneEditText = findViewById(R.id.phone_number_one_edit_text);
        phoneNumberTwoEditText = findViewById(R.id.phone_number_two_edit_text);
        phoneNumberThreeEditText = findViewById(R.id.phone_number_three_edit_text);
        textMessageEditText = findViewById(R.id.text_message_edit_text);

        saveContactsImageButton = findViewById(R.id.save_contacts_Image_button);
        saveContactsImageButton.setOnClickListener(this);

        //Retrieve the message and numbers from shared preference
        loadDataFromSharedPref();
        textMessageEditText.setText(loadedMessage);
        phoneNumberOneEditText.setText(loadedPhoneNumber1);
        phoneNumberTwoEditText.setText(loadedPhoneNumber2);
        phoneNumberThreeEditText.setText(loadedPhoneNumber3);


    }


    public static void sendSms(Context context) {

        String message = textMessageEditText.getText().toString();

            SmsManager sms = SmsManager.getDefault();

            //Sends to the first number if isn't empty.
            if (phoneNumberOneEditText.getText().toString().isEmpty() == false) {
                sms.sendTextMessage(
                        phoneNumberOneEditText.getText().toString(),
                        null,
                        message,
                        null,
                        null);
            }

            //Sends to the second number if isn't empty.
            if (phoneNumberTwoEditText.getText().toString().isEmpty() == false) {
                sms.sendTextMessage(
                        phoneNumberTwoEditText.getText().toString(),
                        null,
                        message,
                        null,
                        null);
            }

            //Sends to the third number if isn't empty
            if ((phoneNumberThreeEditText.getText().toString().isEmpty() == false)) {
                sms.sendTextMessage(
                        phoneNumberThreeEditText.getText().toString(),
                        null,
                        message,
                        null,
                        null);
            }

    }


    @Override
    public void onClick(View v) {
        //Save button

        //Exit if all the numbers are empty
        if (allNumbersAreEmpty()) {
            Toast.makeText(this, "לא בחרת איש קשר", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkIfAllNone_EmptyNumbersAreValid() == true) {
            if (textMessageEditText.getText().toString().isEmpty() == false) {

                // To check if permission was granted
                if (ContextCompat.checkSelfPermission(Contact.this,
                        Manifest.permission.SEND_SMS) ==
                        PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(Contact.this, "אין הרשאה לשליחת הודעות SMS", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Save the phone numbers and message to shared preference
                saveDataToSharedPref();
                //Go to CreateAlarm activity
                super.onBackPressed();


            } else
                Toast.makeText(this, "הודעה ריקה", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "מספר אחד או יותר לא תקינים", Toast.LENGTH_SHORT).show();


    }


    private boolean allNumbersAreEmpty() {
        return phoneNumberOneEditText.getText().toString().isEmpty() &&
                phoneNumberTwoEditText.getText().toString().isEmpty() && phoneNumberThreeEditText.getText().toString().isEmpty();
    }

    private boolean checkIfAllNone_EmptyNumbersAreValid() {


        //Israeli number structure
        String regex = "^\\+?(972|0)(\\-)?0?(([23489]{1}\\d{7})|[5]{1}\\d{8})$";


        //To change the color of the EditText to red if the number isn't empty and isn't israeli.
        if (phoneNumberOneEditText.getText().toString().isEmpty() == false) {

            if (phoneNumberOneEditText.getText().toString().matches(regex) == false) {
                return false;
            }
        }

        if (phoneNumberTwoEditText.getText().toString().isEmpty() == false) {

            if (phoneNumberTwoEditText.getText().toString().matches(regex) == false) {
                return false;
            }
        }

        if (phoneNumberThreeEditText.getText().toString().isEmpty() == false) {

            if (phoneNumberThreeEditText.getText().toString().matches(regex) == false) {
                return false;
            }
        }

        return true;

    }

    // save the numbers and the message so that it won't disappear when we reopen the app
    private void saveDataToSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MESSAGE, textMessageEditText.getText().toString());
        editor.putString(PHONE_NUMBER_1, phoneNumberOneEditText.getText().toString());
        editor.putString(PHONE_NUMBER_2, phoneNumberTwoEditText.getText().toString());
        editor.putString(PHONE_NUMBER_3, phoneNumberThreeEditText.getText().toString());
        editor.apply();
    }

    //To retrieve the message and phone numbers from shared preference.
    private void loadDataFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        loadedMessage = sharedPreferences.getString(MESSAGE, "");
        loadedPhoneNumber1 = sharedPreferences.getString(PHONE_NUMBER_1, "");
        loadedPhoneNumber2 = sharedPreferences.getString(PHONE_NUMBER_2, "");
        loadedPhoneNumber3 = sharedPreferences.getString(PHONE_NUMBER_3, "");

    }

}

