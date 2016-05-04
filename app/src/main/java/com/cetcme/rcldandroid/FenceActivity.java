package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class FenceActivity extends AppCompatActivity {

    private ListView fenceListView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList;
    private KProgressHUD kProgressHUD;

    private Toast toast;
    Boolean refreshEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence);
        fenceListView = (ListView) findViewById(R.id.fenceListView);

        setTitle("港口信息");
        toast = Toast.makeText(FenceActivity.this, "获取成功!", LENGTH_SHORT);

        simpleAdapter = new SimpleAdapter(this, getFenceData(), R.layout.fencelistview,
                new String[]{"fenceName", "bowei", "fenceID", "shipNumber", "fenceType"},
                new int[]{
                        R.id.fenceNameTextViewInFenceListView,
                        R.id.boweiTextViewInFenceListView,
                        R.id.fenceIDTextViewInFenceListView,
                        R.id.shipNumberTextViewInFenceListView,
                        R.id.fenceTypeTextViewInFenceListView});

//        simpleAdapter = new SimpleAdapter(this, getFenceData(), R.layout.fencelistviewsingleline,
//                new String[]{"fenceName", "bowei"},
//                new int[]{R.id.fenceNameTextViewInFenceListViewSingleLine,
//                        R.id.boweiTextViewInFenceListViewSingleLine});

        fenceListView.setAdapter(simpleAdapter);

        fenceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> map = dataList.get(i);
                String fenceName = (String) map.get("fenceName");
                fenceName = fenceName.replace("港口名：", "");
                String fenceID = (String) map.get("fenceID");
                Integer shipNumber = (Integer) map.get("shipNumber");
                String fenceType = (String) map.get("fenceType");
                fenceInfodialog(fenceName, fenceID, shipNumber, fenceType);
            }
        });

    }

    //TODO: 点击扩展Cell 显示详细内容
    //TODO: 点击显示港口地图
    //TODO: 下拉刷新

    //TODO: 泊位api更改 拿到下面一栏
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem setting = menu.add(0, 0, 0, "刷新");
        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        setting.setIcon(R.drawable.refresh1);
        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //toast结束之前 不许刷新
                if (refreshEnable) {
                    getFenceData();
                }
                return false;
            }
        });

        return true;
    }

    void ReadSharedPreferences(){
        String strName,strPassword;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        strName = user.getString("shipNumber","");
        strPassword = user.getString("password","");
        System.out.println("********" + strName + ":" + strPassword);
    }

    public void onBackPressed() {
        toast.cancel();
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    private List<Map<String, Object>> getFenceData() {

        toast.cancel();
        refreshEnable = false;
        kProgressHUD = KProgressHUD.create(FenceActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("获取中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(110, 110)
                .show();

        String shipNumber,password;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");

        dataList = new ArrayList<>();

        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        String urlBody = "http://120.27.149.252/api/app/fence/all.json";
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSONObject", response.toString());
                try {
                    JSONArray dataArray = response.getJSONArray("data");

                    for(int i = 0; i < dataArray.length(); i++) {
                        JSONObject fence = (JSONObject) dataArray.get(i);
                        Map<String, Object> map = new Hashtable<>();
                        map.put("fenceName", "港口名：" + fence.get("fenceName"));
                        Integer bowei = (Integer) fence.get("shipAmount");
                        bowei = bowei * 10;
                        map.put("bowei", "泊位：" + bowei + "%");
                        map.put("fenceID", fence.get("fenceNo"));
                        map.put("shipNumber", fence.get("shipAmount"));
                        map.put("fenceType", fence.get("fenceLevel"));
                        dataList.add(map);
                    }
                    simpleAdapter.notifyDataSetChanged();

                    toast.setText("获取成功");
                    toast.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        kProgressHUD.dismiss();
                    }
                }, 300);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshEnable = true;
                    }
                }, 2000);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                toast.setText("网络连接失败");
                toast.show();
            }
        });

        return dataList;
    }

    protected void fenceInfodialog(String fenceName, String fenceID, Integer shipNumber, String fenceType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FenceActivity.this);
        builder.setMessage("港口ID：" + fenceID + "\n" +  "港内船数：" + shipNumber + "\n" + "港口类型：" + fenceType);
        builder.setTitle(fenceName);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
}
