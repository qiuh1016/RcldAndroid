package com.cetcme.rcldandroidZhejiang;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NewHelpActivity extends AppCompatActivity implements View.OnClickListener {

    private SimpleAdapter simpleAdapter;
    private ArrayList<String> dataList = new ArrayList<>();
    private List<Map<String, Object>> dataList2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_help);

        setTitle("帮助");

        UIOperation();

        dataListOperation();

        ListView listView = (ListView) findViewById(R.id.listViewInNewHelpActivity);
//        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);


        simpleAdapter = new SimpleAdapter(this, dataList2, R.layout.help_view_cell,
                new String[]{"functionName"},
                new int[]{R.id.functionNameInHelpViewCell});
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Log.i("Main","mima*****");
                        Intent changePasswordIntent = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                        startActivity(changePasswordIntent);
                        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                        break;
                    case 1:
                        Log.i("Main","gengxin*****");

                        //将手动检测flag设置为true
                        SharedPreferences system = getSharedPreferences("system", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = system.edit();
                        editor.putBoolean("manualCheckUpdate", true);
                        editor.apply();

                        UpdateAppManager updateManager;
                        updateManager = new UpdateAppManager(NewHelpActivity.this);
                        updateManager.checkUpdateInfo();
                        break;
                    case 2:
                        Log.i("Main","fankui*****");
                        Intent feedbackIntent = new Intent(getApplicationContext(), FeedbackActivity.class);
                        startActivity(feedbackIntent);
                        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                        break;
                }
            }
        });


        //Display the current version number
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
            TextView versionTextView = (TextView) findViewById(R.id.versionTextInNewHelpActivity);
            versionTextView.setText("V" + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //copy right

    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    private void UIOperation() {
        TextView homePageTextView = (TextView) findViewById(R.id.homePageTextView);
        TextView principleTextView = (TextView) findViewById(R.id.principleTextView);
        TextView weiboTextView = (TextView) findViewById(R.id.weiboTextView);

        homePageTextView.setOnClickListener(this);
        principleTextView.setOnClickListener(this);
        weiboTextView.setOnClickListener(this);
    }

    private void dataListOperation() {
        dataList.add("修改密码");
        dataList.add("检测更新");
        dataList.add("信息反馈");

        Map<String, Object> map = new Hashtable<>();
        map.put("functionName", "修改密码");
        dataList2.add(map);

        map = new Hashtable<>();
        map.put("functionName", "检测更新");
        dataList2.add(map);

        map = new Hashtable<>();
        map.put("functionName", "信息反馈");
        dataList2.add(map);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.homePageTextView:
                Log.i("Main", "***** Home page");

                Uri uri = Uri.parse("http://www.cetcme.com/");
                Intent it = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(it);
                break;
            case R.id.principleTextView:
                Log.i("Main", "***** Principle");
                break;
            case R.id.weiboTextView:
                Log.i("Main", "***** weibo");
                break;
        }
    }
}
