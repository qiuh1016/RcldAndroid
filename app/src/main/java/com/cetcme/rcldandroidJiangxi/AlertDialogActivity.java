package com.cetcme.rcldandroidJiangxi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

//        alertTime = "2016/06/17 20:21:47"; //测试用
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
