package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

public class MainActivity extends AppCompatActivity {

    private EditText shipNumberEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button closeButton;
    private ProgressBar progressBar;
    private JSONObject myShipInfo;
    private KProgressHUD kProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        shipNumberEditText = (EditText) findViewById(R.id.editText2);
        passwordEditText = (EditText) findViewById(R.id.editText);
        loginButton = (Button) findViewById(R.id.loginButton);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setEnabled(false);
                kProgressHUD = KProgressHUD.create(MainActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("登录中")
                        .setAnimationSpeed(1)
                        .setDimAmount(0.3f)
                        .setSize(110, 110)
                        .show();
                login(shipNumberEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    loginButton.callOnClick();
                }
                return false;
            }
        });

        closeButton = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button autofillButton = (Button) findViewById(R.id.autofillButton);
        assert autofillButton != null;
        autofillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shipNumberEditText.setText("3304001987070210");
                passwordEditText.setText("ICy5YqxZB1uWSwcVLSNLcA==");
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
            }
        });

    }

    public void login(final String shipNumber, final String password) {
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("userType", 0);

        String urlBody = "http://120.27.149.252/api/app/login.json";

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Integer code;
                try {
                    code = response.getInt("code");
                    if (code == 0) {
                        getShipInfo(shipNumber,password);
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
        });
    }

    public void getShipInfo(final String shipNumber, final String password) {
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        String urlBody = "http://120.27.149.252/api/app/ship/get.json";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println(response);
                myShipInfo = response;
                kProgressHUD.dismiss();
                WriteSharedPreferences(shipNumber, password);
                Toast.makeText(getApplicationContext(), "Login Succeed!", LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        Bundle bundle = new Bundle();
                        bundle.putString("myShipInfo", myShipInfo.toString());

                        Intent indexIntent = new Intent();
                        indexIntent.setClass(getApplicationContext(),IndexActivity.class);
                        indexIntent.putExtras(bundle);
                        startActivity(indexIntent);
                        //overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                        finish();

                    }
                }, 500);
            }
        });
    }

    void ReadSharedPreferences(){
        String strName,strPassword;
        SharedPreferences user = getSharedPreferences("user",0);
        strName = user.getString("shipNumber","");
        strPassword = user.getString("password","");

    }

    void WriteSharedPreferences(String strName,String strPassword){
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = user.edit();
        editor.putString("shipNumber", strName);
        editor.putString("password", strPassword);
        editor.apply();
    }


}

