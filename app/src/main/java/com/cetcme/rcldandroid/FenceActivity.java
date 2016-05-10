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

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
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

    private PullToRefreshListView fenceListView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();
    private KProgressHUD kProgressHUD;

    private Toast toast;
//    Boolean refreshEnable = true;
    Boolean isFirstTimeToGet = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence);

        setTitle("港口信息");
        toast = Toast.makeText(FenceActivity.this, "获取成功!", LENGTH_SHORT);

        fenceListView = (PullToRefreshListView) findViewById(R.id.fenceListView);
        fenceListView.getLoadingLayoutProxy(true,false).setRefreshingLabel("刷新中");
        fenceListView.getLoadingLayoutProxy(true,false).setReleaseLabel("松开立即刷新");
        fenceListView.getLoadingLayoutProxy(true,false).setPullLabel("下拉可以刷新");

        simpleAdapter = new SimpleAdapter(this, getFenceData(), R.layout.fencelistview,
                new String[]{"fenceName", "berthAmount", "fenceNo", "inShipAmount", "fenceLevel"},
                new int[]{
                        R.id.fenceNameTextViewInFenceListView,
                        R.id.boweiTextViewInFenceListView,
                        R.id.fenceIDTextViewInFenceListView,
                        R.id.shipNumberTextViewInFenceListView,
                        R.id.fenceTypeTextViewInFenceListView});

        fenceListView.setAdapter(simpleAdapter);

        fenceListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getFenceData();
                    }
                },1000);
            }
        });

        fenceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Map<String, Object> map = dataList.get(i);
//                String fenceName = (String) map.get("fenceName");
//                String city = (String) map.get("city");
//                String country = (String) map.get("country");
//                String fenceAddr = (String) map.get("fenceAddr");
//                String fenceTypeName = (String) map.get("fenceTypeName");
//                int fenceType = (int) map.get("fenceType");
//
//                fenceInfodialog(fenceName, city, country, fenceAddr, fenceType, fenceTypeName);
            }
        });

    }

    //TODO: 点击扩展Cell 显示详细内容
    //TODO: 点击显示港口地图

    //TODO: 泊位api更改 拿到下面一栏
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//
//        MenuItem setting = menu.add(0, 0, 0, "刷新");
//        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        setting.setIcon(R.drawable.refresh1);
//        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                //toast结束之前 不许刷新
//                if (refreshEnable) {
//                    getFenceData();
//                }
//                return false;
//            }
//        });
//
//        return true;
//    }

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
//        refreshEnable = false;
        if (isFirstTimeToGet) {
            kProgressHUD = KProgressHUD.create(FenceActivity.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("获取中")
                    .setAnimationSpeed(1)
                    .setDimAmount(0.3f)
                    .setSize(110, 110)
                    .setCancellable(false)
                    .show();
        }


        final String shipNumber,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");

        dataList.clear();

        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", new PrivateEncode().b64_md5(password));
        String urlBody = "http://"+serverIP+"/api/app/fence/all.json";
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                try {
                    //TODO: 120.27.149.252 服务器更新后换成新的
                    if (serverIP.equals("120.27.149.252") && false) {
                        //原
                        JSONArray dataArray = response.getJSONArray("data");
                        for(int i = 0; i < dataArray.length(); i++) {
                            JSONObject fence = (JSONObject) dataArray.get(i);
                            Map<String, Object> map = new Hashtable<>();
                            map.put("fenceName", "港口名：" + fence.get("fenceName"));
                            Integer berthAmount = (Integer) fence.get("shipAmount");
                            berthAmount = berthAmount * 10;
                            map.put("berthAmount", "泊位：" + berthAmount + "%");
                            map.put("fenceNo", fence.get("fenceNo"));
                            map.put("inShipAmount", fence.get("inShipAmount"));
                            map.put("fenceType", fence.get("fenceLevel"));
                            dataList.add(map);
                        }
                        simpleAdapter.notifyDataSetChanged();
                    } else {
                        //新
                        JSONArray dataArray = response.getJSONArray("data");
                        for(int i = 0; i < dataArray.length(); i++) {
                            JSONObject fence = (JSONObject) dataArray.get(i);
                            Map<String, Object> map = new Hashtable<>();

                            map.put("fenceName", fence.get("fenceName"));

                            try {
                                map.put("berthAmount", "泊位：" + fence.getString("berthAmount"));
                            } catch (JSONException e) {
                                map.put("berthAmount", "泊位：无");
                                Log.i("Main",fence.get("fenceName").toString() + " : 无berthAmount");
                            }


                            map.put("fenceNo", fence.get("fenceNo"));
                            map.put("inShipAmount", fence.get("inShipAmount"));
                            map.put("fenceLevel", fence.get("fenceLevel"));

//                            map.put("city",fence.getString("city"));
//                            map.put("country",fence.getString("country"));
//                            map.put("fenceAddr",fence.getString("fenceAddr"));
//                            map.put("fenceType",fence.getInt("fenceType"));
//                            map.put("fenceTypeName",fence.getString("fenceTypeName"));

                            dataList.add(map);
                        }
                        simpleAdapter.notifyDataSetChanged();
                    }

                    toast.setText("获取成功");
                    toast.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("解析失败");
                    toast.show();
                }

                if (isFirstTimeToGet) {
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            kProgressHUD.dismiss();
                        }
                    }, 300);
                    isFirstTimeToGet = false;
                }

                fenceListView.onRefreshComplete();


//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        refreshEnable = true;
//                    }
//                }, 2000);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                if (isFirstTimeToGet) {
                    kProgressHUD.dismiss();
                }
                toast.setText("网络连接失败");
                toast.show();
                fenceListView.onRefreshComplete();
            }
        });

        return dataList;
    }

    protected void fenceInfodialog(String fenceName, String city, String country, String fenceAddr, int fenceType, String fenceTypeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FenceActivity.this);
        builder.setMessage(
                "所在市：" + city + "\n" +
                "所在县（市）：" + country + "\n" +
                "港口港址：" + fenceAddr + "\n" +
                "港口类型：" + fenceType + "\n" +
                "港口类型名称：" + fenceTypeName
            );
        builder.setTitle(fenceName);
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }
}
