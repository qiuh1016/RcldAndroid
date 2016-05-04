package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class MyshipActivity extends AppCompatActivity implements View.OnClickListener{

    private JSONObject myShipInfoJSON;

    private ImageButton showShipLocationImageButton;

    private MapView mapView;
    private BaiduMap baiduMap;

    private LatLng shipLocation;

    String shipInfoString;

    String picName;
    String picTelNo;

    Boolean antiThiefIsOpen = false;
    String antiThiefRadius;
    OverlayOptions antiThiefPolygonOption;

    MenuItem antiThiefMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myship);

        setTitle("本船信息");

        mapView = (MapView) findViewById(R.id.baiduMapInMyShipActivity);

        showShipLocationImageButton = (ImageButton) findViewById(R.id.showShipLocationImageButton);

        showShipLocationImageButton.setOnClickListener(this);

        baiduMap = mapView.getMap();

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
            Boolean offlineFlag = data0.getBoolean("offlineFlag");
            String cfsStartDate = data0.getString("cfsStartDate");
            String cfsEndDate = data0.getString("cfsEndDate");
            picName = data0.getString("picName");
            picTelNo = data0.getString("picTelNo");

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
                onLine = "是";
            } else {
                onLine = "否";
            }

            Double Lat = data0.getDouble("latitude");
            Double Lng = data0.getDouble("longitude");

            shipLocation = new LatLng(Lat, Lng);

            shipInfoString = shipName + "\n" +
                    "船东：" + ownerName + "\n" +
                    "电话：" + ownerTelNo + "\n" +
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

        //读取防盗状态
        SharedPreferences antiThief = getSharedPreferences("antiThief", Activity.MODE_PRIVATE);
        antiThiefIsOpen = antiThief.getBoolean("antiThiefIsOpen",false);

        modifyAntiTHiefRadius();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem changeInfo = menu.add(0, 0, 0, "修改信息");
        MenuItem iConfirm = menu.add(0, 0, 0, "出海确认");
        MenuItem oConfirm = menu.add(0, 0, 0, "回港确认");
        MenuItem iofLog = menu.add(0, 0, 0, "出海记录");
        antiThiefMenuItem = menu.add(0, 0, 0, antiThiefIsOpen? "关闭防盗" : "开启防盗");

        changeInfo.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        iConfirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        oConfirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        iofLog.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        antiThiefMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        changeInfo.setIcon(R.drawable.menuicon);
        changeInfo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                Bundle bundle = new Bundle();
                bundle.putString("picName", picName);
                bundle.putString("picTelNo", picTelNo);

                Intent changeInfoIntent = new Intent();
                changeInfoIntent.setClass(getApplicationContext(),ChangeInfoActivity.class);
                changeInfoIntent.putExtras(bundle);
                startActivity(changeInfoIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);

                return false;
            }
        });

        antiThiefMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences antiThief = getSharedPreferences("antiThief", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = antiThief.edit();
                editor.putBoolean("antiThiefIsOpen", !antiThiefIsOpen);
                editor.apply();
                antiThiefIsOpen = !antiThiefIsOpen;

                if (antiThiefIsOpen) {
                    baiduMap.addOverlay(antiThiefPolygonOption);
                } else {
                    baiduMap.clear();
                    mapMark(shipLocation);
                }

                //延时改变菜单内容
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        antiThiefMenuItem.setTitle(antiThiefIsOpen? "关闭防盗" : "开启防盗");
                    }
                }, 200);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showShipLocationImageButton:
                mapStatus(shipLocation);
                break;
        }
    }

    public void geoconv(final LatLng latLng) {

        String urlBody = "http://api.map.baidu.com/geoconv/v1/";
        String ak = "stfZ8nXV0rvMfTLuAAY9SX2AqgLGLuOQ";
        RequestParams params = new RequestParams();
        params.put("coords", latLng.longitude + "," + latLng.latitude);
        params.put("ak", ak);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSONObject",response.toString());
                Integer status;
                try {
                    status = response.getInt("status");
                    if (status == 0) {
                        JSONArray result = response.getJSONArray("result");
                        JSONObject result0 = result.getJSONObject(0);

                        Double latconv = result0.getDouble("y");
                        Double lngconv = result0.getDouble("x");

                        shipLocation = new LatLng(latconv, lngconv);
                        mapMark(shipLocation);
                        setAntiThiefCircle(shipLocation,antiThiefRadius);

                    } else {
                        String message = response.getString("message");
                        Toast.makeText(getApplicationContext(), message, LENGTH_SHORT).show();
                        mapMark(shipLocation);
                        setAntiThiefCircle(shipLocation,antiThiefRadius);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)

            }
        });
    }

//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//        mapView.onDestroy();
//    }
//
    @Override
    protected void onResume() {
        super.onResume();
//        baiduMap.clear();
        //TODO: 修改半径后重新绘制
        modifyAntiTHiefRadius();
        setAntiThiefCircle(shipLocation,antiThiefRadius);
        //mapMark(shipLocation);

        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        //mapView.onResume();
    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
//        mapView.onPause();
//    }

    private void mapSet() {
        mapView.showZoomControls(true);
        mapView.showScaleControl(true);
    }

    private void mapMark(LatLng latLng){
        //设置地图范围
        mapStatus(latLng);

        //定义Maker坐标点
        LatLng point = latLng;
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
//        button.setBackgroundResource(R.drawable.mapinfoview);
        button.setBackgroundResource(R.drawable.boder);
        button.setBackgroundColor(0x88FFFFFF);
        button.setTextSize(15);
        button.setGravity(Gravity.CENTER);
        button.setPadding(20,20,20,20);
        button.setText(shipInfoString);
        button.setTextColor(0xFF7D7D7D);
        button.setGravity(Gravity.LEFT);
        //定义用于显示该InfoWindow的坐标点
//        LatLng pt = new LatLng(Lat, Lng);
        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, point, -75);

        //显示InfoWindow
        baiduMap.showInfoWindow(mInfoWindow);

    }

    private void mapStatus(LatLng latLng) {
        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(15)
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);
    }

    void modifyAntiTHiefRadius() {
        //读取防盗半径，如果没有定义就设为1海里
        SharedPreferences antiThief = getSharedPreferences("antiThief", Activity.MODE_PRIVATE);
        antiThiefRadius = antiThief.getString("antiThiefRadius","");
        if (antiThiefRadius.isEmpty()) {
            SharedPreferences.Editor editor = antiThief.edit();
            editor.putString("antiThiefRadius", "1");
            editor.apply();
            antiThiefRadius = antiThief.getString("antiThiefRadius","");
            Log.i("Main","Default antiThiefRadius set");
        }
    }

    void setAntiThiefCircle(LatLng latLng, String antiThiefRadius) {
        antiThiefPolygonOption = new CircleOptions()
                .center(latLng)
                .radius(Integer.parseInt(antiThiefRadius) * 1852)
                .stroke(new Stroke(3, 0xAA167CF3))
                .fillColor(0x552884EF);
        //在地图上添加多边形Option，用于显示
        if (antiThiefIsOpen) {
            baiduMap.addOverlay(antiThiefPolygonOption);
        }

    }

}


