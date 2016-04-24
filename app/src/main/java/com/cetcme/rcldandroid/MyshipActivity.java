package com.cetcme.rcldandroid;

import android.renderscript.Double2;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
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

    private MapView mapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_myship);

        shipNameTextView = (TextView) findViewById(R.id.shipNameTextViewOnMyShipActivity);
        shipNumberTextView = (TextView) findViewById(R.id.shipNumberTextViewOnMyShipActivity);
        ownerNameTextView = (TextView) findViewById(R.id.ownerNameTextViewOnMyShipActivity);
        ownerTelTextView = (TextView) findViewById(R.id.ownerTelTextViewOnMyShipActivity);
        deviceNoTextView = (TextView) findViewById(R.id.deviceNumberTextViewOnMyShipActivity);
        mapView = (MapView) findViewById(R.id.baiduMapInMyShipActivity);

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

            mapMark(Lat, Lng);

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    public void mapMark(Double Lat, Double Lng){
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

        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(15)
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }
}


