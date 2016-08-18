package com.cetcme.rcldandroidZhejiang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText questionEditText;
    private EditText phoneEditText;
    private Button submitButton;
    private TextView stringLengthTextView;

    private boolean showBackDialog = false;

    private KProgressHUD kProgressHUD;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setTitle("信息反馈");

        /**
         * 导航栏返回按钮
         */
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        questionEditText = (EditText) findViewById(R.id.questionEditTextInFeedback);
        phoneEditText = (EditText) findViewById(R.id.phoneEditTextInFeedback);
        submitButton = (Button) findViewById(R.id.submitButtonInFeedback);
        stringLengthTextView = (TextView) findViewById(R.id.stringLengthTextViewInFeedback);

        questionEditText.addTextChangedListener(textChangeWatcher);
        phoneEditText.addTextChangedListener(textChangeWatcher);
        submitButton.setOnClickListener(this);

//        submitButton.setEnabled(false);
//        questionEditText.setEnabled(false);
//        phoneEditText.setEnabled(false);

        toast = Toast.makeText(getApplicationContext(), "" , Toast.LENGTH_SHORT);

        kProgressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("提交中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(110, 110)
                .setCancellable(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
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
                submitButtonTapped();
                break;
        }
    }

    private void submitButtonTapped() {
        int length = questionEditText.getText().toString().length();
        if (length == 0) {
            showEmptyDialog();
            return;
        }

        if (length > 400) {
            showToMoreTextDialog();
        } else {
            submit();
//            showToDevelopDialog();
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

    private void submit() {

        kProgressHUD.show();

        SharedPreferences user = getSharedPreferences("user",0);
        String username = user.getString("username","");
        String password = user.getString("password","");
        String serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));

        RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("backMsg", questionEditText.getText().toString());
        if (!phoneEditText.getText().toString().isEmpty()) {
            params.put("phone", phoneEditText.getText().toString());
        }

        String urlBody = "http://"+serverIP+ getString(R.string.feedbackUrl);
        AsyncHttpClient client = new AsyncHttpClient();
        client.setURLEncodingEnabled(true);
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", "feedback: " + response.toString());
                Integer code;
                try {
                    code = response.getInt("code");
                    if (code == 0) {
                        toast.setText("提交成功");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showBackDialog = false;
                                onBackPressed();
                            }
                        },1000);
                    } else {
                        String msg = response.getString("msg");
                        toast.setText(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("提交失败");
                }
                kProgressHUD.dismiss();
                toast.show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("Main", errorResponse.toString());
                kProgressHUD.dismiss();
                toast.setText("网络连接失败");
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("Main", responseString);
                kProgressHUD.dismiss();
            }
        });

    }
}
