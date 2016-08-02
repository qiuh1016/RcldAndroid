package com.cetcme.rcldandroidZhejiang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText questionEditText;
    private EditText phoneEditText;
    private Button submitButton;
    private TextView stringLengthTextView;

    private boolean showBackDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setTitle("信息反馈");

        questionEditText = (EditText) findViewById(R.id.questionEditTextInFeedback);
        phoneEditText = (EditText) findViewById(R.id.phoneEditTextInFeedback);
        submitButton = (Button) findViewById(R.id.submitButtonInFeedback);
        stringLengthTextView = (TextView) findViewById(R.id.stringLengthTextViewInFeedback);

        questionEditText.addTextChangedListener(textChangeWatcher);
        phoneEditText.addTextChangedListener(textChangeWatcher);
        submitButton.setOnClickListener(this);
    }

    public void onBackPressed() {
        if (showBackDialog) {
            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
            dialog.setIcon(android.R.drawable.ic_delete);
            dialog.setTitle("放弃输入？");
            dialog.setPositiveButton("放弃", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showBackDialog = false;
                    onBackPressed();
                }
            });
            dialog.setNegativeButton("取消",null);
            dialog.show();
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.push_right_in_no_alpha,
                    R.anim.push_right_out_no_alpha);
        }
    }

    private TextWatcher textChangeWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String question = questionEditText.getText().toString();
            String phone = phoneEditText.getText().toString();

            if (question.length() > 400) {
                stringLengthTextView.setTextColor(Color.RED);
            } else {
                stringLengthTextView.setTextColor(Color.GRAY);
            }

            stringLengthTextView.setText(question.length() + "/400");

            showBackDialog = question.length() != 0 || phone.length() != 0;

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.submitButtonInFeedback:
                submit();
                break;
        }
    }

    private void submit() {
        int length = questionEditText.getText().toString().length();
        if (length == 0) {
            showEmptyDialog();
            return;
        }

        if (length > 400) {
            showToMoreTextDialog();
        } else {
            showToDevelopDialog();
        }
    }

    private void showToMoreTextDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setTitle("提交失败")
                .setMessage("请控制字数在400字以内！")
                .setCancelable(false)
                .setPositiveButton("好的", null);
        builder.create().show();
    }

    private void showEmptyDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setTitle("提交失败")
                .setMessage("内容不能为空！")
                .setCancelable(false)
                .setPositiveButton("好的", null);
        builder.create().show();
    }

    private void showToDevelopDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setTitle("功能待开发")
                .setMessage("请等待！")
                .setCancelable(false)
                .setPositiveButton("好的", null);
        builder.create().show();
    }
}
