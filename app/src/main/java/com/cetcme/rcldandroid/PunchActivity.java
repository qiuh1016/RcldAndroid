package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

import static android.widget.Toast.LENGTH_SHORT;

public class PunchActivity extends AppCompatActivity {

    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String, Object>> dataList;
    Toast toast;
    KProgressHUD kProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch);

        Bundle bundle = this.getIntent().getExtras();
        String title = bundle.getString("title");
        setTitle(title);

        listView = (ListView) findViewById(R.id.punchListView);
        simpleAdapter = new SimpleAdapter(this, getPunchData(), R.layout.punchlistview,
                new String[]{"name", "id"},
                new int[]{
                        R.id.nameTextViewInPunchListView,
                        R.id.idTextViewInPunchListView,
                        });
        listView.setAdapter(simpleAdapter);

        toast = Toast.makeText(PunchActivity.this, "", LENGTH_SHORT);
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
//        toast.cancel();
    }

    private List<Map<String, Object>> getPunchData() {

        kProgressHUD = KProgressHUD.create(PunchActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("获取中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(110, 110)
                .show();

        //获取保存的用户名和密码
        String shipNumber,password;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");

        dataList = new ArrayList<>();

        //设置时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd%20HH:mm:ss");
        Date startDate = new Date();
        String startTime = df.format(startDate);
        Calendar calendar = Calendar.getInstance();//日历对象
        calendar.setTime(startDate);//设置当前日期
        calendar.add(Calendar.YEAR, -5);//小时减5
        String endTime = df.format(calendar.getTime());//输出格式化的日期

        //设置输入参数
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("startTime",startTime);
        params.put("endTime",endTime);

        String urlBody = "http://120.27.149.252/api/app/punch/get.json";
        String url = "http://120.27.149.252/api/app/punch/get.json?userName="+shipNumber+"&password="+password+"&startTime="+startTime+"&endTime="+endTime;
        AsyncHttpClient client = new AsyncHttpClient();
        //TODO: 待接口修改
        client.get(url, null, new JsonHttpResponseHandler("UTF-8"){
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

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                kProgressHUD.dismiss();

                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        kProgressHUD.dismiss();
                    }
                }, 300);

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

}
