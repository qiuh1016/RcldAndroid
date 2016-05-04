package com.cetcme.rcldandroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.github.jjobes.slidedatetimepicker.SlideDateTimeListener;
import com.github.jjobes.slidedatetimepicker.SlideDateTimePicker;
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
import cz.msebera.android.httpclient.impl.client.SystemDefaultCredentialsProvider;

public class RouteActivity extends AppCompatActivity implements View.OnClickListener{

    private Button startTimePickButton;
    private Button endTimePickButton;
    private Button routeSearchButton;
    private Switch showMediumPointSwitch;

    KProgressHUD kProgressHUD;
    Toast toast;
    SlideDateTimePicker slideDateTimeListener;
    Boolean isStartTime = true;

    private String startTime;
    private String endTime;
    private String dataString;

    private SlideDateTimeListener listener = new SlideDateTimeListener() {

        @Override
        public void onDateTimeSet(Date date)
        {
            // Do something with the date. This Date object contains
            // the date and time that the user has selected.

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            if (isStartTime) {
                startTime = df.format(date);
                startTimePickButton.setText(startTime);
            } else {
                endTime = df.format(date);
                endTimePickButton.setText(endTime);
            }

            Log.i("Datetime",df.format(date));
        }

        @Override
        public void onDateTimeCancel()
        {
            // Overriding onDateTimeCancel() is optional.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        setTitle("轨迹记录");
        toast = Toast.makeText(getApplicationContext(),"没有符合条件的数据",Toast.LENGTH_SHORT);

        startTimePickButton = (Button) findViewById(R.id.startTimePickButton);
        endTimePickButton = (Button) findViewById(R.id.endTimePickButton);
        routeSearchButton = (Button) findViewById(R.id.routeSearchButton);
        showMediumPointSwitch = (Switch) findViewById(R.id.showMediumPointSwitchInRouteActivity);

        startTimePickButton.setOnClickListener(this);
        endTimePickButton.setOnClickListener(this);
        routeSearchButton.setOnClickListener(this);

        slideDateTimeListener = new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(listener)
                .setInitialDate(new Date())
                .setIs24HourTime(true)
                .build();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startTimePickButton:
                isStartTime = true;
                slideDateTimeListener.show();
//                pickTime(startTimePickButton);
                break;
            case R.id.endTimePickButton:
                isStartTime = false;
                slideDateTimeListener.show();
//                pickTime(endTimePickButton);
                break;
            case R.id.routeSearchButton:
                //TODO: 7day most
                if (startTime == null || endTime == null) {
                    dialog();
                } else {
                    kProgressHUD = KProgressHUD.create(RouteActivity.this)
                            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel("查询中")
                            .setAnimationSpeed(1)
                            .setDimAmount(0.3f)
                            .setSize(110, 110)
                            .show();
                    getRouteData();
                }
                break;
        }
    }

    public void showDisplayIntent() {
        Bundle bundle = new Bundle();
        bundle.putString("startTime", startTime);
        bundle.putString("endTime", endTime);
        bundle.putBoolean("showMediaPoint", showMediumPointSwitch.isChecked());
        bundle.putString("dataString", dataString);

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), RouteDisplayActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
    }

    public void onBackPressed() {
        toast.cancel();
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    //TODO: 日期和时间 一个界面
    public void pickTime(final Button button) {

        Calendar calendar = Calendar.getInstance();
        String date = "";
        if (button.getId() == R.id.startTimePickButton && startTime != null) {
            date = startTime;
        } else if (button.getId() == R.id.endTimePickButton && endTime != null) {
            date = endTime;
        }

        final int year,month,day,hour,minute;

        if (date.equals("")) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        } else {
            year = Integer.parseInt(date.substring(0, 4));
            month = Integer.parseInt(date.substring(5, 7));
            day = Integer.parseInt(date.substring(8, 10));
            hour = Integer.parseInt(date.substring(11, 13));
            minute = Integer.parseInt(date.substring(14, 16));
        }

        DatePickerDialog startTimeDatePickerDialog = new DatePickerDialog(RouteActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String month, day;
                if (monthOfYear < 10) {
                    month = "0" + (monthOfYear + 1);
                } else {
                    month = String.valueOf(monthOfYear + 1);
                }
                if (dayOfMonth < 10) {
                    day = "0" + dayOfMonth;
                } else {
                    day = String.valueOf(dayOfMonth);
                }
                if (button.getId() == R.id.startTimePickButton) {
                    startTime = year + "/" + month + "/" + day;
                } else if (button.getId() == R.id.endTimePickButton) {
                    endTime = year + "/" + month + "/" + day;
                }

                final TimePickerDialog startTimePickerDialog = new TimePickerDialog(RouteActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String hour, min;
                                if (hourOfDay < 10) {
                                    hour = "0" + hourOfDay;
                                } else {
                                    hour = String.valueOf(hourOfDay);
                                }
                                if (minute < 10) {
                                    min = "0" + minute;
                                } else {
                                    min = String.valueOf(minute);
                                }

                                if (button.getId() == R.id.startTimePickButton) {
                                    startTime += " " + hour + ":" + min;
                                    button.setText(startTime);
                                } else if (button.getId() == R.id.endTimePickButton) {
                                    endTime += " " + hour + ":" + min;
                                    button.setText(endTime);
                                }
                            }
                        },
                        hour,
                        minute,
                        true);

                startTimePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (button.getId() == R.id.startTimePickButton) {
                            if (button.getText().toString().equals("点击选择时间")) {
                                startTime = "";
                            } else {
                                startTime = button.getText().toString();
                            }
                        } else if (button.getId() == R.id.endTimePickButton) {
                            if (button.getText().toString().equals("点击选择时间")) {
                                endTime = "";
                            } else {
                                endTime = button.getText().toString();
                            }
                        }
                    }
                });
                startTimePickerDialog.show();

            }
        }, year, month - 1, day);

        startTimeDatePickerDialog.show();


    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
        builder.setMessage("起始时间或结束时间不能为空！");
        builder.setTitle("Error");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Log.i("Main", "dialog dismiss ok");

            }
        });
        builder.create().show();
    }

    private void getRouteData() {

        String shipNumber,password;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        shipNumber = user.getString("shipNumber","");
        password = user.getString("password","");

        String startTimeURL = startTime.replace(" ", "%20");
        String endTimeURL = endTime.replace(" ", "%20");

        RequestParams params = new RequestParams();
        params.put("userName", shipNumber);
        params.put("password", password);
        params.put("startTime", startTimeURL);
        params.put("endTime", endTimeURL);

        String urlBody = "http://120.27.149.252/api/app/trail/get.json";
        String url = "http://120.27.149.252/api/app/trail/get.json?userName=" + shipNumber +"&password="+password+"&startTime="+startTimeURL+"&endTime=" + endTimeURL;
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("JSONObject", response.toString());
                try {
                    String msg = response.getString("msg");
                    if (msg.equals("没有符合条件的数据")) {
                        toast.setText("没有符合条件的数据");
                        toast.show();
                    } else if (msg.equals("成功")) {
                        dataString = response.toString();
                        showDisplayIntent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    }

}
