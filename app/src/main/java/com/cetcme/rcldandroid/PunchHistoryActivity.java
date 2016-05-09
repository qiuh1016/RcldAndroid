package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class PunchHistoryActivity extends AppCompatActivity {


    PullToRefreshListView listView;

    List<Map<String, Object>> dataList;
    Toast toast;
    SimpleAdapter simpleAdapter;

    int sum; //总数
    int totalPage;
    int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch_history);

        toast = Toast.makeText(PunchHistoryActivity.this, "", LENGTH_SHORT);
        listView = (PullToRefreshListView) findViewById(R.id.punchHistoryListView);
        listView.setMode(PullToRefreshBase.Mode.BOTH);

        listView.getLoadingLayoutProxy(true,false).setRefreshingLabel("刷新中");
        listView.getLoadingLayoutProxy(true,false).setReleaseLabel("松开立即刷新");
        listView.getLoadingLayoutProxy(true,false).setPullLabel("下拉可以刷新");
        listView.getLoadingLayoutProxy(false,true).setRefreshingLabel("加载中");
        listView.getLoadingLayoutProxy(false,true).setReleaseLabel("松开立即加载");
        listView.getLoadingLayoutProxy(false,true).setPullLabel("上拉可以加载");

//        listView.setRefreshing();

        simpleAdapter = new SimpleAdapter(PunchHistoryActivity.this, getPunchData(true), R.layout.punchlistview,
                new String[]{"name", "id", "punchTime", "null"},
                new int[]{
                        R.id.nameTextViewInPunchListView,
                        R.id.idTextViewInPunchListView,
                        R.id.punchTimeTextViewInPunchListView,
                        R.id.dataTypeTextViewInPunchListView
                });
        listView.setAdapter(simpleAdapter);

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                if (listView.isHeaderShown()) {
                    getPunchData(true);
                } else if (listView.isFooterShown()) {
                    if (currentPage != totalPage) {
                        getPunchData(false);
                    } else {
                        listView.getLoadingLayoutProxy(false,true).setRefreshingLabel("已全部加载完成");
                        listView.getLoadingLayoutProxy(false,true).setReleaseLabel("已全部加载完成");
                        listView.getLoadingLayoutProxy(false,true).setPullLabel("已全部加载完成");
                        listView.onRefreshComplete();
                    }

                }

            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    private List<Map<String, Object>> getPunchData(Boolean isRefresh) {

//        final KProgressHUD kProgressHUD = KProgressHUD.create(PunchHistoryActivity.this)
//                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//                .setLabel("获取中")
//                .setAnimationSpeed(1)
//                .setDimAmount(0.3f)
//                .setSize(110, 110)
//                .show();

        //获取保存的用户名和密码
        String shipNumber,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");
        password = new PrivateEncode().b64_md5(password);

        //刷新则清空
        if (isRefresh) {
            dataList = new ArrayList<>();
            currentPage = 1;
            listView.getLoadingLayoutProxy(false,true).setRefreshingLabel("加载中");
            listView.getLoadingLayoutProxy(false,true).setReleaseLabel("松开立即加载");
            listView.getLoadingLayoutProxy(false,true).setPullLabel("上拉可以加载");
        }

        //设置输入参数
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("pageNum", currentPage + 1);
        params.put("pageSize", 20);

        String urlBody = "http://"+serverIP+"/api/app/punch/allByPage.json";
        String url = urlBody+"?userName="+shipNumber+"&password="+password+"&pageNum=0"+"&pageSize=20";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main",response.toString());

                try {
                    String msg = response.getString("msg");
                    if (msg.equals("没有符合条件的数据")) {
                        toast.setText("没有符合条件的数据");
                        toast.show();
                    } else if (msg.equals("成功")) {
                        JSONArray dataArray = response.getJSONArray("data");
                        sum = response.getInt("total");  //获取总数
                        totalPage = sum / 20 + 1;

                        for(int i = 0; i < dataArray.length(); i++) {
                            JSONObject punch = (JSONObject) dataArray.get(i);

                            Map<String, Object> map = new Hashtable<>();
                            map.put("id", punch.getString("sailorIdNo"));
                            map.put("name", punch.getString("sailorName"));
                            map.put("punchTime", punch.getString("punchTime"));
                            map.put("null", "");
                            for (int j = 0; j < currentPage; j++) {
                                dataList.add(map);
                            }

                        }
                        simpleAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("数据解析失败");
                    toast.show();
                }
//                kProgressHUD.dismiss();
                listView.onRefreshComplete();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                kProgressHUD.dismiss();
                listView.onRefreshComplete();
                toast.setText("网络连接失败");
                toast.show();
            }
        });

        return dataList;
    }


}

