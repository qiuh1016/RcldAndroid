package com.cetcme.rcldandroidZhejiang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.baidu.platform.comapi.map.C;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.msebera.android.httpclient.Header;

/**
 * Created by qiuhong on 5/26/16.
 */
public class UpdateAppManager {

    private Double currentVersion;
    private Double serverVersion;
    private KProgressHUD kProgressHUD;

    // 文件分隔符
    private static final String FILE_SEPARATOR = "/";
    // 外存sdcard存放路径
    private static final String FILE_PATH = Environment.getExternalStorageDirectory() + FILE_SEPARATOR +"0RCLDAutoUpdate" + FILE_SEPARATOR;
    // 下载应用存放全路径
    private static  String FILE_NAME = FILE_PATH + "RCLD_AutoUpdate.apk";
    // 更新应用版本标记
    private static final int UPDATE_TOKEN = 0x29;
    // 准备安装新版本应用标记
    private static final int INSTALL_TOKEN = 0x31;

    private Context context;
    private String message = "检测到本程序有新版本发布，建议您更新！";

    // 服务器路径
    private String UPDATE_SERVER_ADDRESS = "http://192.168.0.228:8081";
    // 下载路径
    private String spec = UPDATE_SERVER_ADDRESS + "/download";
    // 版本路径
    private String versionUrl = UPDATE_SERVER_ADDRESS + "/version";
    // 下载应用的对话框
    private Dialog dialog;
    // 下载应用的进度条
//    private ProgressBar progressBar;
    private RoundCornerProgressBar progressBar;
    private TextView progressTextView;
    // 进度条的当前刻度值
    private int curProgress;
    // 用户是否取消下载
    private boolean isCancel;
    // 强制更新
    private boolean forceToUpdate;
    // 是否手动检测更新
    private boolean manualCheckUpdate;

    //用户数据
    private String username,password,serverIP,updateContent;

    public UpdateAppManager(Context context) {
        this.context = context;

        SharedPreferences user = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        username = user.getString("username","");
        password = user.getString("password","");
        serverIP = user.getString("serverIP", context.getString(R.string.defaultServerIP_1));
        UPDATE_SERVER_ADDRESS = "http://" + serverIP;

        //kProgressHUD
        kProgressHUD = KProgressHUD.create(context)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("检测中")
                .setAnimationSpeed(1)
                .setDimAmount(0.3f)
                .setSize(110, 110)
                .setCancellable(false);

        // 下载路径
        spec = UPDATE_SERVER_ADDRESS + context.getString(R.string.getDownLoadUrl);

        // 版本路径
        versionUrl = UPDATE_SERVER_ADDRESS + context.getString(R.string.appVersionUrl);

        SharedPreferences system = context.getSharedPreferences("system", Context.MODE_PRIVATE);
        String versionString = system.getString("version","");
        Log.i("Main","currentVersion: " + versionString );
        try {
            currentVersion = Double.valueOf(versionString);
        } catch (NumberFormatException e) {
            currentVersion = 0.0;
        }

        // 获取是否为手动检测更新
        manualCheckUpdate = system.getBoolean("manualCheckUpdate", false);

        if (manualCheckUpdate) {
            kProgressHUD.show();
        }


    }

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TOKEN:
                    progressBar.setProgress(curProgress);
                    progressTextView.setText(curProgress + "/100");
                    break;

                case INSTALL_TOKEN:
                    installApp();
                    break;
            }
        }
    };

    /**
     * 检测应用更新信息
     */
    public void checkUpdateInfo() {

        RequestParams params = new RequestParams();
        params.put("code", 1); // 1: 渔民app  2: 安装app

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(versionUrl, params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                kProgressHUD.dismiss();

                Log.i("Main", "versionJSON: " + response.toString());
                try {
                    String version = response.getString("version");
                    try {
                        forceToUpdate = response.getBoolean("force_update");
                    } catch (JSONException e) {
                        forceToUpdate = false;
                    }

                    try {
                        updateContent = response.getString("content");
                    } catch (JSONException e) {
                        updateContent = "";
                    }

                    //TODO: 用version Code 比较

                    try {
                        serverVersion = Double.valueOf(version);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        serverVersion = 0.0;
                        Log.i("Main", "******** 服务器新版本号转换错误: " + version);
                    }

                    if (serverVersion > currentVersion) {
                        FILE_NAME = FILE_PATH + "RCLD_V" + version +".apk";
                        showNoticeDialog();
                    } else {
                        if (manualCheckUpdate) {
                            //手动检测更新
                            showNoUpdateDialog();
                            //将手动检测flag设置为false
                            SharedPreferences system = context.getSharedPreferences("system", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = system.edit();
                            editor.putBoolean("manualCheckUpdate", false);
                            editor.apply();
                        } else {
                            //登陆界面自动检测更新
                            Log.i("Main","无更新");
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("Main", "getVersion: " + errorResponse);
                kProgressHUD.dismiss();
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i("Main", responseString);
                kProgressHUD.dismiss();
            }
        });



    }

    /**
     * 显示提示更新对话框
     */
    private void showNoticeDialog() {
        if (!forceToUpdate) {
            message = "检测到新版本发布(V"+ serverVersion + ")，建议您更新！";
        } else {
            message = "检测到新版本发布(V"+ serverVersion + ")，请您更新！";
        }
        AlertDialog.Builder builder =  new AlertDialog.Builder(context);
        builder.setTitle("软件版本更新")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("下载", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showDownloadDialog();
                    }
                });
        if (!forceToUpdate) {
            builder.setNegativeButton("以后再说", null);
        }
        builder.create().show();
    }

    /**
     * 显示无更新对话框
     */
    private void showNoUpdateDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(context);
        builder.setTitle("当前已为最新版本")
                .setMessage("当前版本：V" + currentVersion + "。")
                .setCancelable(false)
                .setPositiveButton("好的", null);
        builder.create().show();
    }

    /**
     * 显示下载进度对话框
     */
    private void showDownloadDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.progress_bar, null);
//        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar = (RoundCornerProgressBar) view.findViewById(R.id.progressBar);
        progressTextView = (TextView) view.findViewById(R.id.progressTextView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("新版本下载中");
        builder.setView(view);
        builder.setCancelable(false);
        if (!forceToUpdate) {
            builder.setNegativeButton("取消", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isCancel = true;
                }
            });
        }
        dialog = builder.create();
        dialog.show();
        downloadApp();

    }

    /**
     * 下载新版本应用
     */
    private void downloadApp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                InputStream in = null;
                FileOutputStream out = null;
                HttpURLConnection conn = null;
                try {
                    url = new URL(spec);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    long fileLength = conn.getContentLength();
                    in = conn.getInputStream();
                    File filePath = new File(FILE_PATH);
                    if(!filePath.exists()) {
                        filePath.mkdir();
                    }
                    out = new FileOutputStream(new File(FILE_NAME));
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    long readedLength = 0L;
                    while((len = in.read(buffer)) != -1) {
                        // 用户点击“取消”按钮，下载中断
                        if(isCancel) {
                            break;
                        }
                        out.write(buffer, 0, len);
                        readedLength += len;
                        curProgress = (int) (((float) readedLength / fileLength) * 100);
                        handler.sendEmptyMessage(UPDATE_TOKEN);
                        if(readedLength >= fileLength) {
                            dialog.dismiss();
                            // 下载完毕，通知安装
                            handler.sendEmptyMessage(INSTALL_TOKEN);
                            break;
                        }
                    }
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if(out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 安装新版本应用
     */
    private void installApp() {
        File appFile = new File(FILE_NAME);
        if(!appFile.exists()) {
            return;
        }
        // 跳转到新版本应用安装页面
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + appFile.toString()), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
