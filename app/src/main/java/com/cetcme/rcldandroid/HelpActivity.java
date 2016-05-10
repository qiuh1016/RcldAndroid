package com.cetcme.rcldandroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

public class HelpActivity extends AppCompatActivity implements View.OnClickListener {

    TextView telTextView;
    TextView addressTextView;
    MapView mapView;
    BaiduMap baiduMap;

    LatLng companyLatlng = new LatLng(29.891853,121.64414);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle("帮助");

        mapView = (MapView) findViewById(R.id.baiduMapInHelpActivity);
        baiduMap = mapView.getMap();


//        TextView location = new TextView(getApplicationContext());
//        location.setBackgroundResource(R.drawable.boder);
//        location.setPadding(15, 15, 8, 35);
//        location.setTextColor(Color.DKGRAY);
//        location.setText("定位时间：");
//        location.setTextSize(12);
//
//        InfoWindow infoWindow = new InfoWindow(location, companyLatlng,0);
//        baiduMap.showInfoWindow(infoWindow);





        telTextView = (TextView) findViewById(R.id.telTextViewInHelpActivity);
        telTextView.setOnClickListener(this);
        telTextView.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG ); //下划线
        telTextView.getPaint().setAntiAlias(true);//抗锯齿

        addressTextView = (TextView) findViewById(R.id.addressTextViewInHelpActivity);
        addressTextView.setOnClickListener(this);

        mapMark(companyLatlng);
//        showInfoWindow();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuItem add = menu.add(0,0,0,"Add");
//        MenuItem del = menu.add(0,0,0,"Del");
//        MenuItem save = menu.add(0,0,0,"save");
//        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        del.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        save.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        add.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                System.out.println("add");
//                return false;
//            }
//        });
//        del.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                System.out.println("del");
//                return false;
//            }
//        });
//        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                System.out.println("save");
//                return false;
//            }
//        });
//
//        return true;
//    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.telTextViewInHelpActivity:
                Log.i("Main", "call");
                //用intent启动拨打电话
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:0574-55712322"));
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
                mapMark(companyLatlng);
                break;

        }
    }

    private void mapMark(LatLng latLng){

        //设置中心点 和显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(15) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);

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
        InfoWindow mInfoWindow = new InfoWindow(textView, point, -75);

        //显示InfoWindow
        baiduMap.showInfoWindow(mInfoWindow);

    }

    boolean isShow = false;

    private void showInfoWindow() {


        //创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
//        button.setBackgroundResource(R.drawable.mapinfoview);
        button.setBackgroundResource(R.drawable.boder);
        button.setBackgroundColor(0x88FFFFFF);
        button.setTextSize(15);
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
        final InfoWindow mInfoWindow = new InfoWindow(textView, companyLatlng, -75);

        //显示InfoWindow



        //bitmap
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.mapmakericon);
        final InfoWindow infoWindow =  new InfoWindow(bitmap, companyLatlng, 0, new InfoWindow.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick() {
                Log.i("Main","tapped");
            }
        });
        baiduMap.showInfoWindow(infoWindow);

        MapStatus mapStatus = new MapStatus.Builder().target(companyLatlng).zoom(15) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);




    }

}
