package com.cetcme.rcldandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class iofSailorActivity extends AppCompatActivity {

    private ListView listView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, Object>> dataList = new ArrayList<>();

    private String sailorListString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iof_sailor);

        Bundle bundle = this.getIntent().getExtras();
        String iofTime = bundle.getString("iofTime");
        String iofFlag = bundle.getString("iofFlag");
        sailorListString = bundle.getString("iofSailorList");
//        setTitle(iofTime + " " + iofFlag);
        setTitle("船员名单");

        listView = (ListView) findViewById(R.id.iofSailorListView);
        simpleAdapter = new SimpleAdapter(iofSailorActivity.this, getioSailorData(), R.layout.punch_list_cell,
                new String[]{"sailorName", "sailorNo", "reason","dataType"},
                new int[]{
                        R.id.nameTextViewInPunchListCell,
                        R.id.idTextViewInPunchListCell,
                        R.id.punchTimeTextViewInPunchListView,
                        R.id.dataTypeTextViewInPunchListView
                });
        listView.setAdapter(simpleAdapter);

    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    private List<Map<String, Object>> getioSailorData() {

        try {
            JSONArray sailorList = new JSONArray(sailorListString);

            for (int i = 0; i < sailorList.length(); i++) {
                JSONObject sailor = sailorList.getJSONObject(i);
                Map<String, Object> map = new Hashtable<>();
                try{
                    map.put("sailorName", sailor.getString("sailorName"));
                }catch(JSONException e){
                    map.put("sailorName", "无");
                }
                try{
                    map.put("sailorNo", sailor.getString("sailorNo"));
                }catch(JSONException e){
                    map.put("sailorNo", "无");
                }
                try{
                    map.put("punchTime", sailor.getString("punchTime"));
                }catch(JSONException e){
                    map.put("punchTime", "无");
                }
                try{
                    int dataType = sailor.getInt("dataType");
                    if (dataType == 0) {
                        map.put("dataType", "自动生成");
                    } else if (dataType == 1) {
                        map.put("dataType", "手动添加");
                    } else if (dataType == 2) {
                        map.put("dataType", "手动删除");
                    } else {
                        map.put("dataType", "无");
                    }

                }catch(JSONException e){
                    map.put("dataType", "无");
                }
                try{
                    map.put("reason", sailor.getString("reason"));
                }catch(JSONException e){
                    map.put("reason", "无");
                }
                dataList.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return dataList;
    }
}
