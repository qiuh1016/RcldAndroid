package com.cetcme.rcldandroid;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener,View.OnKeyListener {

    private EditText shipNumberEditText;
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
                kProgressHUD = KProgressHUD.create(LoginActivity.this)
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

                kProgressHUD.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kProgressHUD.dismiss();
                        okHUD.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                okHUD.dismiss();
                            }
                        },2000);
                    }
                },3000);

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

    private void login(final String shipNumber, final String password) {
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", new PrivateEncode().b64_md5(password));
        params.put("userType", 2);

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

    private void getShipInfo(final String shipNumber, final String password) {
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kProgressHUD.dismiss();
                        okHUD.show();

                        //页面切换
                        new Handler().postDelayed(new Runnable() {
                            public void run() {

                                Bundle bundle = new Bundle();
                                bundle.putString("myShipInfo", myShipInfo.toString());
                                Intent indexIntent = new Intent();
                                indexIntent.setClass(getApplicationContext(), IndexActivity.class);
                                indexIntent.putExtras(bundle);
                                okHUD.dismiss();
                                startActivity(indexIntent);
                                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                                finish();

                            }
                        }, 800);
                    }
                }, 1000);

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
        strName = user.getString("shipNumber", "");
        strPassword = user.getString("password", "");
        //填充EditText
        shipNumberEditText.setText(strName);
        passwordEditText.setText(strPassword);
    }

    private void WriteSharedPreferences(String strName, String strPassword) {
        SharedPreferences user = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = user.edit();
        editor.putString("shipNumber", strName);
        editor.putString("password", strPassword);
        editor.apply();
    }


}
