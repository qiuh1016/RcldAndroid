package com.cetcme.rcldandroidZhejiang;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

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

    private LatLng maxPoint;
    private LatLng minPoint;
    private LatLng mediaPoint;
    private Double distance;

    private Boolean isConverted;

    private int maxMediaPointMarkerNum = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_route_display);
        setTitle("轨迹显示");

        /**
         * 导航栏返回按钮
         */
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /**
         * umeng 推送
         */
        PushAgent.getInstance(this).onAppStart();

        startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
        endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        mapView = (MapView) findViewById(R.id.baiduMapInRouteDisplayActivity);
        baiduMap = mapView.getMap();
        baiduMap.clear();

        //获取上个Activity给的数据
        Bundle bundle = this.getIntent().getExtras();
        String startTime = bundle.getString("startTime");
        String endTime = bundle.getString("endTime");
        showMediaPoint = bundle.getBoolean("showMediaPoint");
        String dataString = bundle.getString("dataString");
        String totalRange = bundle.getString("totalRange");

        ArrayList<String> convedList = bundle.getStringArrayList("convedList");
        boolean geoOK = bundle.getBoolean("geoOK");

        startTimeTextView.setText(startTime);
        endTimeTextView.setText(endTime);

        if (totalRange.length() > 5) {
            distanceTextView.setText(totalRange.substring(4));
        } else {
            distanceTextView.setText(totalRange);
        }


        if (geoOK) {

            for (String convedListString : convedList) {
                try {
//                    Log.i("Main", convedListString);
                    JSONObject dataJSON = new JSONObject(convedListString);
                    JSONArray resultArray = dataJSON.getJSONArray("result");

                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject data = (JSONObject) resultArray.get(i);
                        Double lat = data.getDouble("y");
                        Double lng = data.getDouble("x");
                        LatLng latLng = new LatLng(lat, lng);
                        latLngs.add(latLng);
                    }
                    isConverted = true;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        } else {
            try {
                JSONObject dataJSON = new JSONObject(dataString);
                JSONArray resultArray = dataJSON.getJSONArray("data");

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject data = (JSONObject) resultArray.get(i);
                    Double lat = data.getDouble("latitude");
                    Double lng = data.getDouble("longitude");
                    LatLng latLng = new LatLng(lat, lng);
                    latLngs.add(latLng);
                    latLngs.add(latLng);
                    latLngs.add(latLng);
                }
                isConverted = false;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        UIOperation();

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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

    public void drawRoute(List<LatLng> points) {

        //构建分段颜色索引数组
        List<Integer> colors = new ArrayList<>();
        if (isConverted) {
//            colors.add(R.color.colorRouteConved);
            colors.add(0xFF167CF3); //纠偏成功显示蓝色
        } else {
//            colors.add(R.color.colorRouteunConved);
            colors.add(0xFFE27575); //纠偏失败显示红色
        }

        OverlayOptions ooPolyline = new PolylineOptions()
                .points(points)
                .width(5)
                .colorsValues(colors);
        //添加在地图中
        baiduMap.addOverlay(ooPolyline);

        //TODO: 根据坐标list设置中心点和 范围   完成 需要多测试
        //设置中心点 和 显示范围
        MapStatus mapStatus = new MapStatus.Builder().target(mediaPoint).zoom(zoomLevel(distance)) //15
                .build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                .newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);


        //起点终点标注
        //构建Marker图标
        BitmapDescriptor startBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_start);
        BitmapDescriptor endBitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_end);
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

        //showMediaPoint
        if (showMediaPoint && latLngs.size() < maxMediaPointMarkerNum) {

            BitmapDescriptor mediaBitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_point);
//                        .fromResource(android.R.drawable.ic_notification_overlay);
            for (int i = 1; i < latLngs.size() - 1; i++) {
                OverlayOptions mediaMaker = new MarkerOptions()
                        .position(latLngs.get(i))
                        .icon(mediaBitmap);
                baiduMap.addOverlay(mediaMaker);
            }
        }

    }

    private void getMediaPoint(List<LatLng> points) {
        Double minLat = points.get(0).latitude;
        Double maxLat = points.get(0).latitude;
        Double minLng = points.get(0).longitude;
        Double maxLng = points.get(0).longitude;

        for (LatLng point : points) {
            if (point.latitude > maxLat) {
                maxLat = point.latitude;
            } else if (point.latitude < minLat) {
                minLat = point.latitude;
            }

            if (point.longitude > maxLng) {
                maxLng = point.longitude;
            } else if (point.longitude < minLng) {
                minLng = point.longitude;
            }
        }

        minPoint = new LatLng(minLat,minLng);
        maxPoint = new LatLng(maxLat,maxLng);
        mediaPoint = new LatLng((maxLat + minLat)/2 , (maxLng + minLng)/2);

        distance = new PrivateEncode().GetDistance(maxLat,maxLng,minLat,minLng);

        List<LatLng> list = new LinkedList<>();
        list.add(minPoint);
        list.add(mediaPoint);
        list.add(maxPoint);

    }

    private int zoomLevel(Double d) {

        int zoomLevel = 14;
        int i = 20;
//        if (d < 50 * i) {
//            zoomLevel = 18;
//            return zoomLevel;
//        }
        if (d < 100 * i) {
            zoomLevel = 17;
            return zoomLevel;
        }
        if (d < 200 * i) {
            zoomLevel = 16;
            return zoomLevel;
        }
        if (d < 500 * i) {
            zoomLevel = 15;
            return zoomLevel;
        }
        if (d < 1000 * i) {
            zoomLevel = 14;
            return zoomLevel;
        }
        if (d < 2000 * i) {
            zoomLevel = 13;
            return zoomLevel;
        }
        if (d < 5000 * i) {
            zoomLevel = 12;
            return zoomLevel;
        }
        if (d < 10000 * i) {
            zoomLevel = 11;
            return zoomLevel;
        }
        if (d < 20000 * i) {
            zoomLevel = 10;
            return zoomLevel;
        }
        return zoomLevel;

        //        ["50m","100m","200m","500m","1km","2km","5km","10km","20km","25km","50km","100km","200km","500km","1000km","2000km"];
        //          14     13    12      11     10    9    8      7      6      5     4      3       2         1
        //          18                          14         12                         8                       5
    }

    private void UIOperation() {
        //如果只有一个点 画标注 多于一个点 则画轨迹
        if (latLngs.size() == 1) {

            MapStatus mapStatus = new MapStatus.Builder().target(latLngs.get(0)).zoom(15) //15
                    .build();
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                    .newMapStatus(mapStatus);
            baiduMap.setMapStatus(mapStatusUpdate);

            BitmapDescriptor endBitmap = BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_end);
            //构建MarkerOption，用于在地图上添加Marker
            OverlayOptions endMaker = new MarkerOptions()
                    .position(latLngs.get(0))
                    .icon(endBitmap);
            baiduMap.addOverlay(endMaker);

        } else {
            //先计算中间点 和 地图level 再画路径
            getMediaPoint(latLngs);
            drawRoute(latLngs);
        }

//        distanceTextView.setText("共" + latLngs.size() + "个点");
    }

}
