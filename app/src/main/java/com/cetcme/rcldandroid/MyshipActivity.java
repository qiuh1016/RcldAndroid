package com.cetcme.rcldandroid;

import android.graphics.drawable.Icon;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.BoolRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyshipActivity extends AppCompatActivity {

    private JSONObject myShipInfoJSON;

    private TextView shipNameTextView;
    private TextView shipNumberTextView;
    private TextView ownerNameTextView;
    private TextView ownerTelTextView;
    private TextView deviceNoTextView;

    private ImageButton fullScreenImageButton;
    private ImageButton showShipLocationImageButton;

    private MapView mapView;

    private Boolean isFullScreen = false;

    private LatLng shipLocation;
    private int topMargin;
    private RelativeLayout.LayoutParams layoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_myship);

        shipNameTextView = (TextView) findViewById(R.id.shipNameTextViewOnMyShipActivity);
        shipNumberTextView = (TextView) findViewById(R.id.shipNumberTextViewOnMyShipActivity);
        ownerNameTextView = (TextView) findViewById(R.id.ownerNameTextViewOnMyShipActivity);
        ownerTelTextView = (TextView) findViewById(R.id.ownerTelTextViewOnMyShipActivity);
        deviceNoTextView = (TextView) findViewById(R.id.deviceNumberTextViewOnMyShipActivity);
        mapView = (MapView) findViewById(R.id.baiduMapInMyShipActivity);

        fullScreenImageButton = (ImageButton) findViewById(R.id.fullScreenImageButton);
        showShipLocationImageButton = (ImageButton) findViewById(R.id.showShipLocationImageButton);

        layoutParams = (RelativeLayout.LayoutParams) mapView.getLayoutParams();
        topMargin = layoutParams.topMargin;

        setTitle("本船信息");

        Bundle bundle = this.getIntent().getExtras();
        String str = bundle.getString("myShipInfo");

        try {
            myShipInfoJSON = new JSONObject(str);
            JSONArray data = myShipInfoJSON.getJSONArray("data");
            JSONObject data0 = data.getJSONObject(0);

            String shipName = data0.getString("shipName");
            String shipNumber = data0.getString("shipNo");
            String ownerName = data0.getString("ownerName");
            String deviceNo = data0.getString("deviceNo");
            String owerTel = data0.getString("ownerTelNo");
            Double Lat = data0.getDouble("latitude");
            Double Lng = data0.getDouble("longitude");

            shipNameTextView.setText("  船名：" + shipName);

            shipNumberTextView.setText("  船号：" + shipNumber);
            ownerNameTextView.setText("  船东：" + ownerName);
            ownerTelTextView.setText("  电话：" + owerTel);
            deviceNoTextView.setText("  终端序号：" + deviceNo);

            shipLocation = new LatLng(Lat, Lng);

            mapMark(Lat, Lng);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        fullScreenImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFullScreen) {
                    layoutParams.topMargin = topMargin;
                    fullScreenImageButton.setImageResource(R.drawable.fullsreen);
                } else {
                    layoutParams.topMargin = 0;
                    fullScreenImageButton.setImageResource(R.drawable.unfullsreen);
                }
                isFullScreen = !isFullScreen;

            }
        });

        showShipLocationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapStatus(shipLocation.latitude, shipLocation.longitude);
            }
        });


    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//        mapView.onDestroy();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
//        mapView.onResume();
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        mapView.onPause();
//    }

    private void mapMark(Double Lat, Double Lng){
        //定义Maker坐标点
        LatLng point = new LatLng(Lat, Lng);
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.mapmakericon);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        BaiduMap baiduMap = mapView.getMap();
        baiduMap.addOverlay(option);

        mapStatus(Lat, Lng);

    }

    private void mapStatus(Double Lat, Double Lng) {
        LatLng point = new LatLng(Lat, Lng);
        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(15)
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        BaiduMap baiduMap = mapView.getMap();
        baiduMap.setMapStatus(mapStatusUpdate);
    }

//    private void getUserLocation() {
//        BaiduMap baiduMap = mapView.getMap();
//        // 开启定位图层
//        baiduMap.setMyLocationEnabled(true);
//// 构造定位数据
//        MyLocationData locData = new MyLocationData.Builder()
//                .accuracy(location.getRadius())
//                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(100).latitude(location.getLatitude())
//                .longitude(location.getLongitude()).build();
//// 设置定位数据
//        baiduMap.setMyLocationData(locData);
//// 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.mapmakericon);
//        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
//        baiduMap.setMyLocationConfiguration(config);
//// 当不需要定位图层时关闭定位图层
//        baiduMap.setMyLocationEnabled(false);
//    }
}


