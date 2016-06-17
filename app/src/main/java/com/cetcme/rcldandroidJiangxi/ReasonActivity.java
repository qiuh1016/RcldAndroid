package com.cetcme.rcldandroidJiangxi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ReasonActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView nameTextView;
    private TextView idTextView;
    private TextView reasonTextView;
    private Button addButton;

    private Toast toast;

    private String name;
    private String id;
    private String reason;

    private SharedPreferences user;

    private ArrayList<String> ids = new ArrayList<>();
    private Boolean allowBackPress = true;

    //debug
    private Button fillButton;
    private Button idCheckButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reason);

        setTitle("添加人员");

        Bundle bundle = this.getIntent().getExtras();
        ids = bundle.getStringArrayList("ids");
        Log.i("Main",ids.toString());

        toast = Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT);

        nameTextView = (TextView) findViewById(R.id.nameTextViewInReasonActivity);
        idTextView = (TextView) findViewById(R.id.idTextViewInReasonActivity);
        reasonTextView = (TextView) findViewById(R.id.reasonTextViewInReasonActivity);
        addButton = (Button) findViewById(R.id.addButtonInReasonActivity);
        fillButton = (Button) findViewById(R.id.button111);
        idCheckButton = (Button) findViewById(R.id.idCheckButton);
        addButton.setOnClickListener(this);
        fillButton.setOnClickListener(this);
        idCheckButton.setOnClickListener(this);

        //文本改变监听
        nameTextView.addTextChangedListener(textChangeWatcher);
        idTextView.addTextChangedListener(textChangeWatcher);
        reasonTextView.addTextChangedListener(textChangeWatcher);

        name = nameTextView.getText().toString();
        id = idTextView.getText().toString();
        reason = reasonTextView.getText().toString();

        changeButtonState(false);

        //debugMode
        user = getSharedPreferences("user", 0);
        fillButton.setVisibility(user.getBoolean("debugMode", false) ? View.VISIBLE : View.INVISIBLE);
        idCheckButton.setVisibility(user.getBoolean("debugMode", false) ? View.VISIBLE : View.INVISIBLE);
        idCheckButton.setText(user.getBoolean("idCheck", true)? "ID CHECK: ON" : "ID CHECK: OFF");

//        if (user.getBoolean("debugMode", false)) {
//            fillButton.setVisibility(View.VISIBLE);
//        } else {
//            fillButton.setVisibility(View.INVISIBLE);
//        }

    }

    public void onBackPressed() {
        if (allowBackPress) {
            super.onBackPressed();
            overridePendingTransition(R.anim.push_right_in_no_alpha,R.anim.push_right_out_no_alpha);
        }
    }

    @Override
    public void onClick(View v) {
        Boolean debugModeON = user.getBoolean("debugMode", false);
        Boolean idCheckON   = user.getBoolean("idCheck"  , true );

        switch (v.getId()) {
            case R.id.addButtonInReasonActivity:

                name = nameTextView.getText().toString();
                id = idTextView.getText().toString();
                reason = reasonTextView.getText().toString();

                //身份证不重复
                for (String idNo: ids) {
                    if (id.equals(idNo)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReasonActivity.this);
                        builder.setMessage("身份证有重复");
                        builder.setTitle("错误");
                        builder.setIcon(android.R.drawable.ic_delete);
                        builder.setPositiveButton("OK", null);
                        builder.create().show();
                        return;
                    }
                }


                //身份证校验
                Boolean needCheck = !(debugModeON && !idCheckON);
                Boolean isCard = needCheck ? new PrivateEncode().isCard(id) : (id.length() == 18);
                if (!isCard) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReasonActivity.this);
                    builder.setMessage("身份证错误");
                    builder.setTitle("错误");
                    builder.setIcon(android.R.drawable.ic_delete);
                    builder.setPositiveButton("OK", null);
                    builder.create().show();
                    return;
                }

                //增加
                SharedPreferences punchToAdd = getSharedPreferences("punchToAdd", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = punchToAdd.edit();
                editor.putString("name", name);
                editor.putString("id", id);
                editor.putString("reason", reason);
                editor.putBoolean("toAdd", true);
                editor.apply();

                toast.setText("添加成功");
                toast.show();
                changeButtonState(false);

                allowBackPress = false;
                //返回上一页
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        allowBackPress = true;
                        onBackPressed();
                    }
                },800);
                break;

            case R.id.button111:
                nameTextView.setText("测试人员");
                idTextView.setText("330283198811240134");
                reasonTextView.setText("添加原因");
                break;
            case R.id.idCheckButton:
                idCheckON = !idCheckON;
                SharedPreferences.Editor userEditor = user.edit();
                userEditor.putBoolean("idCheck", idCheckON);
                userEditor.apply();
                idCheckButton.setText(idCheckON? "ID CHECK: ON" : "ID CHECK: OFF");
                toast.setText(idCheckON? "ID CHECK: ON" : "ID CHECK: OFF");
                toast.show();

        }
    }

    private void changeButtonState(Boolean state) {
        addButton.setEnabled(state);
        addButton.setBackgroundColor(state ? 0xFF3562BD : 0x552884EF);
    }

    private TextWatcher textChangeWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            name = nameTextView.getText().toString();
            id = idTextView.getText().toString();
            reason = reasonTextView.getText().toString();

            if (name.isEmpty() || id.isEmpty() || reason.isEmpty()) {
                changeButtonState(false);
            } else {
                changeButtonState(true);
            }

        }
    };

}
