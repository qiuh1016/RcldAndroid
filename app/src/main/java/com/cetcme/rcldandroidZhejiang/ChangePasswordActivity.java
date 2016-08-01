package com.cetcme.rcldandroidZhejiang;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText oldPWEditText;
    private EditText newPWEditText_1;
    private EditText newPWEditText_2;
    private Button changePWButton;

    private String oldPW;
    private String newPW_1;
    private String newPW_2;

    private Toast toast;
    private KProgressHUD kProgressHUD;

    private Boolean showBackDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        setTitle("修改密码");

        oldPWEditText = (EditText) findViewById(R.id.oldPWEditText);
        newPWEditText_1 = (EditText) findViewById(R.id.newPWEditText_1);
        newPWEditText_2 = (EditText) findViewById(R.id.newPWEditText_2);
        changePWButton = (Button) findViewById(R.id.changePWButton);

        changePWButton.setOnClickListener(this);
        changeButtonState(false);

        //文本改变监听
        oldPWEditText.addTextChangedListener(textChangeWatcher);
        newPWEditText_1.addTextChangedListener(textChangeWatcher);
        newPWEditText_2.addTextChangedListener(textChangeWatcher);


        toast = Toast.makeText(getApplicationContext(), "" , Toast.LENGTH_SHORT);
        kProgressHUD = KProgressHUD.create(ChangePasswordActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("修改中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setCancellable(false)
                .setSize(110, 110);

    }

    public void onBackPressed() {
        if (showBackDialog) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ChangePasswordActivity.this);
            dialog.setIcon(android.R.drawable.ic_delete);
            dialog.setTitle("放弃修改？");
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

    private void showNotEqualsDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ChangePasswordActivity.this);
        dialog.setIcon(android.R.drawable.ic_delete);
        dialog.setTitle("错误");
        dialog.setMessage("新密码输入不一致");
        dialog.setCancelable(false);
        dialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newPWEditText_1.setText("");
                newPWEditText_2.setText("");
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePWButton:
                if (newPW_1.equals(newPW_2)) {
                    changePW();
                } else {
                    showNotEqualsDialog();
                }

                break;
        }
    }

    private void changePW() {

        changeButtonState(false);
        kProgressHUD.show();

        SharedPreferences user = getSharedPreferences("user",0);
        String username = user.getString("username","");
        String password = user.getString("password","");
        String serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));

        RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", PrivateEncode.b64_md5(oldPW));
        params.put("newPassword", PrivateEncode.b64_md5(newPW_1));

        String urlBody = "http://"+serverIP+ getString(R.string.changePWUrl);
//        String url = urlBody + "?userName="+username+"&password="+password+"&shipNo="+shipNo+"&picName="+toChangePicName+"&picTelNo="+toChangePicTelNo;
        AsyncHttpClient client = new AsyncHttpClient();
        //TODO: 拼接url问题
        client.setURLEncodingEnabled(true);
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                Integer code;
                try {
                    code = response.getInt("code");
                    if (code == 0) {
                        toast.setText("修改成功");
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
                    toast.setText("修改失败");
                }
                kProgressHUD.dismiss();
                toast.show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.i("Main", errorResponse.toString());
                kProgressHUD.dismiss();
                changeButtonState(true);
                toast.setText("网络连接失败");
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("Main", responseString);
                kProgressHUD.dismiss();
                changeButtonState(true);
            }
        });

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
            oldPW = oldPWEditText.getText().toString();
            newPW_1 = newPWEditText_1.getText().toString();
            newPW_2 = newPWEditText_2.getText().toString();

            //都不为空
            if (oldPW != null && newPW_1 != null && newPW_2 != null ) {
                changeButtonState(true);
                showBackDialog = true;

            } else {
                changeButtonState(false);
                showBackDialog = false;
            }

        }
    };

    private void changeButtonState(Boolean state) {
        changePWButton.setEnabled(state);
        changePWButton.setBackgroundColor(state ? 0xFF3562BD : 0x552884EF);
    }
}
