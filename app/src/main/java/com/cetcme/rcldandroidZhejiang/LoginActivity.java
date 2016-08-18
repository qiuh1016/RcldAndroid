package com.cetcme.rcldandroidZhejiang;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,View.OnKeyListener {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button closeButton;
    private CheckBox savePasswordCheckBox;

    private JSONObject myShipInfo;
    private KProgressHUD kProgressHUD;
    private KProgressHUD okHUD;

    private AsyncHttpClient client;
    private Boolean savePassword;
    private Toast toast;

    //debug button
    private Button ipButton;
    private Button ip1Button;
    private Button ip2Button;
    private Button fillButton;
    private Button nullButton;
    private Button quitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //百度地图初始化
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        client = new AsyncHttpClient();
        toast = Toast.makeText(getApplicationContext(),"",LENGTH_SHORT);

        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
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
        } else {
            String strName;
            strName = user.getString("username", "");
            //填充EditText
            usernameEditText.setText(strName);
            savePasswordCheckBox.setChecked(false);
        }

        //Display the current version number
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
            TextView versionNumber = (TextView) findViewById(R.id.versionTextViewInMainActivity);
            versionNumber.setText("©2016 CETCME V" + pi.versionName);

            //保存
            SharedPreferences system = getSharedPreferences("system", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = system.edit();
            editor.putString("version", pi.versionName);
            editor.apply();
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

        //hudView
        kProgressHUD = KProgressHUD.create(LoginActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("登录中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(110, 110)
                .setCancellable(false);
        ImageView imageView = new ImageView(LoginActivity.this);
        imageView.setBackgroundResource(R.drawable.checkmark);
        okHUD  =  KProgressHUD.create(LoginActivity.this)
                .setCustomView(imageView)
                .setLabel("登录成功")
                .setCancellable(false)
                .setSize(110,110)
                .setDimAmount(0.3f);

        //autoUpdate
        UpdateAppManager updateManager;
        updateManager = new UpdateAppManager(this);
        updateManager.checkUpdateInfo();

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem close = menu.add(0,0,0,"二维码");
        close.setIcon(R.drawable.icon_close_window);
        close.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        close.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finish();
                return false;
            }
        });

        return true;

    }

    private void debugModeEnable(Boolean enable) {
        ipButton    .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        ip1Button   .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        ip2Button   .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        fillButton  .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        nullButton  .setVisibility( enable? View.VISIBLE : View.INVISIBLE);
        quitButton  .setVisibility( enable? View.VISIBLE : View.INVISIBLE);

        usernameEditText.clearFocus();
        passwordEditText.clearFocus();
    }

    @Override
    public void onClick(View v) {

        String shipName = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user.edit();
        switch (v.getId()) {
            case R.id.loginButton:

                if (shipName.equals("debugmodeon") && password.equals("admin")) {
                    //debug mode on 显示fill button
                    editor.putBoolean("debugMode", true);
                    editor.apply();
                    toast.setText("Debug Mode: ON");
                    toast.show();
                    debugModeEnable(true);
                    passwordEditText.setText("");
                    usernameEditText.setText("");
                    return;
                }

                //登录
                loginButton.setEnabled(false);
                kProgressHUD.show();


                //与保存的密码相等 则不加密，不相等则加密
                String savedPassword = user.getString("password", "");
                if (password.equals(savedPassword)) {
                    login(shipName, password);
                } else {
                    login(shipName,PrivateEncode.b64_md5(password));
                }

                break;
            case R.id.closeButton:
                finish();
                break;
            case R.id.autofillButton:
                usernameEditText.setText("3304001987070210"); //3304001987070210   16040205  99999999
                passwordEditText.setText("123"); //ICy5YqxZB1uWSwcVLSNLcA==
                break;
            case R.id.ipButton:

                final EditText editText = new EditText(LoginActivity.this);
                editText.setSingleLine();

                new AlertDialog.Builder(LoginActivity.this).setTitle("服务器IP")
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
                editor.putString("serverIP", getString(R.string.defaultServerIP_1));
                editor.apply();
                toast.setText("服务器IP修改成功：" + getString(R.string.defaultServerIP_1));
                toast.show();
                return;
            case R.id.ip2Button:
                editor.putString("serverIP", getString(R.string.defaultServerIP_2));
                editor.apply();
                toast.setText("服务器IP修改成功：" + getString(R.string.defaultServerIP_2));
                toast.show();
                return;
            case R.id.nullButton:
                String serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));
                toast.setText("当前服务器IP：" + serverIP);
                toast.show();

//                kProgressHUD.show();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        kProgressHUD.dismiss();
//                        okHUD.show();
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                okHUD.dismiss();
//                            }
//                        },2000);
//                    }
//                },3000);

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
                    loginButton.callOnClick();
                }
                break;
        }
        return false;
    }

    private void login(final String username, final String password) {
        RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("userType", 2);

//        Log.i("Main", params.toString());

        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        String serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));
        String urlBody = "http://"+serverIP+getString(R.string.loginUrl);

        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8") {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                Integer code;
                try {
                    code = response.getInt("code");
                    if (code == 0) {

                        //保存deviceNo 供轨迹查询
                        try {
                            String deviceNo = response.getString("deviceNo");

                            //保存deviceNo
                            SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = user.edit();
                            editor.putString("deviceNo", deviceNo);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //保存shipNo
                        try {
                            String shipNo = response.getString("shipNo");

                            //保存deviceNo
                            SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = user.edit();
                            editor.putString("shipNo", shipNo);
                            editor.apply();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        getShipInfo(username, password);

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

    private void getShipInfo(final String username, final String password) {
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        String shipNo = user.getString("shipNo", "");

        RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("shipNo", shipNo);

        Log.i("Main", "getShipInfo: " + params.toString());


        String serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));
        String urlBody = "http://"+serverIP+getString(R.string.shipGetUrl);
        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8") {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                myShipInfo = response;

//                //保存deviceNo 供轨迹查询
//                try {
//                    JSONArray data = response.getJSONArray("data");
//                    JSONObject data0 = data.getJSONObject(0);
//                    String deviceNo = data0.getString("deviceNo");
//
//                    //保存deviceNo
//                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = user.edit();
//                    editor.putString("deviceNo", deviceNo);
//                    editor.apply();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                //保存shipNo
//                try {
//                    JSONArray data = response.getJSONArray("data");
//                    JSONObject data0 = data.getJSONObject(0);
//                    String shipNo = data0.getString("shipNo");
//
//                    //保存deviceNo
//                    SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = user.edit();
//                    editor.putString("shipNo", shipNo);
//                    editor.apply();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                //与保存的账号不一致 清除其他设置
                SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
                String savedShipNumber = user.getString("username", "");
                if (!username.equals(savedShipNumber)) {
                    SharedPreferences antiThief = getSharedPreferences("antiThief", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = antiThief.edit();
                    editor.clear();
                    editor.apply();
                }

                WriteSharedPreferences(username, password);

                //指示器
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kProgressHUD.dismiss();
                        okHUD.show();

                        //页面切换
                        new Handler().postDelayed(new Runnable() {
                            public void run() {

                                Bundle bundle = new Bundle();
//                                String shipInfoTemp = "{\"code\":0,\"data\":[{\"cfsEndDate\":\"2016\\/06\\/02\",\"cfsStartDate\":\"2016\\/06\\/01\",\"deviceNo\":\"10000003\",\"fenceName\":\"园区测试港\",\"fenceNo\":\"10001\",\"idcardreaderNo\":\"10000003\",\"latitudeDisp\":\"\",\"longitudeDisp\":\"\",\"offlineFlag\":false,\"ownerName\":\"10000003\",\"ownerTelNo\":\"10000003\",\"pairFlag\":true,\"picName\":\"船主3\",\"picTelNo\":\"15058463252\",\"qrPicPath\":\"\\/static\\/images\\/10000003_c1950a9f9d984dbf86efbed635a67d2a.jpeg\",\"shipName\":\"10000003\",\"shipNo\":\"10000003\"}]}";
                                bundle.putString("myShipInfo", myShipInfo.toString());
//                                bundle.putString("myShipInfo", shipInfoTemp);
                                Intent indexIntent = new Intent();
                                indexIntent.setClass(getApplicationContext(), IndexActivity.class);
                                indexIntent.putExtras(bundle);

                                startActivity(indexIntent);
                                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
//                                okHUD.dismiss();
                                finish();

                            }
                        }, 500);
                    }
                }, 500);

//                toast.setText("登录成功!");
//                toast.show();
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

    private void ReadSharedPreferences() {
        String strName, strPassword;
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        strName = user.getString("username", "");
        strPassword = user.getString("password", "");
        //填充EditText
        usernameEditText.setText(strName);
        passwordEditText.setText(strPassword);
    }

    private void WriteSharedPreferences(String strName, String strPassword) {
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user.edit();
        editor.putString("username", strName);
        editor.putString("password", strPassword);
        editor.apply();
    }

}
