package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlertDialogActivity extends Activity {

    private Button closeButton;
    private TextView timeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_dialog);

        setTitle("防盗报警");
        closeButton = (Button) findViewById(R.id.closeButtonInDialogActivity);
        timeTextView = (TextView) findViewById(R.id.timeTextViewInDialogActivity);

        SharedPreferences s = getSharedPreferences("antiThief", Context.MODE_PRIVATE);
        String alertTime = s.getString("alertTime","/");
        timeTextView.setText("报警时间:" + alertTime);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences antiThief = getSharedPreferences("antiThief", 0);
                SharedPreferences.Editor edit = antiThief.edit();
                edit.putBoolean("notification", false);
                edit.putBoolean("antiThiefIsOpen", false);
                edit.apply();
                finish();
            }
        });

    }
}
