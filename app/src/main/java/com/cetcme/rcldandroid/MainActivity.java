package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnKeyListener {

    private EditText shipNumberEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button closeButton;
    private CheckBox savePasswordCheckBox;

    private JSONObject myShipInfo;
    private KProgressHUD kProgressHUD;

    AsyncHttpClient client;
    Boolean savePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //百度地图初始化
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        client = new AsyncHttpClient();

        shipNumberEditText = (EditText) findViewById(R.id.shipNumberEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        passwordEditText.setOnKeyListener(this);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);
        savePasswordCheckBox = (CheckBox) findViewById(R.id.savePasswordCheckBox);
        savePasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePassword = isChecked;
                SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = user.edit();
                editor.putBoolean("savePassword",savePassword);
                editor.apply();
            }
        });

        //savePassword operation
        SharedPreferences user = getSharedPreferences("user", 0);
        savePassword = user.getBoolean("savePassword", false);
        if (savePassword) {
            ReadSharedPreferences();
            savePasswordCheckBox.setChecked(true);
        }

        //Display the current version number
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
            TextView versionNumber = (TextView) findViewById(R.id.versionTextViewInMainActivity);
            versionNumber.setText("©2016 CETCME " + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Button autofillButton = (Button) findViewById(R.id.autofillButton);
        autofillButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:

                String shipName = shipNumberEditText.getText().toString();
                String password = passwordEditText.getText().toString();


                //设置
                if (shipName.equals("setserverip") && !password.equals("")) {
                    //setServerIP
                    Boolean isIP =  new PrivateEncode().ipCheck(password);
                    if (isIP) {
                        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = user.edit();
                        editor.putString("serverIP", password);
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "服务器IP修改成功：" + password, LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "IP格式错误", LENGTH_SHORT).show();
                    }
                    return;
                } else if (shipName.equals("showserverip")) {
                    //showserverip
                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                    String serverIP = user.getString("serverIP", "120.27.149.252");
                    Toast.makeText(getApplicationContext(), "当前服务器IP：" + serverIP, LENGTH_SHORT).show();
                    return;
                } else if (shipName.equals("setserveripdefault")) {
                    //setServerIP
                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = user.edit();
                    editor.putString("serverIP", "120.27.149.252");
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "服务器IP修改成功：" + "120.27.149.252", LENGTH_SHORT).show();
                    return;
                }  else if (shipName.equals("setserveripdefault2")) {
                    //setServerIP2
                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = user.edit();
                    editor.putString("serverIP", "114.55.101.20");
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "服务器IP修改成功：" + "114.55.101.20", LENGTH_SHORT).show();
                    return;  //114.55.101.20
                }


                //登录
                loginButton.setEnabled(false);
                kProgressHUD = KProgressHUD.create(MainActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("登录中")
                        .setAnimationSpeed(1)
                        .setDimAmount(0.3f)
                        .setSize(110, 110)
                        .show();
                login(shipName, password);
                break;
            case R.id.autofillButton:
                shipNumberEditText.setText("3304001987070210"); //3304001987070210   16040205  99999999
                passwordEditText.setText("123"); //ICy5YqxZB1uWSwcVLSNLcA==
//TODO: OKView
//                ImageView imageView = new ImageView(MainActivity.this);
//                imageView.setImageResource(R.drawable.checkmark);
////                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
////                params.height = 30;
////                params.width = 30;
////                imageView.setLayoutParams(params);
//
//
//                final KProgressHUD textHud = KProgressHUD.create(MainActivity.this)
//                        .setCustomView(imageView)
//                        .setLabel("登录成功")
//                        .setSize(110,110)
//                        .show();
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        textHud.dismiss();
//                    }
//                }, 1000);
                break;
            case R.id.closeButton:
                finish();
                break;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.passwordEditText:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //TODO: 回车登录
//                    loginButton.callOnClick();
                }
                break;
        }

        return false;
    }

    public void login(final String shipNumber, final String password) {
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", new PrivateEncode().b64_md5(password));
        params.put("userType", 0);

        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        String serverIP = user.getString("serverIP", "120.27.149.252");
        String urlBody = "http://"+serverIP+"/api/app/login.json";


        //TODO: json 解析 try 全部分开
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8") {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Integer code;
                try {
                    code = response.getInt("code");
                    if (code == 0) {
                        getShipInfo(shipNumber, password);
                        return;
                    } else {
                        String msg = response.getString("msg");
                        System.out.println(msg);
                        Toast.makeText(getApplicationContext(), msg, LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Login Failed!", LENGTH_SHORT).show();
                }
                kProgressHUD.dismiss();
                loginButton.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                loginButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getShipInfo(final String shipNumber, final String password) {
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", new PrivateEncode().b64_md5(password));

        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        String serverIP = user.getString("serverIP", "120.27.149.252");
        String urlBody = "http://"+serverIP+"/api/app/ship/get.json";
        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8") {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());

                myShipInfo = response;
                kProgressHUD.dismiss();
                WriteSharedPreferences(shipNumber, password);
                Toast.makeText(MainActivity.this, "登录成功!", LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("myShipInfo", myShipInfo.toString());

                        Intent indexIntent = new Intent();
                        indexIntent.setClass(getApplicationContext(), IndexActivity.class);
                        indexIntent.putExtras(bundle);
                        startActivity(indexIntent);
                        //overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                        finish();

                    }
                }, 500);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                loginButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void ReadSharedPreferences() {
        String strName, strPassword;
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        strName = user.getString("shipNumber", "");
        strPassword = user.getString("password", "");

        shipNumberEditText.setText(strName);
        passwordEditText.setText(strPassword);

    }

    void WriteSharedPreferences(String strName, String strPassword) {
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user.edit();
        editor.putString("shipNumber", strName);
        editor.putString("password", strPassword);
        editor.apply();
    }

    //TODO: kprogress 按返回键的情况
    @Override
    protected void onStart() {
        super.onStart();
        //client.cancelAllRequests(true);
//        client.cancelRequests(getApplicationContext(), true);
//        Log.i("******************main", "123");
    }
}
