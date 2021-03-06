package com.cetcme.rcldandroidZhejiang;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class HelpActivity extends AppCompatActivity implements View.OnClickListener , BaiduMap.OnMarkerClickListener{

    private TextView telTextView;
    private TextView addressTextView;
    private MapView mapView;
    private BaiduMap baiduMap;

    private LatLng companyPosition = new LatLng(30.772614,120.669103);   // 嘉兴120.669103,30.772614   宁波 29.891853,121.64414

    private Marker comMarker;
    private InfoWindow mInfoWindow;
    private Boolean infoWindowIsShow = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_help);
        setTitle("帮助");

        mapView = (MapView) findViewById(R.id.baiduMapInHelpActivity);
        baiduMap = mapView.getMap();
        baiduMap.setOnMarkerClickListener(this);

        telTextView = (TextView) findViewById(R.id.telTextViewInHelpActivity);
        telTextView.setOnClickListener(this);
        telTextView.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
        telTextView.getPaint().setAntiAlias(true);//抗锯齿

        addressTextView = (TextView) findViewById(R.id.addressTextViewInHelpActivity);
        addressTextView.setOnClickListener(this);

        mapMark(companyPosition);
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.telTextViewInHelpActivity:
                //用intent启动拨打电话
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getString(R.string.telephone)));  //"tel:0573-82793269"
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
                break;
            case R.id.addressTextViewInHelpActivity:
                mapMark(companyPosition);
                break;

        }
    }

    private void mapMark(LatLng point){

        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(17) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);

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
        TextView textView = new TextView(getApplicationContext());
        textView.setBackgroundResource(R.drawable.infowindow_white);
        textView.setTextSize(13);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20,10,20,30);
        textView.setText("中电科（宁波）海洋电子研究院有限公司");
        textView.setTextColor(0xFF7D7D7D);
        textView.setGravity(Gravity.CENTER);

        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量(maker 的高度)
        mInfoWindow = new InfoWindow(textView, point, -bitmap.getBitmap().getHeight());

        //显示InfoWindow
        baiduMap.showInfoWindow(mInfoWindow);
        infoWindowIsShow = true;

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

    private void showInfoWindow() {

        //创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
//        button.setBackgroundResource(R.drawable.mapinfoview);
        button.setBackgroundResource(R.drawable.boder);
        button.setBackgroundColor(0x88FFFFFF);
        button.setTextSize(13);
        button.setGravity(Gravity.CENTER);
        button.setPadding(20,20,20,20);
        button.setText("中电科（宁波）海洋电子研究院有限公司");
        button.setTextColor(0xFF7D7D7D);
        button.setGravity(Gravity.CENTER);

        TextView textView = new TextView(getApplicationContext());
        textView.setBackgroundResource(R.drawable.boder);
//        textView.setBackgroundColor(0x88FFFFFF);
        textView.setTextSize(15);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20,20,20,20);
        textView.setText("中电科（宁波）海洋电子研究院有限公司");
        textView.setTextColor(0xFF7D7D7D);
        textView.setGravity(Gravity.CENTER);
        //定义用于显示该InfoWindow的坐标点
//        LatLng pt = new LatLng(Lat, Lng);
        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        final InfoWindow mInfoWindow = new InfoWindow(textView, companyPosition, -75);

        //显示InfoWindow



        //bitmap
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_point);
        final InfoWindow infoWindow =  new InfoWindow(bitmap, companyPosition, 0, new InfoWindow.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick() {
                Log.i("Main","tapped");
            }
        });
        baiduMap.showInfoWindow(infoWindow);

        MapStatus mapStatus = new MapStatus.Builder().target(companyPosition).zoom(15) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);




    }
}
