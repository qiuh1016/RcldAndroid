package com.cetcme.rcldandroidZhejiang;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.WindowManager;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by qiuhong on 5/16/16.
 */
public class AntiThiefService extends Service{

    private int sleepTime = 120;  //单位秒
    private Boolean antiThief = false;
    private Boolean detectionEnable = true;
    private AntiThiefListener antiThiefListener;
    private int progress = 0;
    private SharedPreferences userSharedPreferences;
    private SharedPreferences antiThiefSharedPreferences;

    public void setOnAntiThiefListener(AntiThiefListener antiThiefListener) {
        this.antiThiefListener = antiThiefListener;
    }

    public void startDetection() {
        userSharedPreferences = getSharedPreferences("user",Context.MODE_PRIVATE);
        antiThiefSharedPreferences = getSharedPreferences("antiThief",Context.MODE_PRIVATE);

        Log.i("Main", "startDetection");
        progress = 0;
        detectionEnable = true;
        antiThief = false;

        //测试用
//        showAlertDialog();

//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                CreateInform();
//            }
//        }, 5000);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (detectionEnable) {

                    String username = userSharedPreferences.getString("username","");
                    String password = userSharedPreferences.getString("password","");
                    String shipNo   = userSharedPreferences.getString("shipNo","");
                    RequestParams params = new RequestParams();
                    params.put("userName", username);
                    params.put("password", password);
                    params.put("shipNo",   shipNo);

                    String serverIP = userSharedPreferences.getString("serverIP", getString(R.string.defaultServerIP_1));
                    String urlBody = "http://"+serverIP+getString(R.string.shipGetUrl);
                    SyncHttpClient client = new SyncHttpClient();
                    client.get(urlBody, params, new JsonHttpResponseHandler("UTF-8") {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            // If the response is JSONObject instead of expected JSONArray
//                            Log.i("Main", response.toString());
                            //获取经纬度进行比较
                            try {
                                JSONArray data = response.getJSONArray("data");
                                JSONObject data0 = data.getJSONObject(0);
                                Double Lat = data0.getDouble("latitude");
                                Double Lng = data0.getDouble("longitude");

                                //更新船的位置
                                Intent intent = new Intent();
                                intent.putExtra("lat" , Lat);
                                intent.putExtra("lng" , Lng);
                                intent.setAction("com.updateShipLocation");
                                sendBroadcast(intent);
                                Log.i("Main", "ship location updated: lat:" + Lat + " lng: " + Lng);

                                //如果开启了报警 就做判断
                                Boolean antiThiefIsOpen = antiThiefSharedPreferences.getBoolean("antiThiefIsOpen", false);
                                if (antiThiefIsOpen) {
                                    Double antiThiefLat = Double.valueOf(antiThiefSharedPreferences.getString("antiThiefLat", "0"));
                                    Double antiThiefLng = Double.valueOf(antiThiefSharedPreferences.getString("antiThiefLng", "0"));
                                    Double distance = new PrivateEncode().GetDistance(Lat, Lng, antiThiefLat,antiThiefLng); // + progress;
                                    int antiThiefRadius = antiThiefSharedPreferences.getInt("antiThiefRadius", 1);
                                    Log.i("Main", "current: " + Lat + "," + Lng);
                                    Log.i("Main", "saved  : " + antiThiefLat + "," + antiThiefLng);

                                    Log.i("Main", "distance: " + distance + "-->" + antiThiefRadius * 1852);

                                    if (distance > antiThiefRadius * 1852) {
                                        stopDetection();

                                        SharedPreferences.Editor edit = antiThiefSharedPreferences.edit();

                                        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                        Date alertDate = new Date();
                                        String alertTime = df.format(alertDate);

                                        if (isAppOnForeground()){
                                            showAlertDialog();
                                        }
                                        CreateInform();
                                        edit.putBoolean("notification", true);
                                        edit.putString("alertTime",alertTime);
                                        edit.apply();
//                                    antiThief = true;
//                                    if (antiThiefListener != null) {
//                                        antiThiefListener.antiThiefState(true);
//                                    }

                                    }
                                }



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)

                        }
                    });

                    //设置每次时间间隔；
                    try {
                        Thread.sleep(sleepTime * 1000);
                        Log.i("Main", "sleep for " + sleepTime + "s");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopDetection() {
        antiThief = false;
        detectionEnable = false;
        Log.i("Main", "stopDetection");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        public AntiThiefService getService() {
            return AntiThiefService.this;
        }
    }

    public void CreateInform() {
        //定义一个PendingIntent，当用户点击通知时，跳转到某个Activity(也可以发送广播等)
        Intent intent = new Intent(getApplicationContext(), AlertDialogActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        //创建一个通知
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("注意")
                .setContentText("当前位置已超出您设置的防盗范围")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        //用NotificationManager的notify方法通知用户生成标题栏消息通知
        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nManager.notify(100, notification);//id是应用中通知的唯一标识
        //如果拥有相同id的通知已经被提交而且没有被移除，该方法会用更新的信息来替换之前的通知。
    }

    private Boolean isAppOnForeground() {
        ActivityManager activityManager =(ActivityManager) getApplicationContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        String packageName =getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AntiThiefService.this);
        builder.setMessage("service dialog");
        builder.setPositiveButton("OK",null);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();

    }

    private void showAlertDialog() {
        Intent dialog = new Intent(getApplicationContext(), AlertDialogActivity.class);
        dialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialog);
    }
}
