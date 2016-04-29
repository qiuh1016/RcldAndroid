package com.cetcme.rcldandroid;

import android.graphics.drawable.Icon;
import android.media.Image;
import android.media.ImageReader;
import android.support.annotation.BoolRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private BaiduMap baiduMap;

    private Boolean isFullScreen = false;

    private LatLng shipLocation;
    private int topMargin;
    private RelativeLayout.LayoutParams layoutParams;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

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
        baiduMap = mapView.getMap();

        fullScreenImageButton = (ImageButton) findViewById(R.id.fullScreenImageButton);
        showShipLocationImageButton = (ImageButton) findViewById(R.id.showShipLocationImageButton);

        layoutParams = (RelativeLayout.LayoutParams) mapView.getLayoutParams();
        topMargin = layoutParams.topMargin;

        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();

        setTitle("本船信息");

        mapSet();

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
            String ownerTelNo = data0.getString("ownerTelNo");
            Double Lat = data0.getDouble("latitude");
            Double Lng = data0.getDouble("longitude");

            shipNameTextView.setText("  船名：" + shipName);

            shipNumberTextView.setText("  船号：" + shipNumber);
            ownerNameTextView.setText("  船东：" + ownerName);
            ownerTelTextView.setText("  电话：" + ownerTelNo);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

//        MenuItem setting = menu.add(0, 0, 0, "菜单");
//        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        setting.setIcon(R.drawable.menuicon);
//        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                Toast.makeText(getApplicationContext(),"待开发",Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });


        //TODO: 回港 出海
        MenuItem changeInfo = menu.add(0, 0, 0, "修改信息");
        MenuItem iConfirm = menu.add(0, 0, 0, "出海确认");
        MenuItem oConfirm = menu.add(0, 0, 0, "回港确认");
        MenuItem iofLog = menu.add(0, 0, 0, "出海记录");

        changeInfo.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        iConfirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        oConfirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        iofLog.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        changeInfo.setIcon(R.drawable.menuicon);
        changeInfo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(getApplicationContext(),"待开发",Toast.LENGTH_SHORT).show();
                return false;
            }
        });



        return true;
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

    void mapSet() {
        mapView.showZoomControls(true);
        mapView.showScaleControl(true);
    }

    private void mapMark(Double Lat, Double Lng){
        //设置地图范围
        mapStatus(Lat, Lng);

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
        baiduMap.addOverlay(option);

        //创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.drawable.mapinfoview);
        button.setTextSize(15);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0,0,0,20);
        button.setText("设备编号：10000001");
        button.setTextColor(0xFF000000);
        //定义用于显示该InfoWindow的坐标点
        LatLng pt = new LatLng(Lat, Lng);
        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, pt, -75);
        //显示InfoWindow
        baiduMap.showInfoWindow(mInfoWindow);

    }

    private void mapStatus(Double Lat, Double Lng) {
        LatLng point = new LatLng(Lat, Lng);
        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(15)
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

//    private void showUserLocation() {
//        BaiduMap baiduMap = mapView.getMap();
//        // 开启定位图层
//        baiduMap.setMyLocationEnabled(true);
//        // 构造定位数据
//        MyLocationData locData = new MyLocationData.Builder()
//                .accuracy(location.getRadius())
//                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(100).latitude(location.getLatitude())
//                .longitude(location.getLongitude()).build();
//        // 设置定位数据
//        baiduMap.setMyLocationData(locData);
//        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
//                .fromResource(R.drawable.mapmakericon);
//        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
//        baiduMap.setMyLocationConfiguration(config);
//        // 当不需要定位图层时关闭定位图层
//        baiduMap.setMyLocationEnabled(false);
//    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        //option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }






}


