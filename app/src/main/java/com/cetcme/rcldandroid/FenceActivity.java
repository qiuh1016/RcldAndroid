package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    private Boolean isFirstTimeToGet = true;

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

        simpleAdapter = new SimpleAdapter(this, getFenceData(), R.layout.fence_list_cell,
                new String[]{"fenceName", "berthAmount", "inShipAmount", "fenceLevel"},
                new int[]{
                        R.id.fenceNameTextViewInFenceListView,
                        R.id.boweiTextViewInFenceListView,
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
                Map<String, Object> map = dataList.get(i-1);
                String fenceName = (String) map.get("fenceName");
                String city = (String) map.get("city");
                String country = (String) map.get("country");
                String fenceAddr = (String) map.get("fenceAddr");
                String fenceTypeName = (String) map.get("fenceTypeName");
                int fenceType = (int) map.get("fenceType");

                fenceInfoDialog(fenceName, city, country, fenceAddr, fenceType, fenceTypeName);
            }
        });

    }

    public void onBackPressed() {
        toast.cancel();
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    private List<Map<String, Object>> getFenceData() {

        toast.cancel();
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


        final String username,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        username = user.getString("username","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));

        dataList.clear();

        RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        String urlBody = "http://"+serverIP+ getString(R.string.fenceUrl);
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                try {

                    //新
                    JSONArray dataArray = response.getJSONArray("data");
                    for(int i = 0; i < dataArray.length(); i++) {
                        JSONObject fence = (JSONObject) dataArray.get(i);
                        Map<String, Object> map = new Hashtable<>();

                        try {
                            map.put("fenceName", fence.get("fenceName"));
                        } catch (JSONException e) {
                            map.put("fenceName", "无");
                        }

                        try {
                            map.put("berthAmount", fence.getString("berthAmount"));
                        } catch (JSONException e) {
                            map.put("berthAmount", "无");
                        }

                        try {
                            map.put("fenceNo", fence.get("fenceNo"));
                        } catch (JSONException e) {
                            map.put("fenceNo","无");
                        }

                        try {
                            map.put("inShipAmount", fence.get("inShipAmount"));
                        } catch (JSONException e) {
                            map.put("inShipAmount",0);
                        }

                        try {
                            map.put("fenceLevel", fence.get("fenceLevel"));
                        } catch (JSONException e) {
                            map.put("fenceLevel","无");
                        }

                        try {
                            map.put("city",fence.getString("city"));
                        } catch (JSONException e) {
                            map.put("city","无");
                        }

                        try {
                            map.put("country",fence.getString("country"));
                        } catch (JSONException e) {
                            map.put("country","无");
                        }

                        try {
                            map.put("fenceAddr",fence.getString("fenceAddr"));
                        } catch (JSONException e) {
                            map.put("fenceAddr","无");
                        }

                        try {
                            map.put("fenceType",fence.getInt("fenceType"));
                        } catch (JSONException e) {
                            map.put("fenceType","无");
                        }

                        try {
                            map.put("fenceTypeName",fence.getString("fenceTypeName"));
                        } catch (JSONException e) {
                            map.put("fenceTypeName","无");
                        }

                        dataList.add(map);
                    }
                    simpleAdapter.notifyDataSetChanged();


                    toast.setText("获取成功");
                    toast.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("解析失败");
                    toast.show();
                }

                if (isFirstTimeToGet) {
                    kProgressHUD.dismiss();
                    isFirstTimeToGet = false;
                }

                fenceListView.onRefreshComplete();

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

    private void fenceInfoDialog(String fenceName, String city, String country, String fenceAddr, int fenceType, String fenceTypeName) {
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
