package com.cetcme.rcldandroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LongSparseArray;
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

    private KProgressHUD kProgressHUD;
    private Toast toast;
    private SlideDateTimePicker slideDateTimeListener;
    private Boolean isStartTime = true;

    private String startTime;
    private String endTime;
    private Date startDate;
    private Date endDate;

    private String dataString;

    private List<LatLng> route;

    private Boolean reducePointBySize = false;  //根据轨迹点数量 来减少距离较近的点

    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            // Do something with the date. This Date object contains
            // the date and time that the user has selected.

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            if (isStartTime) {
                startDate = date;
                startTime = df.format(date);
                startTimePickButton.setText(startTime);
            } else {
                endDate = date;
                endTime = df.format(date);
                endTimePickButton.setText(endTime);
            }

//            Log.i("Datetime",df.format(date));
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
                break;
            case R.id.endTimePickButton:
                isStartTime = false;
                slideDateTimeListener.show();
                break;
            case R.id.routeSearchButton:
                if (startTime == null || endTime == null) {
                    dialog("起始时间或结束时间不能为空！");
                } else {
                    //1天时间
                    Long ms = endDate.getTime() - startDate.getTime();
                    if (ms > 1000 * 3600 * 24) {
                        dialog("时间差不能超过1天！");
                    } else {
                        kProgressHUD = KProgressHUD.create(RouteActivity.this)
                                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                                .setLabel("查询中")
                                .setAnimationSpeed(1)
                                .setDimAmount(0.3f)
                                .setSize(110, 110)
                                .setCancellable(false)
                                .show();
                        getRouteData();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void showDisplayIntent() {
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

    protected void dialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setMessage(msg);
        builder.setTitle("错误");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    private void getRouteData() {

        String username,password,serverIP,deviceNo;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        username = user.getString("username","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");
        deviceNo = user.getString("deviceNo","");

        //设置参数
        final RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("deviceNo", deviceNo);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        String urlBody = "http://"+serverIP+ getString(R.string.trailGetUrl);
        String url = urlBody+"?userName=" + username +"&password="+password+"&deviceNo=" + deviceNo+"&startTime="+startTime+"&endTime=" + endTime;
        AsyncHttpClient client = new AsyncHttpClient();
        client.setURLEncodingEnabled(true);
        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
//                Log.i("Main", response.toString());
                dataString = response.toString();
                route = new ArrayList<>();
                try {
                    String msg = response.getString("msg");

                    if (msg.equals("成功")) {
                        JSONArray data = response.getJSONArray("data");

                        for (int i = 0; i < data.length(); i++) {
                            try {
                                JSONObject point = data.getJSONObject(i);
                                Double lat = point.getDouble("latitude");
                                Double lng = point.getDouble("longitude");
                                LatLng latLng = new LatLng(lat,lng);
                                route.add(latLng);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                geoconv(route);
                            }
                        },1000);

                    } else {
                        //显示失败信息
                        toast.setText(msg);
                        toast.show();
                        kProgressHUD.dismiss();
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

    private void geoconv(List<LatLng> list) {

        String urlBody = getString(R.string.baiduGeoConvUrl);
        String ak = getString(R.string.baiduGeoConvAppKey);
        RequestParams params = new RequestParams();
        String coords = "";

        int sum = list.size();

        //减少重复点
//        if (list.size() > 100) {
//            list = reducePointByDistance(list);
//            Log.i("Main", sum + "--->" + list.size());
//        }

        //把坐标array转成字符串
        for (LatLng latLng :list) {
            coords += latLng.longitude + "," + latLng.latitude + ";";
        }
        coords = coords.substring(0, coords.length() - 1); //去掉最后一个分号

        //设置参数
        params.put("coords", coords);
        params.put("ak", ak);

        //TODO: 一次最多100个点

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
//                Log.i("Main", response.toString());
                Integer status;
                try {
                    status = response.getInt("status");
                    if (status == 0) {
                        dataString = response.toString();
                        toast.setText("获取成功");
                        toast.show();
                    } else {
                        toast.setText("纠偏失败");
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("纠偏失败");
                    toast.show();
                }
                kProgressHUD.dismiss();
                showDisplayIntent();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                toast.setText("纠偏服务器连接失败");
                toast.show();
                //纠偏失败时显示原来的轨迹
                showDisplayIntent();
            }
        });
    }

    private List<LatLng> reducePointByDistance(List<LatLng> list) {

        Log.i("Main", list.toString());
        int pointNumber = list.size();

        if (pointNumber <= 1) {
            return list;
        }

        Double defaultDistance = 0.0;


        if (reducePointBySize) {
            if (pointNumber > 100 && pointNumber <= 200) {
                defaultDistance = 10.0;
            } else if (pointNumber > 200 && pointNumber <= 500) {
                defaultDistance = 30.0;
            } else if (pointNumber > 500) {
                defaultDistance = 50.0;
            }
        }

        List<LatLng> noDuplicateList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0 && i!= list.size() -1) {
                Double lat1 = list.get(i).latitude;
                Double lat2 = list.get(i - 1).latitude;
                Double lng1 = list.get(i).longitude;
                Double lng2 = list.get(i - 1).longitude;

                Double distance = new PrivateEncode().GetDistance(lat1,lng1,lat2,lng2);

                //去掉重复点
                if (distance != 0.0 && distance > defaultDistance) {
                    noDuplicateList.add(list.get(i));
                }
                Log.i("Main",distance + "");

            } else {
                noDuplicateList.add(list.get(i)); //添加第一个和最后一个点
            }
        }
        return noDuplicateList;
    }

}
