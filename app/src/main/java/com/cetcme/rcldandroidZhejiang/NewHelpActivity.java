package com.cetcme.rcldandroidZhejiang;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import java.util.List;
import java.util.Map;

public class NewHelpActivity extends AppCompatActivity {

    private SimpleAdapter simpleAdapter;
    private ArrayList<String> dataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_help);

        setTitle("帮助");

        dataList.add("修改密码");
        dataList.add("检测更新");
        dataList.add("信息反馈");

        ListView listView = (ListView) findViewById(R.id.listViewInNewHelpActivity);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Log.i("Main","mima*****");
                        break;
                    case 1:
                        Log.i("Main","gengxin*****");
                        UpdateAppManager updateManager;
                        updateManager = new UpdateAppManager(getApplicationContext());
                        updateManager.checkUpdateInfo();
                        break;
                    case 2:
                        Log.i("Main","fankui*****");
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
}
