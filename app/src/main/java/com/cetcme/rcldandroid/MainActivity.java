package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
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

    //debug button
    private Button ipButton;
    private Button ip1Button;
    private Button ip2Button;
    private Button fillButton;
    private Button nullButton;
    private Button quitButton;

    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //百度地图初始化
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        client = new AsyncHttpClient();
        toast = Toast.makeText(getApplicationContext(),"",LENGTH_SHORT);

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
            versionNumber.setText("©2016 CETCME V" + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //debug
        ipButton    = (Button) findViewById(R.id.ipButton       );
        ip1Button   = (Button) findViewById(R.id.ip1Button      );
        ip2Button   = (Button) findViewById(R.id.ip2Button      );
        fillButton  = (Button) findViewById(R.id.autofillButton );
        nullButton  = (Button) findViewById(R.id.nullButton     );
        quitButton  = (Button) findViewById(R.id.quitButton     );

        ipButton    .setOnClickListener(this);
        ip1Button   .setOnClickListener(this);
        ip2Button   .setOnClickListener(this);
        fillButton  .setOnClickListener(this);
        nullButton  .setOnClickListener(this);
        quitButton  .setOnClickListener(this);

        if (user.getBoolean("debugMode", false)) {
            debugModeEnable(true);
        } else {
            debugModeEnable(false);
        }


    }

    private void debugModeEnable(Boolean enable) {
//        if (enable) {
//            ipButton.setVisibility(View.VISIBLE);
//            ip1Button.setVisibility(View.VISIBLE);
//            ip2Button.setVisibility(View.VISIBLE);
//            fillButton.setVisibility(View.VISIBLE);
//            nullButton.setVisibility(View.VISIBLE);
//            quitButton.setVisibility(View.VISIBLE);
//        } else {
//            ipButton.setVisibility(View.INVISIBLE);
//            ip1Button.setVisibility(View.INVISIBLE);
//            ip2Button.setVisibility(View.INVISIBLE);
//            fillButton.setVisibility(View.INVISIBLE);
//            nullButton.setVisibility(View.INVISIBLE);
//            quitButton.setVisibility(View.INVISIBLE);
//        }
        ipButton    .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        ip1Button   .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        ip2Button   .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        fillButton  .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        nullButton  .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        quitButton  .setVisibility( enable? View.VISIBLE : View.INVISIBLE);

        shipNumberEditText.clearFocus();
        passwordEditText.clearFocus();
    }

    @Override
    public void onClick(View v) {

        String shipName = shipNumberEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user.edit();
        switch (v.getId()) {
            case R.id.loginButton:

                //设置
//                if (shipName.equals("setserverip") && !password.equals("")) {
//                    //setServerIP
//                    Boolean isIP =  new PrivateEncode().ipCheck(password);
//                    if (isIP) {
//                        editor.putString("serverIP", password);
//                        editor.apply();
//                        Toast.makeText(getApplicationContext(), "服务器IP修改成功：" + password, LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getApplicationContext(), "IP格式错误", LENGTH_SHORT).show();
//                    }
//                    return;
//                } else if (shipName.equals("showserverip")) {
//                    //showserverip
//                    String serverIP = user.getString("serverIP", "120.27.149.252");
//                    Toast.makeText(getApplicationContext(), "当前服务器IP：" + serverIP, LENGTH_SHORT).show();
//                    return;
//                } else if (shipName.equals("setserveripdefault")) {
//                    //setServerIP
//                    editor.putString("serverIP", "120.27.149.252");
//                    editor.apply();
//                    Toast.makeText(getApplicationContext(), "服务器IP修改成功：" + "120.27.149.252", LENGTH_SHORT).show();
//                    return;
//                }  else if (shipName.equals("setserveripdefault2")) {
//                    //setServerIP2
//                    editor.putString("serverIP", "114.55.101.20");
//                    editor.apply();
//                    Toast.makeText(getApplicationContext(), "服务器IP修改成功：" + "114.55.101.20", LENGTH_SHORT).show();
//                    return;  //114.55.101.20
//                } else if (shipName.equals("debugmodeon") && password.equals("admin")) {
//                    //debug mode on 显示fill button
//                    editor.putBoolean("debugMode", true);
//                    editor.apply();
//                    Toast.makeText(getApplicationContext(), "Debug Mode: ON", LENGTH_SHORT).show();
//                    debugModeEnable(true);
//                    return;
//                } else if (shipName.equals("debugmodeoff") && password.equals("admin")) {
//                    //debug mode off 不显示fill button
//                    editor.putBoolean("debugMode", false);
//                    editor.apply();
//                    Toast.makeText(getApplicationContext(), "Debug Mode: OFF", LENGTH_SHORT).show();
//                    fillButton.setVisibility(View.INVISIBLE);
//                    return;
//                }

                //打开debug mode
                if (shipName.equals("debugmodeon") && password.equals("admin")) {
                    //debug mode on 显示fill button
                    editor.putBoolean("debugMode", true);
                    editor.apply();
                    toast.setText("Debug Mode: ON");
                    toast.show();
                    debugModeEnable(true);
                    passwordEditText.setText("");
                    shipNumberEditText.setText("");
                    return;
                }

                //登录
                loginButton.setEnabled(false);
                kProgressHUD = KProgressHUD.create(MainActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("登录中")
                        .setAnimationSpeed(1)
                        .setDimAmount(0.3f)
                        .setSize(110, 110)
                        .setCancellable(false)
                        .show();
                login(shipName, password);
                break;
            case R.id.closeButton:
                finish();
                break;
            case R.id.autofillButton:
                shipNumberEditText.setText("3304001987070210"); //3304001987070210   16040205  99999999
                passwordEditText.setText("123"); //ICy5YqxZB1uWSwcVLSNLcA==
                break;
            case R.id.ipButton:

                final EditText editText = new EditText(MainActivity.this);
                editText.setSingleLine();

                new AlertDialog.Builder(MainActivity.this).setTitle("服务器IP")
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = editText.getText().toString();
                                Boolean ipCheck = new PrivateEncode().ipCheck(input);
                                if (!ipCheck) {
                                    toast.setText("IP地址格式错误");
                                    toast.show();
                                } else {
                                    //操作
                                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = user.edit();
                                    editor.putString("serverIP", input);
                                    editor.apply();
                                    toast.setText("服务器IP修改成功：" + input);
                                    toast.show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                break;
            case R.id.ip1Button:
                editor.putString("serverIP", "120.27.149.252");
                editor.apply();
                toast.setText("服务器IP修改成功：" + "120.27.149.252");
                toast.show();
                return;
            case R.id.ip2Button:
                editor.putString("serverIP", "114.55.101.20");
                editor.apply();
                toast.setText("服务器IP修改成功：" + "114.55.101.20");
                toast.show();
                return;
            case R.id.nullButton:
                //TODO: kprogress okView
//                ImageView imageView = new ImageView(this);
//
//
//                Drawable drawable = getResources().getDrawable(R.drawable.checkmark);
//                drawable.setBounds(0,0,30,30);
////                imageView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,all,null,null);
////                imageView.setImageResource(drawable);
//                imageView.setImageDrawable(drawable);
////                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams();
////                params.width = 40;
////                params.height = 40;
////                imageView.setLayoutParams(params);
//
//
////                ViewGroup.LayoutParams params = mBackgroundLayout.getLayoutParams();
////                params.width = Helper.dpToPixel(mWidth, getContext());
////                params.height = Helper.dpToPixel(mHeight, getContext());
////                mBackgroundLayout.setLayoutParams(params);
//                final KProgressHUD k =  KProgressHUD.create(MainActivity.this)
//                        .setCustomView(imageView)
//                        .setLabel("登录成功")
//                        .setCancellable(false)
//                        .setSize(110,110)
//                        .setDimAmount(0.3f)
//                        .show();
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        k.dismiss();
//                    }
//                },2000);

                break;
            case R.id.quitButton:
                editor.putBoolean("debugMode", false);
                editor.apply();
                toast.setText("Debug Mode: OFF");
                toast.show();
                debugModeEnable(false);
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
                    toast.setText("Login Failed!");
                    toast.show();
//                    Toast.makeText(getApplicationContext(), "Login Failed!", LENGTH_SHORT).show();
                }
                kProgressHUD.dismiss();
                loginButton.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                loginButton.setEnabled(true);
                toast.setText("网络连接失败");
                toast.show();
//                Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
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

                //保存deviceNo 供轨迹查询
                try {
                    JSONArray data = response.getJSONArray("data");
                    JSONObject data0 = data.getJSONObject(0);
                    String deviceNo = data0.getString("deviceNo");

                    //保存deviceNo
                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = user.edit();
                    editor.putString("deviceNo", deviceNo);
                    editor.apply();
                    Log.i("Main", "deviceNo saved:" + deviceNo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                WriteSharedPreferences(shipNumber, password);

                //指示器
                kProgressHUD.dismiss();
                toast.setText("登录成功!");
                toast.show();

                //页面切换
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("myShipInfo", myShipInfo.toString());
                        Intent indexIntent = new Intent();
                        indexIntent.setClass(getApplicationContext(), IndexActivity.class);
                        indexIntent.putExtras(bundle);
                        startActivity(indexIntent);
                        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                        finish();

                    }
                }, 300);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                loginButton.setEnabled(true);
                toast.setText("网络连接失败");
                toast.show();
            }
        });
    }

    void ReadSharedPreferences() {
        String strName, strPassword;
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        strName = user.getString("shipNumber", "");
        strPassword = user.getString("password", "");
        //填充EditText
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

}
