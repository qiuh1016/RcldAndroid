package com.cetcme.rcldandroidZhejiang;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class FenceMapActivity extends AppCompatActivity {

    private MapView mapView;
    private BaiduMap baiduMap;
    private KProgressHUD kProgressHUD;

    private String fenceNo;
    private String fenceTypeName;
    private String fenceName;

    private int fillColor = 0x77FFFF00;
    private int strokeColor = 0x8800FF00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_map);

        /**
         * 导航栏返回按钮
         */
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mapView = (MapView) findViewById(R.id.baiduMapInFenceMapAtivity);
        baiduMap = mapView.getMap();

        //获取上一个Activity的数据
        Bundle bundle = this.getIntent().getExtras();
        fenceNo = bundle.getString("fenceNo");
        fenceTypeName = bundle.getString("fenceTypeName");
        fenceName = bundle.getString("fenceName");

        setTitle(fenceName);

        kProgressHUD = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("获取中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(110, 110)
                .setCancellable(false);

        //获取港口坐标
        getFenceConstruction();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    private void getFenceConstruction() {
        kProgressHUD.show();

        String username,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        username = user.getString("username","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));

        RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("fenceNo" , fenceNo);

        String urlBody = "http://"+serverIP+ getString(R.string.fenceConstructionUrl);
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());

                Log.i("Main", fenceTypeName);
                if (fenceTypeName.equals("多边形") || fenceTypeName.equals("矩形")) {
                    List<LatLng> fencePts = new ArrayList();
                    try {
                        JSONArray array = response.getJSONArray("data");

                        if (array.length() == 0) {
                            kProgressHUD.dismiss();
                            Toast.makeText(getApplicationContext(), "无数据",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject data = (JSONObject) array.get(i);
                            double bmapLat = data.getDouble("bmapLatitude");
                            double bmapLng = data.getDouble("bmapLongitude");
                            LatLng point = new LatLng(bmapLat, bmapLng);
                            fencePts.add(point);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    drawPolygonFence(fencePts);
                } else if (fenceTypeName.equals("圆形")) {

                    try {
                        JSONObject data = response.getJSONObject("data");

                        double bmapLat = data.getDouble("bmapLatitude");
                        double bmapLng = data.getDouble("bmapLongitude");
                        LatLng point = new LatLng(bmapLat, bmapLng);
                        Log.i("Main", "double:" + data.getDouble("radius"));
                        int radius = (int) data.getDouble("radius");
                        Log.i("Main", "int:" + radius);
                        drawCircleFence(point, radius);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                kProgressHUD.dismiss();
                Toast.makeText(getApplicationContext(), "获取成功",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                Toast.makeText(getApplicationContext(), "获取失败",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                },1000);
            }
        });
    }

    private void drawPolygonFence(List<LatLng> pts) {
        //构建用户绘制多边形的Option对象
        OverlayOptions polygonOption = new PolygonOptions()
                .points(pts)
                .stroke(new Stroke(5, strokeColor))
                .fillColor(fillColor);
        //在地图上添加多边形Option，用于显示
        baiduMap.addOverlay(polygonOption);

        //设置中心点 和 显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(getMediaPoint(pts)).zoom(15) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

    private void drawCircleFence(LatLng latLng, int radius) {
        OverlayOptions circleOption = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .stroke(new Stroke(5, strokeColor))
                .fillColor(fillColor);
        //在地图上添加多边形Option，用于显示
        baiduMap.addOverlay(circleOption);

        //设置中心点 和 显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(15) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

    //        ["50m","100m","200m","500m","1km","2km","5km","10km","20km","25km","50km","100km","200km","500km","1000km","2000km"];
    //          14     13    12      11     10    9    8      7      6      5     4      3       2         1
    //          18                          14         12                         8                       5

    private LatLng getMediaPoint (List<LatLng> pts) {
        int n = pts.size();
        double sumLat = 0;
        double sumLng = 0;
        for (int i = 0; i < n; i++) {
            sumLat += pts.get(i).latitude;
            sumLng += pts.get(i).longitude;
        }
        double mediaLat = sumLat / n;
        double mediaLng = sumLng / n;

        return new LatLng(mediaLat, mediaLng);

    }


}
