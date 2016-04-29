package com.cetcme.rcldandroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IndexActivity extends AppCompatActivity {

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

        //setTitleBar();

        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        myShipButton = (Button) findViewById(R.id.myShipButton);
        fenceButton = (Button) findViewById(R.id.fenceButton);
        routeButton = (Button) findViewById(R.id.routeButton);
        helpButton = (Button) findViewById(R.id.helpButton);

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

        myShipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("myShipInfo", myShipInfoJSON.toString());

                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), MyshipActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
//                finish();
//                overridePendingTransition(R.anim.push_right_in_no_alpha, R.anim.push_right_out_no_alpha);
            }
        });

        fenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), FenceActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
            }
        });

        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), RouteActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
            }
    });
    }

    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
