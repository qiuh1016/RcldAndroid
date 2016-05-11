package com.cetcme.rcldandroid;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

    private Button fillButton;

    String name;
    String id;
    String reason;

    ArrayList<String> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reason);

        setTitle("添加人员");

        Bundle bundle = this.getIntent().getExtras();
        ids = bundle.getStringArrayList("ids");
        Log.i("Main",ids.toString());

        nameTextView = (TextView) findViewById(R.id.nameTextViewInReasonActivity);
        idTextView = (TextView) findViewById(R.id.idTextViewInReasonActivity);
        reasonTextView = (TextView) findViewById(R.id.reasonTextViewInReasonActivity);
        addButton = (Button) findViewById(R.id.addButtonInReasonActivity);
        fillButton = (Button) findViewById(R.id.button111);
        addButton.setOnClickListener(this);
        fillButton.setOnClickListener(this);

        SharedPreferences user = getSharedPreferences("user", 0);
        if (user.getBoolean("debugMode", false)) {
            fillButton.setVisibility(View.VISIBLE);
        } else {
            fillButton.setVisibility(View.INVISIBLE);
        }

        //文本改变监听
        nameTextView.addTextChangedListener(textChangeWatcher);
        idTextView.addTextChangedListener(textChangeWatcher);
        reasonTextView.addTextChangedListener(textChangeWatcher);

        name = nameTextView.getText().toString();
        id = idTextView.getText().toString();
        reason = reasonTextView.getText().toString();

        changeButtonState(false);
    }

    public void onBackPressed() {
        super.onBackPressed();
//        overridePendingTransition(R.anim.zoom_in_back,R.anim.zoom_out_back);
//        overridePendingTransition(R.anim.zoom_out,R.anim.zoom_in);
        //        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        overridePendingTransition(R.anim.push_right_in_no_alpha,R.anim.push_right_out_no_alpha);
    }

    @Override
    public void onClick(View v) {
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

                //TODO: 身份证 正确
                if (id.length() != 18 /*&& new PrivateEncode().isCard(id)*/) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReasonActivity.this);
                    builder.setMessage("身份证错误");
                    builder.setTitle("错误");
                    builder.setIcon(android.R.drawable.ic_delete);
                    builder.setPositiveButton("OK", null);
                    builder.create().show();
                    return;
                }


                //增加
                SharedPreferences user = getSharedPreferences("punchToAdd", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();
                editor.putString("name", name);
                editor.putString("id", id);
                editor.putString("reason", reason);
                editor.putBoolean("toAdd", true);
                editor.apply();

                Toast.makeText(getApplicationContext(),"添加成功",Toast.LENGTH_SHORT).show();
                changeButtonState(false);
//                dialog(name,id,reason);

                break;
            case R.id.button111:
                nameTextView.setText("测试人员");
                idTextView.setText("330283198811240000");
                reasonTextView.setText("添加原因");
                break;
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

    protected void dialog(String name, String id, String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReasonActivity.this);
        builder.setMessage(
                "姓名：" + name + "\n" +
                "身份证：" + id + "\n" +
                "原因：" + reason + "\n"
        );
        builder.setTitle("添加完成");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeButtonState(false);
//                onBackPressed();
//                overridePendingTransition(R.anim.push_left_out_no_alpha, R.anim.push_left_in_no_alpha);
            }
        });
        builder.create().show();
    }

}
