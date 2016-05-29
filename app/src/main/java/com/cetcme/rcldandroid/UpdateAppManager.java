package com.cetcme.rcldandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

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
    private String UPDATE_SERVER_ADDRESS = "http://192.168.0.116:8081";
    // 下载路径
    private String spec = UPDATE_SERVER_ADDRESS + "/download";
    // 版本路径
    private String versionUrl = UPDATE_SERVER_ADDRESS + "/version";
    // 下载应用的对话框
    private Dialog dialog;
    // 下载应用的进度条
    private ProgressBar progressBar;
    // 进度条的当前刻度值
    private int curProgress;
    // 用户是否取消下载
    private boolean isCancel;

    public UpdateAppManager(Context context) {
        this.context = context;

        SharedPreferences system = context.getSharedPreferences("system", Context.MODE_PRIVATE);
        String versionString = system.getString("version","");
        Log.i("Main","currentVersion: " + versionString );
        try {
            currentVersion = Double.valueOf(versionString);
        } catch (NumberFormatException e) {
            currentVersion = 0.0;
        }
    }

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TOKEN:
                    progressBar.setProgress(curProgress);
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
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(versionUrl, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Double version = response.getDouble("version");
                    if (version > currentVersion) {
                        FILE_NAME = FILE_PATH + "RCLD_V" + version +".apk";
                        showNoticeDialog();
                    } else {
                        Log.i("Main","无更新");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    /**
     * 显示提示更新对话框
     */
    private void showNoticeDialog() {
        new AlertDialog.Builder(context)
                .setTitle("软件版本更新")
                .setMessage(message)
                .setPositiveButton("下载", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showDownloadDialog();
                    }
                }).setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * 显示下载进度对话框
     */
    private void showDownloadDialog() {
        View view = LayoutInflater.from(context).inflate(R.layout.progress_bar, null);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("软件版本更新");
        builder.setView(view);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isCancel = true;
            }
        });
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
