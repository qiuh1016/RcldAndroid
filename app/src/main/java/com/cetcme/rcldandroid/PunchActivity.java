package com.cetcme.rcldandroid;

import android.app.Activity;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.baidu.platform.comapi.map.L;
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
    int iofFlag;
    ArrayList<String> ids = new ArrayList<>();
    ArrayList<Integer> uploadOKList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_punch);

        Bundle bundle = this.getIntent().getExtras();
        iofFlag = bundle.getInt("iofFlag");
        if (iofFlag == 1) {
            setTitle("出海确认");
        } else if (iofFlag == 2) {
            setTitle("回港确认");
        }

        listView = (ListView) findViewById(R.id.punchListView);
        simpleAdapter = new SimpleAdapter(this, getPunchData(), R.layout.punchlistview,
                new String[]{"name", "id", "punchTime", "dataTypeString"},
                new int[]{
                        R.id.nameTextViewInPunchListView,
                        R.id.idTextViewInPunchListView,
                        R.id.punchTimeTextViewInPunchListView,
                        R.id.dataTypeTextViewInPunchListView
                        });
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog(dataList.get(position), position);
            }
        });

        toast = Toast.makeText(PunchActivity.this, "", LENGTH_SHORT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem add = menu.add(0, 0, 0, "添加");
        MenuItem confirm = menu.add(0, 0, 0, "确认");

        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        confirm.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        add.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("ids", ids);
                Intent addIntent = new Intent();
                addIntent.setClass(getApplicationContext(), ReasonActivity.class);
                addIntent.putExtras(bundle);
                startActivity(addIntent);
//                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                return false;
            }
        });

        confirm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                String punchInfo = "";
                for (Map<String, Object> map: dataList) {
                    punchInfo += "姓名：" + map.get("name") + "，身份证：" + map.get("id") + "\n";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(PunchActivity.this);
                builder.setMessage(punchInfo);
                builder.setTitle("确认上传");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        upload();
                    }
                });
                builder.create().show();
                return false;
            }
        });

        return true;
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
//        toast.cancel();
    }

    @Override
    public void onResume(){
        super.onResume();

        //更新adapter 添加之后 toAdd 为true
        String name, id, reason;
        SharedPreferences punchToAdd = getSharedPreferences("punchToAdd", Context.MODE_PRIVATE);
        Boolean toAdd = punchToAdd.getBoolean("toAdd", false);
        if (toAdd) {
            name = punchToAdd.getString("name", "");
            id = punchToAdd.getString("id", "");
            reason = punchToAdd.getString("reason", "");

            Map<String, Object> map = new Hashtable<>();
            map.put("iofFlag",iofFlag);
            map.put("id", id);
            map.put("name", name);
            map.put("dataType", 1);
            map.put("dataTypeString", "手动添加");
            map.put("punchTime", "");
            map.put("reason", reason);
            dataList.add(map);

            SharedPreferences.Editor editor = punchToAdd.edit();
            editor.putBoolean("toAdd",false);
            editor.apply();

            ids.add(id);

            simpleAdapter.notifyDataSetChanged();
        }

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
        String shipNumber,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");
        password = new PrivateEncode().b64_md5(password);

        dataList = new ArrayList<>();

        //设置时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd%20HH:mm:ss");
        Date endDate = new Date();
        String endTime = df.format(endDate);
        Calendar calendar = Calendar.getInstance();//日历对象
        calendar.setTime(endDate);//设置当前日期
        //TODO:完成后改回小时减5
        calendar.add(Calendar.YEAR, -5);//小时减5
        String startTime = df.format(calendar.getTime());//输出格式化的日期

        //设置输入参数
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        Log.i("Main",startTime +" "+ endTime);

        String urlBody = "http://"+serverIP+"/api/app/punch/get.json";
        String url = urlBody+"?userName="+shipNumber+"&password="+password+"&startTime="+startTime+"&endTime="+endTime;
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

                        for(int i = 0; i < dataArray.length(); i++) {
                            JSONObject punch = (JSONObject) dataArray.get(i);

                            Map<String, Object> map = new Hashtable<>();
                            map.put("iofFlag",iofFlag);
                            map.put("id", punch.getString("sailorIdNo"));
                            map.put("name", punch.getString("sailorName"));
                            map.put("dataType", 0);
                            map.put("dataTypeString", "自动生成");
                            map.put("punchTime", punch.getString("punchTime"));
                            map.put("reason", "");
                            dataList.add(map);
                            //id检测是否重复用
                            ids.add(punch.getString("sailorIdNo"));
                        }
                        simpleAdapter.notifyDataSetChanged();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    toast.setText("数据解析失败");
                    toast.show();
                }
                kProgressHUD.dismiss();

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

    protected void dialog(final Map<String,Object> map, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PunchActivity.this);
        builder.setMessage(
                "姓名：" + map.get("name") + "\n" +
                "身份证：" + map.get("id") + "\n" +
                "出入港标志：" + map.get("iofFlag") + "\n" +
                "数据类型：" + map.get("dataTypeString") + "\n" +
                "打卡时间：" + map.get("punchTime") + "\n"+
                "原因：" + map.get("reason")
        );
        builder.setTitle("人员信息");
        builder.setPositiveButton("取消", null);
        //删除
        builder.setNegativeButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("Main","删除");

                int dataType = (int) map.get("dataType");

                //dataType 0: 自动生成 1: 手动添加 2: 手动删除
                if (dataType == 1) {
                    dataList.remove(position);
                    ids.remove(position);
                } else if (dataType == 0) {

                    final EditText et = new EditText(PunchActivity.this);

                    new AlertDialog.Builder(PunchActivity.this).setTitle("删除原因")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setView(et)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = et.getText().toString();
                                    if (input.equals("")) {
                                        Toast.makeText(getApplicationContext(), "删除原因不能为空", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        //操作
                                        dataList.get(position).put("reason", input);
                                        dataList.get(position).put("dataType", 2);
                                        dataList.get(position).put("dataTypeString", "手动删除");
                                        simpleAdapter.notifyDataSetChanged();
                                    }
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();



                } else if (dataType == 2) {
                    dataList.get(position).put("reason", "");
                    dataList.get(position).put("dataType", 0);
                    dataList.get(position).put("dataTypeString", "自动生成");
                }

                simpleAdapter.notifyDataSetChanged();

            }
        });
        builder.create().show();
    }

    private void upload() {

        kProgressHUD = KProgressHUD.create(PunchActivity.this)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("上传中")
            .setAnimationSpeed(1)
            .setDimAmount(0.3f)
            .setSize(110, 110)
            .show();

        uploadOKList = new ArrayList<>();
        Log.i("Main" , "一共" + dataList.size() + "个数据");
        for (int i = 0; i < dataList.size(); i++) {
            uploadPunchs(i);
        }

    }

    private void uploadPunchs(final int position) {

        //获取保存的用户名和密码
        String shipNumber,password,serverIP;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", "120.27.149.252");
        password = new PrivateEncode().b64_md5(password);

        //设置输入参数
        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("iofFlag", iofFlag);
        params.put("sailorIdNo", dataList.get(position).get("id"));
        params.put("sailorName", dataList.get(position).get("name"));
        params.put("punchTime", dataList.get(position).get("punchTime"));
        params.put("dataType", dataList.get(position).get("dataType"));
        params.put("reason", dataList.get(position).get("reason"));


        String urlBody = "http://"+serverIP+"/api/app/iof/sailor/new.json";
//        String url = urlBody+"?userName="+shipNumber+"&password="+password+"&startTime="+startTime+"&endTime="+endTime;
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main",response.toString());
                try {
                    int code = response.getInt("code");
                    if (code == 0) {
                        uploadOKList.add(position);
                        Log.i("Main", uploadOKList.toString());
                        if (uploadOKList.size() == dataList.size()) {
                            kProgressHUD.dismiss();
                            toast.setText("上传成功");
                            toast.show();
                            return;
                        }
                    } else {
                        Log.i("Main", position + "not ok");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Main", "error");
                }

                kProgressHUD.dismiss();
                toast.setText("上传失败");
                toast.show();

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                toast.setText("网络连接失败");
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.i("Main", response);
                kProgressHUD.dismiss();
                toast.setText("网络连接失败");
                toast.show();
            }


        });
    }

}
