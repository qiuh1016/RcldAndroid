package com.cetcme.rcldandroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.client.SystemDefaultCredentialsProvider;

public class RouteActivity extends AppCompatActivity implements View.OnClickListener{

    private Button startTimePickButton;
    private Button endTimePickButton;
    private Button routeSearchButton;
    private Switch showMediumPointSwitch;

    KProgressHUD kProgressHUD;
    Toast toast;
    SlideDateTimePicker slideDateTimeListener;
    Boolean isStartTime = true;

    private String startTime;
    private String endTime;
    private String dataString;

    List<LatLng> route;

    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            // Do something with the date. This Date object contains
            // the date and time that the user has selected.

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            if (isStartTime) {
                startTime = df.format(date);
                startTimePickButton.setText(startTime);
            } else {
                endTime = df.format(date);
                endTimePickButton.setText(endTime);
            }

            Log.i("Datetime",df.format(date));
        }

        @Override
        public void onDateTimeCancel()
        {
            // Overriding onDateTimeCancel() is optional.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        setTitle("轨迹记录");
        toast = Toast.makeText(getApplicationContext(),"没有符合条件的数据",Toast.LENGTH_SHORT);

        startTimePickButton = (Button) findViewById(R.id.startTimePickButton);
        endTimePickButton = (Button) findViewById(R.id.endTimePickButton);
        routeSearchButton = (Button) findViewById(R.id.routeSearchButton);
        showMediumPointSwitch = (Switch) findViewById(R.id.showMediumPointSwitchInRouteActivity);

        startTimePickButton.setOnClickListener(this);
        endTimePickButton.setOnClickListener(this);
        routeSearchButton.setOnClickListener(this);

        //TODO：点击已有时间的按钮 时候 时间
        slideDateTimeListener = new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(listener)
                .setInitialDate(new Date())
                .setIs24HourTime(true)
                .build();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startTimePickButton:
                isStartTime = true;
                slideDateTimeListener.show();
//                pickTime(startTimePickButton);
                break;
            case R.id.endTimePickButton:
                isStartTime = false;
                slideDateTimeListener.show();
//                pickTime(endTimePickButton);
                break;
            case R.id.routeSearchButton:
                if (startTime == null || endTime == null) {
                    dialog();
                } else {
                    kProgressHUD = KProgressHUD.create(RouteActivity.this)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("查询中")
                            .setAnimationSpeed(1)
                            .setDimAmount(0.3f)
                            .setSize(110, 110)
                            .show();
                    getRouteData();
                }
                break;
        }
    }

    public void showDisplayIntent() {
        Bundle bundle = new Bundle();
        bundle.putString("startTime", startTime);
        bundle.putString("endTime", endTime);
        bundle.putBoolean("showMediaPoint", showMediumPointSwitch.isChecked());
        bundle.putString("dataString", dataString);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), RouteDisplayActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
    }

    public void onBackPressed() {
        toast.cancel();
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
        builder.setMessage("起始时间或结束时间不能为空！");
        builder.setTitle("Error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Log.i("Main", "dialog dismiss ok");

            }
        });
        builder.create().show();
    }

    private void getRouteData() {

        String shipNumber,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");

        String startTimeURL = startTime.replace(" ", "%20");
        String endTimeURL = endTime.replace(" ", "%20");

        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", new PrivateEncode().b64_md5(password));
        params.put("startTime", startTimeURL);
        params.put("endTime", endTimeURL);

        String ps = new PrivateEncode().b64_md5(password);

        String urlBody = "http://"+serverIP+"/api/app/trail/get.json";
        String url = urlBody+"?userName=" + shipNumber +"&password="+ps+"&startTime="+startTimeURL+"&endTime=" + endTimeURL;
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                route = new ArrayList<>();

                //TODO: 有问题
                try {
                    String msg = response.getString("msg");
                    Log.i("Main", msg);
                    if (msg.equals("没有符合条件的数据")) {
                        toast.setText("没有符合条件的数据");
                        toast.show();
                        kProgressHUD.dismiss();
                    } else if (msg.equals("成功")) {
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject point = data.getJSONObject(i);
                            Double lat = point.getDouble("latitude");
                            Double lng = point.getDouble("longitude");
                            LatLng latLng = new LatLng(lat,lng);
                            route.add(latLng);
                        }
                        Log.i("Main", "getRouteArray");
                        geoconv(route);
//                        showDisplayIntent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    kProgressHUD.dismiss();
                    toast.setText("获取失败");
                    toast.show();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                toast.setText("获取失败");
                toast.show();
            }
        });

    }

    public void geoconv(List<LatLng> list) {

        String urlBody = "http://api.map.baidu.com/geoconv/v1/";
        String ak = "stfZ8nXV0rvMfTLuAAY9SX2AqgLGLuOQ";
        RequestParams params = new RequestParams();
        String coords = "";
        for (LatLng latLng :list) {
            coords += latLng.longitude + "," + latLng.latitude + ";";
        }
        coords = coords.substring(0, coords.length() - 1); //去掉最后一个分号
        params.put("coords", coords);
        params.put("ak", ak);

        //TODO: 一次最多100个点
        //TODO: 纠偏失败 显示原来的点

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                Integer status;
                try {
                    status = response.getInt("status");
                    if (status == 0) {
                        dataString = response.toString();
                        showDisplayIntent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("纠偏失败");
                    toast.show();
                }
                kProgressHUD.dismiss();

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                toast.setText("纠偏失败");
                toast.show();
            }
        });
    }

}
