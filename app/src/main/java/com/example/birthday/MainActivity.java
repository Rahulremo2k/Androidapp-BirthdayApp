package com.example.birthday;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements android.view.View.OnClickListener {
    EditText Dob, Name, Phone;
    Button Insert, ViewAll, ViewToday, Sms, ViewTomorrow;
    SQLiteDatabase db;
    Calendar cal = Calendar.getInstance();
    String dayToday;
    String dayTomo;
    String monthToday;
    String monthTomo;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkForSmsPermission();
        Name = (EditText) findViewById(R.id.Name);
        Dob = (EditText) findViewById(R.id.Dob);
        Phone = (EditText) findViewById(R.id.Phone);
        Insert = (Button) findViewById(R.id.Insert);
        ViewAll = (Button) findViewById(R.id.ViewAll);
        ViewToday = (Button) findViewById(R.id.ViewToday);
        ViewTomorrow = (Button) findViewById(R.id.ViewTomorrow);
        Sms = (Button) findViewById(R.id.Sms);
        Insert.setOnClickListener(this);
        ViewAll.setOnClickListener(this);
        ViewToday.setOnClickListener(this);
        ViewTomorrow.setOnClickListener(this);
        Sms.setOnClickListener(this);
        // Creating database and table
        db = openOrCreateDatabase("BirthdayDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS birthday(name VARCHAR,dob VARCHAR,phone VARCHAR)");
        Date today = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd");
        DateFormat monthFormat = new SimpleDateFormat("MM");
        dayToday = dateFormat.format(today);
        monthToday = monthFormat.format(today);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = cal.getTime();
        dayTomo = dateFormat.format(tomorrow);
        monthTomo = monthFormat.format(tomorrow);
    }

    public void onClick(View view) {
        // Inserting a record to the Student table
        if (view == Insert) {
            // Checking for empty fields
            if (Name.getText().toString().trim().length() == 0 ||
                    Dob.getText().toString().trim().length() == 0 ||
                    Phone.getText().toString().trim().length() == 0) {
                showMessage("Error", "Please enter all values");
                return;
            }
            db.execSQL("INSERT INTO birthday VALUES('" + Name.getText().toString().trim() + "', '" + Dob.getText().toString().trim() + "', '" + Phone.getText().toString().trim() + "')");
            showMessage("Success", "Record added");
            clearText();
        }
        // Displaying all the records (Today)
        if (view == ViewToday) {

            String sql = "SELECT * FROM birthday where dob like '" + dayToday + "/" + monthToday + "%'";
            Cursor c = db.rawQuery(sql, null);
            if (c.getCount() == 0) {
                showMessage("Error", "No records found");
                return;
            }
            StringBuffer buffer = new StringBuffer();
            while (c.moveToNext()) {
                buffer.append("Name: " + c.getString(0) + "\n");
                buffer.append("Dob: " + c.getString(1) + "\n");
                buffer.append("Phone: " + c.getString(2) + "\n\n");
            }
            showMessage("Birthday Details", buffer.toString());
        }
        // Displaying all the records
        if (view == ViewAll) {
            Cursor c = db.rawQuery("SELECT * FROM birthday", null);
            if (c.getCount() == 0) {
                showMessage("Error", "No records found");
                return;
            }
            StringBuffer buffer = new StringBuffer();
            while (c.moveToNext()) {
                buffer.append("Name: " + c.getString(0) + "\n");
                buffer.append("Dob: " + c.getString(1) + "\n");
                buffer.append("Phone: " + c.getString(2) + "\n\n");
            }
            showMessage("Birthday Details", buffer.toString());
        }
        // Displaying all the records (Tomorrow)
        if (view == ViewTomorrow) {
            String sql = "SELECT * FROM birthday where dob like '" + dayTomo + "/" + monthTomo + "%'";
            Cursor c = db.rawQuery(sql, null);
            if (c.getCount() == 0) {
                showMessage("Error", "No records found");
                return;
            }
            StringBuffer buffer = new StringBuffer();
            while (c.moveToNext()) {
                buffer.append("Name: " + c.getString(0) + "\n");
                buffer.append("Dob: " + c.getString(1) + "\n");
                buffer.append("Phone: " + c.getString(2) + "\n\n");
            }
            showMessage("Birthday Details", buffer.toString());
        }
        //Scheduling SMS
        if (view == Sms) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            today.set(Calendar.MILLISECOND, 999);
            long initialDelay = new Date(today.getTimeInMillis() - System.currentTimeMillis()).getTime();
            //Set the schedule function
            Timer t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    sendSMS();
                }
            };
            t.scheduleAtFixedRate(tt, initialDelay, 86400000);
            showMessage("Done!", "SMS Scheduled");
            Sms.setClickable(false);
        }
    }

    public void sendSMS() {
        String number;
        String message;
        String name;
        SmsManager mySmsManager = SmsManager.getDefault();
        String sql = "SELECT * FROM birthday where dob like '" + dayTomo + "/" + monthTomo + "%'";
        Cursor c = db.rawQuery(sql, null);
        if (c.getCount() == 0) {
            showMessage("Error", "No records found");
            return;
        }
        while (c.moveToNext()) {
            name = c.getString(0);
            message = "Happy Birthday " + name + ". I wish you a day filled with great fun and a year filled with true happiness.";
            number = c.getString(2);
            mySmsManager.sendTextMessage(number, null, message, null, null);

        }
        showMessage("Success", "SMS Sent");
    }

    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void clearText() {
        Dob.setText("");
        Name.setText("");
        Phone.setText("");
        Name.requestFocus();
    }

    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
    }
}
