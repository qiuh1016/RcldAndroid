package com.cetcme.rcldandroidZhejiang;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class IndexActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView welcomeTextView;
    private Button myShipButton;
    private Button fenceButton;
    private Button routeButton;
    private Button helpButton;
    private AntiThiefService antiThiefService = new AntiThiefService();
//    private AntiThiefReceiver antiThiefReceiver;

    private JSONObject myShipInfoJSON;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("Main","onServiceConnected");
            antiThiefService = ((AntiThiefService.MsgBinder)service).getService();
            antiThiefService.setOnAntiThiefListener(new AntiThiefListener() {
                @Override
                public void antiThiefState(Boolean antiThief) {
                    Log.i("Main", "get:" + antiThief.toString());
                    if (antiThief) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAntiThiefDialog();
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    private BDLocation bdLocation;

    private int getLocationTime = 30; //秒

    private KProgressHUD kProgressHUD;
    private KProgressHUD okHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        setTitle(getString(R.string.loginTitle));

        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        myShipButton = (Button) findViewById(R.id.myShipButton);
        fenceButton = (Button) findViewById(R.id.fenceButton);
        routeButton = (Button) findViewById(R.id.routeButton);
        helpButton = (Button) findViewById(R.id.helpButton);

        myShipButton.setOnClickListener(this);
        fenceButton.setOnClickListener(this);
        routeButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        //set button size
        setButtonSize();

        //获取上一个Activity的数据
        Bundle bundle = this.getIntent().getExtras();
        String str = bundle.getString("myShipInfo");
        try {
            myShipInfoJSON = new JSONObject(str);
            JSONArray data = myShipInfoJSON.getJSONArray("data");
            JSONObject data0 = data.getJSONObject(0);
            String ownerName = data0.getString("ownerName");
            if (ownerName.isEmpty()) {
                welcomeTextView.setText("欢迎使用本软件!");
            } else {
                welcomeTextView.setText(ownerName + "，欢迎使用本软件!");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            welcomeTextView.setText("欢迎使用本软件!");
        }

        //绑定service
        Intent intent = new Intent();
        intent.setAction("com.cetcme.rcldandroid.AntiThiefService");
        intent.setPackage(getPackageName());
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                antiThiefService.startDetection();
            }
        },1000);


//        //接受广播
//        antiThiefReceiver = new AntiThiefReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.antiThief");
//        registerReceiver(antiThiefReceiver,intentFilter);

        //定位
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
//        mLocationClient.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        /**
         *  二维码
         */
        /*
        MenuItem qrcode = menu.add(0,0,0,"二维码");
        qrcode.setIcon(R.drawable.qrcodeicon);
        qrcode.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        qrcode.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //TODO: QRCODE
                Toast.makeText(getApplicationContext(),"二维码扫描，待开发",LENGTH_SHORT).show();
                return false;
            }
        });
        */

        /**
         *  sos
         */
//        MenuItem sos = menu.add(0, 0, 0, "SOS");
//        sos.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        sos.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                mLocationClient.start();
//                sosDialog();
//                return false;
//            }
//        });

        /**
         *  退出登录
         */
        MenuItem setting = menu.add(0, 0, 0, "退出");
        setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        setting.setIcon(R.drawable.user);
        setting.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                logoutDialog();
                return false;
            }
        });

        //TODO: 修改密码
//        MenuItem changePassword = menu.add(0, 0, 0, "修改密码");
//        changePassword.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        changePassword.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Toast.makeText(getApplicationContext(),"修改密码",LENGTH_SHORT).show();
//                return false;
//            }
//        });




        return true;

    }

    public void onBackPressed() {
        //返回手机桌面
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    private void logout() {
        Intent loginIntent = new Intent();
        loginIntent.setClass(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.push_right_in_no_alpha, R.anim.push_right_out_no_alpha);
        finish();
    }

    private void logoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
//        builder.setIcon(R.mipmap.ic_launcher);
        builder.setIcon(android.R.drawable.ic_menu_myplaces);
        builder.setMessage("是否继续?");
        builder.setTitle("即将退出登录");
        builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final KProgressHUD kProgressHUD = KProgressHUD.create(IndexActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("退出中")
                        .setAnimationSpeed(1)
                        .setDimAmount(0.3f)
                        .setSize(110, 110)
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        kProgressHUD.dismiss();
                        logout();
                    }
                },1000);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    private void finishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
//        builder.setIcon(R.mipmap.ic_launcher);
        builder.setIcon(android.R.drawable.ic_delete);

        builder.setMessage("是否继续?");
        builder.setTitle("即将关闭程序");
        builder.setNeutralButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setPositiveButton("最小化", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //返回手机桌面
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    //TODO: 定位 和 报警求助 搬这来
    private void sosDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(IndexActivity.this);
//        builder.setIcon(R.mipmap.ic_launcher);
        builder.setIcon(android.R.drawable.ic_menu_view);
//        builder.setMessage("是否继续?");
        builder.setTitle("请选择");
        builder.setPositiveButton("报警", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                kProgressHUD = KProgressHUD.create(IndexActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("报警中")
                        .setAnimationSpeed(1)
                        .setDimAmount(0.3f)
                        .setSize(110, 110)
                        .show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadHelpAlarm();
                    }
                },1000);
            }
        });
//        builder.setNegativeButton("查看", null);
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.myShipButton:
                Bundle bundle = new Bundle();
                bundle.putString("myShipInfo", myShipInfoJSON.toString());
                Intent myShipIntent = new Intent();
//                myShipIntent.setClass(getApplicationContext(), MyShipActivity.class);
                myShipIntent.setClass(getApplicationContext(), VisaActivity.class);
                myShipIntent.putExtras(bundle);
                startActivity(myShipIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;
            case R.id.fenceButton:
                Intent fenceIntent = new Intent();
                fenceIntent.setClass(getApplicationContext(), FenceActivity.class);
                startActivity(fenceIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;
            case R.id.routeButton:
//                Intent recordIntent = new Intent();
//                recordIntent.setClass(getApplicationContext(), RecordActivity.class);
//                startActivity(recordIntent);
//                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);

                Intent routeIntent = new Intent();
                routeIntent.setClass(getApplicationContext(), RouteActivity.class);
                startActivity(routeIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;
            case R.id.helpButton:
                Intent helpIntent = new Intent();
                helpIntent.setClass(getApplicationContext(), NewHelpActivity.class);
                startActivity(helpIntent);
                overridePendingTransition(R.anim.push_left_in_no_alpha, R.anim.push_left_out_no_alpha);
                break;

        }
    }

    private void showAntiThiefDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(IndexActivity.this);
        builder.setMessage("当前位置已超出您设置的防盗范围");
        builder.setTitle("注意");
        builder.setIcon(android.R.drawable.ic_delete);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences antiThief = getSharedPreferences("antiThief", 0);
                SharedPreferences.Editor edit = antiThief.edit();
                edit.putBoolean("notification", false);
                edit.putBoolean("antiThiefIsOpen", false);
                edit.apply();
            }
        });
        builder.create().show();
    }

//    public class AntiThiefReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context arg0, Intent arg1) {
//
//            Bundle bundle = arg1.getExtras();
//            Boolean antiThiefIsOpen = bundle.getBoolean("antiThiefIsOpen");
//            if (antiThiefIsOpen) {
//                antiThiefService.startDetection();
//                Log.i("Main","startDetection");
//            } else {
//                antiThiefService.stopDetection();
//                Log.i("Main","stopDetection");
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
//        antiThiefService.stopDetection();
//        unbindService(serviceConnection);
//        unregisterReceiver(antiThiefReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences antiThiefSharedPreferences = getSharedPreferences("antiThief",Context.MODE_PRIVATE);
        Boolean gotNotification = antiThiefSharedPreferences.getBoolean("notification", false);
        if (gotNotification) {
            Intent dialog = new Intent(getApplicationContext(), AlertDialogActivity.class);
//            dialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialog);

        }


    }

    private void setButtonSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point pointSize = new Point();
        display.getSize(pointSize);
        int buttonSize = pointSize.x * 14 / 44;
        int horizontalMargin = buttonSize / 3;
        int verticalMargin = pointSize.y * 7 / 100;

        RelativeLayout.LayoutParams welcomeTextParams = (RelativeLayout.LayoutParams) welcomeTextView.getLayoutParams();
        welcomeTextParams.bottomMargin = verticalMargin * 4 / 5;
        welcomeTextView.setLayoutParams(welcomeTextParams);

        RelativeLayout.LayoutParams myShipButtonParams = (RelativeLayout.LayoutParams) myShipButton.getLayoutParams();
        myShipButtonParams.height = buttonSize;
        myShipButtonParams.width = buttonSize;
        myShipButtonParams.rightMargin = horizontalMargin;
        myShipButton.setLayoutParams(myShipButtonParams);

        RelativeLayout.LayoutParams fenceButtonParams = (RelativeLayout.LayoutParams) fenceButton.getLayoutParams();
        fenceButtonParams.height = buttonSize;
        fenceButtonParams.width = buttonSize;
        fenceButton.setLayoutParams(fenceButtonParams);

        RelativeLayout.LayoutParams routeButtonParams = (RelativeLayout.LayoutParams) routeButton.getLayoutParams();
        routeButtonParams.height = buttonSize;
        routeButtonParams.width = buttonSize;
        routeButtonParams.topMargin = verticalMargin;
        routeButton.setLayoutParams(routeButtonParams);

        RelativeLayout.LayoutParams helpButtonParams = (RelativeLayout.LayoutParams) helpButton.getLayoutParams();
        helpButtonParams.height = buttonSize;
        helpButtonParams.width = buttonSize;
        helpButton.setLayoutParams(helpButtonParams);
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = getLocationTime * 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            bdLocation = location;
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
//            toast.setText(sb.toString());
//            toast.show();
        }

    }

    private void uploadHelpAlarm() {
        String username,password,serverIP,deviceNo,shipNo;
        SharedPreferences user = getSharedPreferences("user", Activity.MODE_PRIVATE);
        username = user.getString("username","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", getString(R.string.defaultServerIP_1));
        deviceNo = user.getString("deviceNo","");
        shipNo   = user.getString("shipNo","");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String date = sdf.format(new java.util.Date());

        //设置参数
        final RequestParams params = new RequestParams();
        params.put("userName", username);
        params.put("password", password);
        params.put("deviceNo", deviceNo);
        params.put("shipNo", shipNo);
        params.put("alertType", 1);
        params.put("longitude", bdLocation.getLongitude());
        params.put("latitude", bdLocation.getLatitude());
        params.put("time", date); //bdLocation.getTime()
        params.put("description", "报警求助");

        Log.i("Main", "报警内容：" + params.toString());

        mLocationClient.stop();

        String urlBody = "http://"+serverIP+ getString(R.string.helpAlarmUrl);
        AsyncHttpClient client = new AsyncHttpClient();
        client.setURLEncodingEnabled(true);
        client.post(urlBody, params, new JsonHttpResponseHandler("UTF-8"){
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("Main", response.toString());
                int code = 0;
                String msg = "";
                try {
                    code = response.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                kProgressHUD.dismiss();

                if (code != 0) {
                    try {
                        msg = response.getString("msg");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                } else {

                    ImageView imageView = new ImageView(IndexActivity.this);
                    imageView.setBackgroundResource(R.drawable.checkmark);
                    okHUD = KProgressHUD.create(IndexActivity.this)
                            .setCustomView(imageView)
                            .setLabel("报警成功")
                            .setCancellable(false)
                            .setSize(110,110)
                            .setDimAmount(0.3f)
                            .show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            okHUD.dismiss();
                        }
                    },1500);
                }


            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                kProgressHUD.dismiss();
                Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                kProgressHUD.dismiss();
                Log.i("Main", response);
                Toast.makeText(getApplicationContext(), "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
