package com.cetcme.rcldandroid;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RouteDisplayActivity extends AppCompatActivity {

    private TextView startTimeTextView;
    private TextView endTimeTextView;
    private TextView distanceTextView;
    private MapView mapView;
    private BaiduMap baiduMap;

    private Boolean showMediaPoint;

    private List<LatLng> latLngs = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_display);
        setTitle("轨迹显示");

        startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
        endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        mapView = (MapView) findViewById(R.id.baiduMapInRouteDisplayActivity);
        baiduMap = mapView.getMap();
        baiduMap.clear();

        Bundle bundle = this.getIntent().getExtras();
        String startTime = bundle.getString("startTime");
        String endTime = bundle.getString("endTime");
        showMediaPoint = bundle.getBoolean("showMediaPoint");
        String dataString = bundle.getString("dataString");

        startTimeTextView.setText(startTime);
        endTimeTextView.setText(endTime);

//        if (showMediaPoint) {
//            distanceTextView.setText("showMediaPoint");
//        } else {
//            distanceTextView.setText("doesn't showMediaPoint");
//        }


        try {
            JSONObject dataJSON = new JSONObject(dataString);
            JSONArray resultArray = dataJSON.getJSONArray("result");

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject data = (JSONObject) resultArray.get(i);
                Double lat = data.getDouble("y");
                Double lng = data.getDouble("x");
                LatLng latLng = new LatLng(lat, lng);
                latLngs.add(latLng);

            }

            //如果只有一个点 画标注 多于一个点 则画轨迹
            if (latLngs.size() == 1) {

            } else {
                drawRoute(latLngs);
            }

            distanceTextView.setText("共" + latLngs.size() + "个点");

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        baiduMap.clear();
//        Log.i("Main", "onDestroy");
//
//    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    public void drawRoute(List<LatLng> points) {

        //构建分段颜色索引数组
        List<Integer> colors = new ArrayList<>();
        colors.add(0xAA167CF3);


        OverlayOptions ooPolyline = new PolylineOptions()
                .points(points)
                .width(5)
                .colorsValues(colors);
        //添加在地图中
        baiduMap.addOverlay(ooPolyline);

        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(points.get(0)).zoom(14) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);


        //起点终点标注
        //构建Marker图标
        BitmapDescriptor startBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.routestarticon);
        BitmapDescriptor endBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.routeendicon);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions startMaker = new MarkerOptions()
                .position(latLngs.get(0))
                .icon(startBitmap);
        OverlayOptions endMaker = new MarkerOptions()
                .position(latLngs.get(latLngs.size() - 1))
                .icon(endBitmap);
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(startMaker);
        baiduMap.addOverlay(endMaker);

    }

}
