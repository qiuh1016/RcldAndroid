package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.PrivateKey;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.ResponseHandler;

import static android.widget.Toast.LENGTH_SHORT;

public class ChangeInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText picNameEditText;
    private EditText picTelNoEditText;
    private EditText antiThiefRadiusEditText;
    private Button changeInfoButton;

    String originalPicName;
    String originalPicTelNo;
    String originalAntiThiefRadius;
    String toChangePicName;
    String toChangePicTelNo;
    String toChangeAntiThiefRadius;

    Toast toast;
    KProgressHUD kProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);
        setTitle("信息修改");

        picNameEditText = (EditText) findViewById(R.id.picNameEditTextInChangeInfoActivity);
        picTelNoEditText = (EditText) findViewById(R.id.picTelNoEditTextInChangeInfoActivity);
        antiThiefRadiusEditText = (EditText) findViewById(R.id.antiThiefRadiusTextInChangeInfoActivity);
        changeInfoButton = (Button) findViewById(R.id.changeInfoButton);

        changeInfoButton.setOnClickListener(this);
        changeButtonState(false);

        Bundle bundle = this.getIntent().getExtras();
        originalPicName = bundle.getString("picName");
        originalPicTelNo = bundle.getString("picTelNo");

        SharedPreferences antiThief = getSharedPreferences("antiThief", Activity.MODE_PRIVATE);
        originalAntiThiefRadius = antiThief.getString("antiThiefRadius","");

        picNameEditText.setText(originalPicName);
        picTelNoEditText.setText(originalPicTelNo);
        antiThiefRadiusEditText.setText(originalAntiThiefRadius);

        //文本改变监听
        picNameEditText.addTextChangedListener(textChangeWatcher);
        picTelNoEditText.addTextChangedListener(textChangeWatcher);
        antiThiefRadiusEditText.addTextChangedListener(textChangeWatcher);


        toast = Toast.makeText(getApplicationContext(), "" , Toast.LENGTH_SHORT);
        kProgressHUD = KProgressHUD.create(ChangeInfoActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("修改中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setCancellable(false)
                .setSize(110, 110);

    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
        toast.cancel();
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

                //如果负责人姓名或电话需要修改，则联网在成功的时候修改半径，如果不需要修改，直接修改半径
                if (!toChangePicName.equals(originalPicName) ||
                        !toChangePicTelNo.equals(originalPicTelNo)) {
                    //修改信息
                    changeInfo();

                } else {
                    //修改半径
                    changeRadius();
                    toast.setText("半径修改成功：" + toChangeAntiThiefRadius);
                    toast.show();
                    changeButtonState(false);
                }

                break;
        }
    }

    private void changeRadius() {
        SharedPreferences antiThief = getSharedPreferences("antiThief", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = antiThief.edit();
        editor.putString("antiThiefRadius", toChangeAntiThiefRadius);
        editor.apply();
        originalAntiThiefRadius = toChangeAntiThiefRadius;
    }

    private void changeInfo() {

        changeButtonState(false);
        kProgressHUD.show();

        SharedPreferences user = getSharedPreferences("user",0);
        String shipNumber = user.getString("shipNumber","");
        String password = user.getString("password","");
        String serverIP = user.getString("serverIP", "120.27.149.252");
        password = new PrivateEncode().b64_md5(password);

        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);

        if (!toChangePicName.equals(originalPicName)) {
            params.put("picName", toChangePicName);
        }
        if (!toChangePicTelNo.equals(originalPicTelNo)) {
            params.put("picTelNo",toChangePicTelNo);
        }

//        Log.i("Main",params.toString());

        String urlBody = "http://"+serverIP+"/api/app/ship/update.json";
        String url = urlBody + "?userName="+shipNumber+"&password="+password+"&picName="+toChangePicName+"&picTelNo="+toChangePicTelNo;
        AsyncHttpClient client = new AsyncHttpClient();
        //TODO: 拼接url问题
        client.setURLEncodingEnabled(true);
        client.put(url, null, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                Integer code;
                try {
                    code = response.getInt("code");
                    if (code == 0) {
                        originalPicName = toChangePicName;
                        originalPicTelNo = toChangePicTelNo;
                        //修改半径
                        if (!toChangeAntiThiefRadius.equals(originalAntiThiefRadius)) {
                            changeRadius();
                        }
                        toast.setText("修改成功,退出后生效");
                        changeButtonState(true);
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
            //TODO: 半径01的时候
            toChangePicName = picNameEditText.getText().toString();
            toChangePicTelNo = picTelNoEditText.getText().toString();
            toChangeAntiThiefRadius = antiThiefRadiusEditText.getText().toString();
            //如果内容有变，则enable changeButton
            if (toChangePicName.equals(originalPicName) &&
                    toChangePicTelNo.equals(originalPicTelNo) &&
                    toChangeAntiThiefRadius.equals(originalAntiThiefRadius) ||
                    toChangePicTelNo.equals("") ||
                    toChangePicName.equals("") ||
                    toChangeAntiThiefRadius.equals("0") ||
                    toChangeAntiThiefRadius.equals("") ) {
                changeButtonState(false);
            } else {
                changeButtonState(true);
            }

        }
    };

    private void changeButtonState(Boolean state) {
        changeInfoButton.setEnabled(state);
        changeInfoButton.setBackgroundColor(state ? 0xFF3562BD : 0x552884EF);
    }

}
