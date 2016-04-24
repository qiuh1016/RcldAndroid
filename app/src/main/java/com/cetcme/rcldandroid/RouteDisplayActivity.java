package com.cetcme.rcldandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class RouteDisplayActivity extends AppCompatActivity {

    private TextView startTimeTextView;
    private TextView endTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_display);
        setTitle("轨迹显示");

        Bundle bundle = this.getIntent().getExtras();
        String startTime = bundle.getString("startTime");
        String endTime = bundle.getString("endTime");

        startTimeTextView = (TextView) findViewById(R.id.startTimeTextView);
        endTimeTextView = (TextView) findViewById(R.id.endTimeTextView);

        startTimeTextView.setText(startTime);
        endTimeTextView.setText(endTime);



    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }

}
