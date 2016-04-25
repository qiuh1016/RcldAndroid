package com.cetcme.rcldandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

        Bundle bundle = this.getIntent().getExtras();
        String startTime = bundle.getString("startTime");
        String endTime = bundle.getString("endTime");
        showMediaPoint = bundle.getBoolean("showMediaPoint");
        String dataString = bundle.getString("dataString");

        try {
            JSONObject dataJSON = new JSONObject(dataString);
            JSONArray dataArray = dataJSON.getJSONArray("data");
            System.out.println("length ********* " + dataArray.length());
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject data = (JSONObject) dataArray.get(i);
                Double lat = data.getDouble("latitude");
                Double lng = data.getDouble("longitude");
                LatLng latLng = new LatLng(lat, lng);
                latLngs.add(latLng);
            }
            System.out.println(latLngs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
        endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);
        distanceTextView = (TextView) findViewById(R.id.distanceTextView);
        mapView = (MapView) findViewById(R.id.baiduMapInRouteDisplayActivity);
        baiduMap = mapView.getMap();


        startTimeTextView.setText(startTime);
        endTimeTextView.setText(endTime);

        if (showMediaPoint) {
            distanceTextView.setText("showMediaPoint");
        } else {
            distanceTextView.setText("doesn't showMediaPoint");
        }



    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

}
