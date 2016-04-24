package com.cetcme.rcldandroid;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class RouteActivity extends AppCompatActivity {

    private Button startTimePickButton;
    private Button endTimePickButton;
    private Button routeSearchButton;
    private String startTime;
    private String endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        setTitle("轨迹记录");
        startTimePickButton = (Button) findViewById(R.id.startTimePickButton);
        endTimePickButton = (Button) findViewById(R.id.endTimePickButton);
        routeSearchButton = (Button) findViewById(R.id.routeSearchButton);

        startTimePickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime(startTimePickButton);
            }
        });

        endTimePickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime(endTimePickButton);
            }
        });

        routeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startTime == null || endTime == null) {
                    dialog();
                } else {
                    showDisplayIntent();
                }
            }
        });

    }

    public void showDisplayIntent() {
        Bundle bundle = new Bundle();
        bundle.putString("startTime", startTime);
        bundle.putString("endTime", endTime);
        //bundle.putBoolean("displayMediaPoint", );

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), RouteDisplayActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

    public void pickTime(final Button button) {
        Calendar calendar = Calendar.getInstance();

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
                    startTime = year + month + day;
                } else if (button.getId() == R.id.endTimePickButton) {
                    endTime = year + month + day;
                }
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        startTimeDatePickerDialog.show();

        startTimeDatePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog startTimePickerDialog = new TimePickerDialog(RouteActivity.this,
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
                                    startTime += hour + min;
                                    button.setText(startTime);
                                } else if (button.getId() == R.id.endTimePickButton) {
                                    endTime += hour + min;
                                    button.setText(endTime);
                                }
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true);
                startTimePickerDialog.show();
            }
        });
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

}
