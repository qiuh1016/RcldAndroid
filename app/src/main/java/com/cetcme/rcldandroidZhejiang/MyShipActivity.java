package com.cetcme.rcldandroidZhejiang;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class MyShipActivity extends AppCompatActivity implements View.OnClickListener, BaiduMap.OnMarkerClickListener{

    private ImageButton showShipLocationImageButton;

    private MapView mapView;
    private BaiduMap baiduMap;

    private LatLng shipLocation;
    private LatLng shipLocationUnConved;
    private String shipInfoString;
    private String picName;
    private String picTelNo;
    private JSONObject myShipInfoJSON;
    private Boolean antiThiefIsOpen = false;
    private int antiThiefRadius;
    private OverlayOptions antiThiefPolygonOption;
    private Boolean isGeoConved = false;

    private MenuItem antiThiefMenuItem;
    private Toast toast;

    private Marker comMarker;
    private InfoWindow mInfoWindow;
    private Boolean infoWindowIsShow = true ;
    private Boolean isFirstToShow = true;

    private ShipLocationReceiver shipLocationReceiver;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private BDLocation bdLocation;

    private int getLocationTime = 30; //秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_myship);
        setTitle("本船信息");

        /**
         * 导航栏返回按钮
         */
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mapView = (MapView) findViewById(R.id.baiduMapInMyShipActivity);
        showShipLocationImageButton = (ImageButton) findViewById(R.id.showShipLocationImageButton);
        showShipLocationImageButton.setOnClickListener(this);

        baiduMap = mapView.getMap();
        baiduMap.setOnMarkerClickListener(this);

        toast =  Toast.makeText(MyShipActivity.this, "", LENGTH_SHORT);

        //mapSet();
        getShipData();

        //读取防盗状态 和 防盗半径
        SharedPreferences antiThief = getSharedPreferences("antiThief", Context.MODE_PRIVATE);
        antiThiefIsOpen = antiThief.getBoolean("antiThiefIsOpen",false);
        modifyAntiThiefRadius();

        //接受广播
        shipLocationReceiver = new ShipLocationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.updateShipLocation");
        registerReceiver(shipLocationReceiver,intentFilter);

        //定位
        /*
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();
        */
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

    private void getShipData() {
        Bundle bundle = this.getIntent().getExtras();
        String str = bundle.getString("myShipInfo");
        try {
            myShipInfoJSON = new JSONObject(str);
            JSONArray data = myShipInfoJSON.getJSONArray("data");
            JSONObject data0 = data.getJSONObject(0);

            String shipName;
            try {
                shipName = data0.getString("shipName");
            } catch (JSONException e) {
                shipName = "无";
            }

//            String shipNumber = data0.getString("shipNo");

            String ownerName;
            try {
                ownerName = data0.getString("ownerName");
            } catch (JSONException e) {
                ownerName = "无";
            }

            String deviceNo;
            try {
                deviceNo = data0.getString("deviceNo");
            } catch (JSONException e) {
                deviceNo = "无";
            }

            String ownerTelNo;
            try {
                ownerTelNo = data0.getString("ownerTelNo");
            } catch (JSONException e) {
                ownerTelNo = "无";
            }

            Boolean offlineFlag;
            try {
                offlineFlag = data0.getBoolean("offlineFlag");
            } catch (JSONException e) {
                offlineFlag = false;
            }

            String cfsStartDate;
            try {
                cfsStartDate = data0.getString("cfsStartDate");
                if (cfsStartDate.equals("1900/01/01")) {
                    cfsStartDate = "无";
                }
            } catch (JSONException e) {
                cfsStartDate = "无";
            }

            String cfsEndDate;
            try {
                cfsEndDate = data0.getString("cfsEndDate");
                if (cfsEndDate.equals("1900/01/01")) {
                    cfsEndDate = "无";
                }
            } catch (JSONException e) {
                cfsEndDate = "无";
            }

            try {
                picName = data0.getString("picName");
                picTelNo = data0.getString("picTelNo");
            } catch (JSONException e) {
                picName = "无";
                picTelNo = "无";
            }


            String atFence;
            String onLine;
            try {
                String fenceNo = data0.getString("fenceNo");
                atFence = "是";
            }  catch (JSONException e) {
                e.printStackTrace();
                atFence = "否";
            }

            if (offlineFlag) {
                onLine = "否";
            } else {
                onLine = "是";
            }

            Double Lat;
            Double Lng;
            try {
                Lat = data0.getDouble("latitude");
            } catch (JSONException e) {
                Lat = 0.0;
            }
            try {
                Lng = data0.getDouble("longitude");
            } catch (JSONException e) {
                Lng = 0.0;
            }

            shipLocation = new LatLng(Lat, Lng);
            shipLocationUnConved = new LatLng(Lat, Lng);

            shipInfoString = shipName + "\n" +
                    "船东：" + ownerName + "\n" +
                    "电话：" + ownerTelNo + "\n" +
                    "负责人：" + picName + "\n" +
                    "电话：" + picTelNo + "\n" +
                    "终端序号：" + deviceNo + "\n" +
                    "是否在线：" + onLine + "\n" +
                    "是否在港：" + atFence + "\n" +
                    "伏休期起始日期：" + cfsStartDate + "\n" +
                    "伏休期结束日期：" + cfsEndDate;

//            mapMark(Lat, Lng);

            //校准
            geoconv(shipLocation);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

//        MenuItem changeInfo = menu.add(0, 0, 0, "修改信息");
        MenuItem oConfirm = menu.add(0, 0, 0, R.string.oConfirm);
        MenuItem iConfirm = menu.add(0, 0, 0, R.string.iConfirm);
        MenuItem punch = menu.add(0, 0, 0, "打卡记录");
        MenuItem iofLog = menu.add(0, 0, 0, "出海记录");
//        antiThiefMenuItem = menu.add(0, 0, 0, antiThiefIsOpen? "关闭防盗" : "开启防盗");
//        final MenuItem helpAlarm = menu.add(0,0,0,"报警求助");

//        changeInfo.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        oConfirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        iConfirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        punch.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        iofLog.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//        antiThiefMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//        helpAlarm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

//        changeInfo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//
//                Bundle bundle = new Bundle();
//                bundle.putString("picName", picName);
//                bundle.putString("picTelNo", picTelNo);
//
//                Intent changeInfoIntent = new Intent();
//                changeInfoIntent.setClass(getApplicationContext(),ChangeInfoActivity.class);
//                changeInfoIntent.putExtras(bundle);
//                startActivity(changeInfoIntent);
//                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
//
//                return false;
//            }
//        });

        iConfirm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundle bundle = new Bundle();
                bundle.putInt("iofFlag", 2);
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ioConfirmActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                return false;
            }
        });

        oConfirm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundle bundle = new Bundle();
                bundle.putInt("iofFlag", 1);
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ioConfirmActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                return false;
            }
        });

        punch.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), PunchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                return false;
            }
        });

        iofLog.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), ioLogActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                return false;
            }
        });

//        antiThiefMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                SharedPreferences antiThief = getSharedPreferences("antiThief", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = antiThief.edit();
//                editor.putBoolean("antiThiefIsOpen", !antiThiefIsOpen);
//                editor.putString("antiThiefLat", String.valueOf(shipLocationUnConved.latitude));
//                editor.putString("antiThiefLng", String.valueOf(shipLocationUnConved.longitude));
//                Log.i("Main", "saved  :" + String.valueOf(shipLocationUnConved.latitude) + "," + String.valueOf(shipLocationUnConved.longitude));
//                editor.apply();
//                antiThiefIsOpen = !antiThiefIsOpen;
//
//                if (antiThiefIsOpen) {
//                    baiduMap.addOverlay(antiThiefPolygonOption);
//                    toast.setText("防盗已开启，防盗半径：" + antiThiefRadius + "海里");
//                } else {
//                    baiduMap.clear();
//                    mapMark(shipLocation);
//                    toast.setText("防盗已关闭");
//                }
//
////                //通知index
////                Intent intent = new Intent();
////                intent.putExtra("antiThiefIsOpen" , antiThiefIsOpen);
////                intent.setAction("com.antiThief");
////                sendBroadcast(intent);
////                toast.show();
//
//                //延时改变菜单内容
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        antiThiefMenuItem.setTitle(antiThiefIsOpen? "关闭防盗" : "开启防盗");
//                    }
//                }, 200);
//
//                return false;
//            }
//        });

//        helpAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (bdLocation != null) {
//                    uploadHelpAlarm();
//                } else {
//                    toast.setText("请等待定位数据");
//                    toast.show();
//                }
//                return false;
//            }
//        });

        return true;
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
        toast.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showShipLocationImageButton:
                mapStatus(shipLocation);
                break;
        }
    }

    public void geoconv(final LatLng latLng) {

        String urlBody = getString(R.string.baiduGeoConvUrl);
        String ak = getString(R.string.baiduGeoConvAppKey);
        RequestParams params = new RequestParams();
        params.put("coords", latLng.longitude + "," + latLng.latitude);
        params.put("ak", ak);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main",response.toString());
                baiduMap.clear();
                Integer status;
                try {
                    status = response.getInt("status");
                    if (status == 0) {
                        Log.i("Main","geoconv OK");
                        JSONArray result = response.getJSONArray("result");
                        JSONObject result0 = result.getJSONObject(0);

                        Double latconv = result0.getDouble("y");
                        Double lngconv = result0.getDouble("x");

                        shipLocation = new LatLng(latconv, lngconv);
                        mapMark(shipLocation);
                        isGeoConved = true;
                        setAntiThiefCircle(shipLocation,antiThiefRadius);

                    } else {
                        String message = response.getString("message");
                        toast.setText(message);
                        toast.show();
                        mapMark(shipLocation);
                        setAntiThiefCircle(shipLocation,antiThiefRadius);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("纠偏失败");
                    toast.show();

                    mapMark(shipLocation);
                    setAntiThiefCircle(shipLocation,antiThiefRadius);
                }

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                toast.setText("纠偏失败");
                toast.show();

                mapMark(shipLocation);
                setAntiThiefCircle(shipLocation,antiThiefRadius);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        MobclickAgent.onResume(this);
        baiduMap.clear();
        //修改半径后重新绘制
        modifyAntiThiefRadius();

        if (isGeoConved) {
            mapMark(shipLocation);
            setAntiThiefCircle(shipLocation,antiThiefRadius);
        }

        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        //mapView.onResume();
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(shipLocationReceiver);
        super.onDestroy();
//        mLocationClient.stop();
    }

    private void mapSet() {
        mapView.showZoomControls(true);
        mapView.showScaleControl(true);
    }

    private void mapMark(LatLng latLng){

        Log.i("Main","mapMark");

        if (isFirstToShow) {
            //设置地图范围
            mapStatus(latLng);
            isFirstToShow = false; //位置更新后地图范围不变
        }

        //定义Maker坐标点
        LatLng point = latLng;
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_point);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        comMarker = (Marker) baiduMap.addOverlay(option);

        //创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.drawable.infowindow_white);
        button.setTextSize(13);
        button.setGravity(Gravity.CENTER);
        button.setPadding(20,20,20,40);
        button.setText(shipInfoString);
        button.setTextColor(0xFF7D7D7D);
        button.setGravity(Gravity.LEFT);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baiduMap.hideInfoWindow();
                infoWindowIsShow = false;
            }
        });

        if (infoWindowIsShow) {
            //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
            mInfoWindow = new InfoWindow(button, point, -bitmap.getBitmap().getHeight());

            //显示InfoWindow
            baiduMap.showInfoWindow(mInfoWindow);
            infoWindowIsShow = true;
        }


    }

    private void mapStatus(LatLng latLng) {
        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(9) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

    private void modifyAntiThiefRadius() {
        //读取防盗半径，如果没有定义就设为1海里
        SharedPreferences antiThief = getSharedPreferences("antiThief", Context.MODE_PRIVATE);
        antiThiefRadius =antiThief.getInt("antiThiefRadius" , 1);
//        if (antiThiefRadius.isEmpty()) {
//            SharedPreferences.Editor editor = antiThief.edit();
//            editor.putInt("antiThiefRadius", 1);
//            editor.apply();
//            antiThiefRadius = antiThief.getString("antiThiefRadius","");
//            Log.i("Main","Default antiThiefRadius set");
//        }
    }

    private void setAntiThiefCircle(LatLng latLng, int antiThiefRadius) {
        antiThiefPolygonOption = new CircleOptions()
                .center(latLng)
                .radius(antiThiefRadius * 1852)
                .stroke(new Stroke(2, 0xAA167CF3))
                .fillColor(0x332884EF);
        //在地图上添加多边形Option，用于显示
        if (antiThiefIsOpen) {
            baiduMap.addOverlay(antiThiefPolygonOption);
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(comMarker)) {
            if (infoWindowIsShow) {
                baiduMap.hideInfoWindow();
            } else {
                baiduMap.showInfoWindow(mInfoWindow);
            }
            infoWindowIsShow = !infoWindowIsShow;
        }

        return false;
    }

    public class ShipLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Bundle bundle = arg1.getExtras();
            Double lat = bundle.getDouble("lat");
            Double lng = bundle.getDouble("lng");
            shipLocationUnConved = new LatLng(lat, lng);
            geoconv(shipLocationUnConved);
        }
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = getLocationTime * 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            bdLocation = location;
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlongitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }

    }

    private void uploadHelpAlarm() {
        String username,password,serverIP,deviceNo,shipNo;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        username = user.getString("username","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));
        deviceNo = user.getString("deviceNo","");
        shipNo   = user.getString("shipNo","");

        //设置参数
        final RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("deviceNo", deviceNo);
        params.put("shipNo", shipNo);
        params.put("alertType", 1);
        params.put("longitude", bdLocation.getLongitude());
        params.put("latitude", bdLocation.getLatitude());
        params.put("time", "2016/06/22%2013:22:22"); //bdLocation.getTime()
        params.put("description", "报警求助");

        Log.i("Main", "报警内容：" + params.toString());

        String urlBody = "http://"+serverIP+ getString(R.string.helpAlarmUrl);
        AsyncHttpClient client = new AsyncHttpClient();
        client.setURLEncodingEnabled(true);
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                toast.setText("报警成功");
                toast.show();

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                toast.setText("报警失败");
                toast.show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.i("Main", response);
                toast.setText("网络连接失败");
                toast.show();
            }
        });

    }

}


