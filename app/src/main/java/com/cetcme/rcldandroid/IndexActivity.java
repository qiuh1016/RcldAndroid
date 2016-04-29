package com.cetcme.rcldandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.widget.Toast.LENGTH_SHORT;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView welcomeTextView;
    private Button myShipButton;
    private Button fenceButton;
    private Button routeButton;
    private Button helpButton;

    private JSONObject myShipInfoJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        myShipButton = (Button) findViewById(R.id.myShipButton);
        fenceButton = (Button) findViewById(R.id.fenceButton);
        routeButton = (Button) findViewById(R.id.routeButton);
        helpButton = (Button) findViewById(R.id.helpButton);

        myShipButton.setOnClickListener(this);
        fenceButton.setOnClickListener(this);
        routeButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        Bundle bundle = this.getIntent().getExtras();
        String str = bundle.getString("myShipInfo");
        //str = "{\"code\":0,\"data\":[{\"deviceNo\":\"10000001\",\"latitude\":30.782284,\"latitudeDisp\":\"N30°46′56.22\\\"\",\"longitude\":120.669029,\"longitudeDisp\":\"E120°40′8.50\\\"\",\"offlineFlag\":true,\"ownerName\":\"船东\",\"ownerTelNo\":\"18877779999\",\"picName\":\"qhhhhhTest\",\"picTelNo\":\"123456789900009\",\"shipName\":\"浙嘉渔0415\",\"shipNo\":\"3304001987070210\"}],\"msg\":\"成功\",\"success\":true,\"total\":0}";
        try {
            myShipInfoJSON = new JSONObject(str);
            JSONArray data = myShipInfoJSON.getJSONArray("data");
            JSONObject data0 = data.getJSONObject(0);
            String picName = data0.getString("picName");
            welcomeTextView.setText(picName + "，欢迎您使用本软件！");
        } catch (JSONException e) {
            e.printStackTrace();
            welcomeTextView.setText("欢迎您使用本软件！");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem qrcode = menu.add(0,0,0,"二维码");
        qrcode.setIcon(R.drawable.qrcode);
        qrcode.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        qrcode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //TODO: QRCODE
                Toast.makeText(getApplicationContext(),"二维码扫描，待开发",LENGTH_SHORT).show();
                return false;
            }
        });

        MenuItem setting = menu.add(0, 0, 0, "退出");
        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setting.setIcon(R.drawable.setting);
        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                logoutDialog();
                return false;
            }
        });


        return true;

    }

    void logout() {
        Intent loginIntent = new Intent();
        loginIntent.setClass(getApplicationContext(), MainActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.push_right_in_no_alpha, R.anim.push_right_out_no_alpha);
        finish();
    }

    protected void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("是否继续?");
        builder.setTitle("即将退出登录");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final KProgressHUD kProgressHUD = KProgressHUD.create(IndexActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("退出中")
                        .setAnimationSpeed(1)
                        .setDimAmount(0.3f)
                        .setSize(110, 110)
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kProgressHUD.dismiss();
                        logout();
                    }
                },1000);
            }
        });
        builder.setNegativeButton("否", null);
        builder.create().show();
    }

    protected void finishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("是否继续?");
        builder.setTitle("即将关闭程序");
        builder.setNeutralButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setPositiveButton("最小化", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //返回手机桌面
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    public void onBackPressed() {
        //super.onBackPressed();
        finishDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.myShipButton:
                Bundle bundle = new Bundle();
                bundle.putString("myShipInfo", myShipInfoJSON.toString());
                Intent myshipIntent = new Intent();
                myshipIntent.setClass(getApplicationContext(), MyshipActivity.class);
                myshipIntent.putExtras(bundle);
                startActivity(myshipIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;
            case R.id.fenceButton:
                Intent fenceIntent = new Intent();
                fenceIntent.setClass(getApplicationContext(), FenceActivity.class);
                startActivity(fenceIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;
            case R.id.routeButton:
                Intent routeIntent = new Intent();
                routeIntent.setClass(getApplicationContext(), RouteActivity.class);
                startActivity(routeIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;
            case R.id.helpButton:
                Intent helpIntent = new Intent();
                helpIntent.setClass(getApplicationContext(), HelpActivity.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;

        }
    }


}
