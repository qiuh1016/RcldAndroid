package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class ioLogActivity extends AppCompatActivity {


    private PullToRefreshListView listView;

    private List<Map<String, Object>> dataList = new ArrayList<>();
    private Toast toast;
    private SimpleAdapter simpleAdapter;

    private int sum; //总数
    private int totalPage;
    private int currentPage = 0;
    private Boolean isFirstTimeToGet = true;

    private KProgressHUD kProgressHUD;
    private int pageSize = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io_log);

        toast = Toast.makeText(ioLogActivity.this, "", LENGTH_SHORT);
        listView = (PullToRefreshListView) findViewById(R.id.ioLogListView);
        listView.setMode(PullToRefreshBase.Mode.BOTH);

        listView.getLoadingLayoutProxy(true,false).setRefreshingLabel("刷新中");
        listView.getLoadingLayoutProxy(true,false).setReleaseLabel("松开立即刷新");
        listView.getLoadingLayoutProxy(true,false).setPullLabel("下拉可以刷新");
        listView.getLoadingLayoutProxy(false,true).setRefreshingLabel("加载中");
        listView.getLoadingLayoutProxy(false,true).setReleaseLabel("松开立即加载");
        listView.getLoadingLayoutProxy(false,true).setPullLabel("上拉可以加载");

        simpleAdapter = new SimpleAdapter(ioLogActivity.this, getioLogData(true), R.layout.io_log_list_cell,
                new String[]{"iofTime", "id", "punchTime", "null"},
                new int[]{
                        R.id.timeTextInioLogCell,
                        R.id.ioFlagTextInioLogCell,
                        R.id.punchTimeTextViewInPunchListView,
                        R.id.dataTypeTextViewInPunchListView
                });
        listView.setAdapter(simpleAdapter);

        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {

                if (listView.isHeaderShown()) {
                    getioLogData(true);
                } else if (listView.isFooterShown()) {
                    if (currentPage != totalPage) {
                        getioLogData(false);
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

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    //TODO: 等该接口
    private List<Map<String, Object>> getioLogData(final Boolean isRefresh) {

        if (isFirstTimeToGet) {
            kProgressHUD = KProgressHUD.create(ioLogActivity.this)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel("获取中")
                    .setAnimationSpeed(1)
                    .setDimAmount(0.3f)
                    .setSize(110, 110)
                    .setCancellable(false)
                    .show();
        }

        //获取保存的用户名和密码
        String shipNumber,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");

        //刷新则清空
        if (isRefresh) {
            dataList.clear();
            currentPage = 0;
            Log.i("Main", "isRefresh");
            listView.getLoadingLayoutProxy(false,true).setRefreshingLabel("加载中");
            listView.getLoadingLayoutProxy(false,true).setReleaseLabel("松开立即加载");
            listView.getLoadingLayoutProxy(false,true).setPullLabel("上拉可以加载");
        }

        //设置时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd%20HH:mm:ss");
        Date endDate = new Date();
        String endTime = df.format(endDate);
        Calendar calendar = Calendar.getInstance();//日历对象
        calendar.setTime(endDate);//设置当前日期
        calendar.add(Calendar.MONTH, -1);//
        String startTime = df.format(calendar.getTime());//设置起始日期

        //设置输入参数
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

//        params.put("pageNum", currentPage + 1);
//        params.put("pageSize", pageSize);

        String urlBody = "http://"+serverIP+"/api/app/iof/get.json";
        String url = urlBody+"?userName="+shipNumber+"&password="+password+"&pageNum=0"+"&pageSize=20";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main",response.toString());

                try {
                    JSONArray dataArray = response.getJSONArray("data");

                    for(int i = 0; i < dataArray.length(); i++) {
                        JSONObject punch = (JSONObject) dataArray.get(i);

                        Map<String, Object> map = new Hashtable<>();
                        map.put("id", punch.getString("sailorIdNo"));
                        map.put("name", punch.getString("sailorName"));
                        map.put("punchTime", punch.getString("punchTime"));
                        map.put("null", "");
//                            map.put("null", "第"+currentPage+"页");

                        dataList.add(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("数据解析失败");
                    toast.show();
                }
//                /*try {
//                    String msg = response.getString("msg");
//                    if (msg.equals("没有符合条件的数据")) {
//                        toast.setText("没有符合条件的数据");
//                        toast.show();
//                    } else if (msg.equals("成功")) {
//                        JSONArray dataArray = response.getJSONArray("data");
//
//                        //刷新的时候 重置总数和总页数
//                        if (isRefresh) {
//                            sum = response.getInt("total");  //获取总数
//                            totalPage = sum / pageSize + 1;
//
//                            //如果只有1页
//                            if (totalPage == 1) {
//                                listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//                            } else {
//                                listView.setMode(PullToRefreshBase.Mode.BOTH);
//                            }
//                        } else {
//                            //加载的时候检测总数是否变化 如变化 重新刷新
//                            int sumToget = response.getInt("total");
//                            if (sumToget != sum) {
//                                getPunchData(true);
//                                Toast.makeText(getApplicationContext(),"总数有变，已刷新数据",LENGTH_SHORT).show();
//                                return;
//                            }
//
//                        }
//
//                        currentPage += 1;
//
//                        for(int i = 0; i < dataArray.length(); i++) {
//                            JSONObject punch = (JSONObject) dataArray.get(i);
//
//                            Map<String, Object> map = new Hashtable<>();
//                            map.put("id", punch.getString("sailorIdNo"));
//                            map.put("name", punch.getString("sailorName"));
//                            map.put("punchTime", punch.getString("punchTime"));
//                            map.put("null", "");
////                            map.put("null", "第"+currentPage+"页");
//
//                            dataList.add(map);
//                        }
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    toast.setText("数据解析失败");
//                    toast.show();
//                }*/
                if (isFirstTimeToGet) {
                    kProgressHUD.dismiss();
                    isFirstTimeToGet = false;
                }
                simpleAdapter.notifyDataSetChanged();
                listView.onRefreshComplete();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                if (isFirstTimeToGet) {
                    kProgressHUD.dismiss();
                    isFirstTimeToGet = false;
                }
                listView.onRefreshComplete();
                toast.setText("网络连接失败");
                toast.show();
            }
        });

        return dataList;
    }

}
