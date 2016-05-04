package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.security.PrivateKey;

public class ChangeInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText picNameEditText;
    private EditText picTelNoEditText;
    private EditText antiThiefRadiusEditText;
    private Button changeInfoButton;

    String originalPicName;
    String originalPicTelNo;
    String originalAntiThiefRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);
        setTitle("信息修改");

        picNameEditText = (EditText) findViewById(R.id.picNameEditTextInChangeInfoActivity);
        picTelNoEditText = (EditText) findViewById(R.id.picTelNoEditTextInChangeInfoActivity);
        antiThiefRadiusEditText = (EditText) findViewById(R.id.antiThiefRadiusTextInChangeInfoActivity);
        changeInfoButton = (Button) findViewById(R.id.changeInfoButton);

        Bundle bundle = this.getIntent().getExtras();
        originalPicName = bundle.getString("picName");
        originalPicTelNo = bundle.getString("picTelNo");

        SharedPreferences user = getSharedPreferences("user",0);
//        originalAntiThiefRadius = user.getString("antiThiefRadius","");
//        originalAntiThiefRadius = "2"; //TODO:

        setDefaultAntiTHiefRadius();

        picNameEditText.setText(originalPicName);
        picTelNoEditText.setText(originalPicTelNo);
        antiThiefRadiusEditText.setText(originalAntiThiefRadius);


    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);

        //TODO: 返回提示
//        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeInfoActivity.this);
//        builder.setMessage("");
//        builder.setTitle("放弃修改？");
//        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//                fileList();
//                overridePendingTransition(R.anim.push_right_in_no_alpha,
//                        R.anim.push_right_out_no_alpha);
//
//            }
//        });
//
//        builder.setNegativeButton("否",null);
//        builder.create().show();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changeInfoButton:

                break;
        }
    }

    void setDefaultAntiTHiefRadius() {
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        originalAntiThiefRadius = user.getString("antiThiefRadius","");
        Log.i("Main",originalAntiThiefRadius);
        if (originalAntiThiefRadius == null) {
            SharedPreferences.Editor editor = user.edit();
            editor.putString("antiThiefRadius", "2");
            editor.apply();
            originalAntiThiefRadius = "2";
        }

    }
}
