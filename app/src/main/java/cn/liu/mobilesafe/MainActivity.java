package cn.liu.mobilesafe;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import utils.InputStreamUtils;
import utils.ToastUtils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView vn;
    private static final String TAG = "MainActivity";
    protected static final int UPDATE_VERSION = 100;
    protected static final int ENTER_MAIN = 101;
    protected static final int IO_ERROR = 102;
    protected static final int JSON_ERROR = 103;
    protected static final int URL_ERROR = 104;
    private int mVersionCode;
    private String description;
    private String download_url;


    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ENTER_MAIN:
//                    Log.d(TAG, "llll");
                    //进入主界面
                    enterHomeActivity();

                    break;
                case URL_ERROR:
                    ToastUtils.showToast(MainActivity.this, "url错误");
                    enterHomeActivity();
                    break;
                case JSON_ERROR:
                    ToastUtils.showToast(MainActivity.this, "json解析错误");
                    enterHomeActivity();
                    break;
                case UPDATE_VERSION:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("是否更新软件");
                    builder.setMessage(description);
                    builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //请求更新
                            downloadApk();

                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            enterHomeActivity();
                        }
                    });
                    builder.show();

                    break;
                case IO_ERROR:
                    ToastUtils.showToast(MainActivity.this, "io异常");
                    enterHomeActivity();
                    break;

                default:
                    break;
            }


        }


        /**
         * 下载apk方法
         */

        private void downloadApk() {
            //判断sd卡状态,File.separator 表示:/
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //获取sd卡路径
//                String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
//                        File.separator + "aa.apk";
                RequestParams parms=new RequestParams(download_url);
                parms.setSaveFilePath(Environment.getExternalStorageDirectory()+"/myapp/");

                x.http().post(parms, new org.xutils.common.Callback.ProgressCallback<File>() {
                    
                    //网络请求之前回调
                    @Override
                    public void onWaiting() {

                    }

                    @Override
                    public void onStarted() {
                        Log.i(TAG, "onStarted: 开始下载");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isDownloading) {

                    }

                    @Override
                    public void onSuccess(File result) {
                        Log.i(TAG, "onSuccess: 下载成功");
                        installApk(result);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        Log.i(TAG, "onError: 下载失败");
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
                }



        }


    };
   //安装apk
    private void installApk(File file) {
        //系统应用界面,源码,安装apk入口
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
		/*//文件作为数据源
		intent.setData(Uri.fromFile(file));
		//设置安装的类型
		intent.setType("application/vnd.android.package-archive");*/
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
//		startActivity(intent);
        startActivityForResult(intent, 0);

    }

    //进入主界面
    private void enterHomeActivity() {
        Log.i(TAG, "enterHomeActivity: 跳转");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //结束导航页面
        finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vn = (TextView) findViewById(R.id.tv_version_name);
        initData();
    }

    private void initData() {
        String versionName = getVersionName();
        mVersionCode = getVersionCode();
        Log.i(TAG, "mVersionCode " + mVersionCode);
        getDataFromServer();
        vn.setText("版本名称" + versionName);

    }

    private void getDataFromServer() {

        new Thread(new Runnable() {
            Message msg = Message.obtain();

            @Override
            public void run() {
                long startCurrentTimeMillis = System.currentTimeMillis();
                try {

                    URL url = new URL("http://192.168.1.107:8080/data.json");
                    try {


                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(2000);
                        connection.setReadTimeout(2000);
                        int code = connection.getResponseCode();
                        //请求成功
                        if (code == 200) {

                            InputStream is = connection.getInputStream();
                            String json = InputStreamUtils.streamToString(is);
                            JSONObject jsonObject = new JSONObject(json);
                            Log.v(TAG, json);
                            String versionName = jsonObject.getString("version_name");
                            String versionCode = jsonObject.getString("version_code");
                            description = jsonObject.getString("description");
                            download_url = jsonObject.getString("download_url");
                            Log.i(TAG, "run: " + versionCode);
                            if (mVersionCode < Integer.parseInt(versionCode)) {
                                //弹出更新对话框
                                Log.d(TAG, "run: 弹出更新对话框" + versionCode + mVersionCode);
                                msg.what = UPDATE_VERSION;
                            } else {

                                //进入主界面
                                msg.what = ENTER_MAIN;

                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        msg.what = IO_ERROR;
                    } catch (JSONException e) {
                        msg.what = JSON_ERROR;
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    msg.what = URL_ERROR;
                    e.printStackTrace();
                } finally {
                    long endCurrentTimeMillis = System.currentTimeMillis();
                    long dit = endCurrentTimeMillis - startCurrentTimeMillis;
                    if (dit < 4000) {
                        try {
                            Thread.sleep(4000 - dit);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                    }

                    handler.sendMessage(msg);

                }

            }
        }).start();

    }


    private String getVersionName() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            String name = info.versionName;

            return name;


        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;


    }

    private int getVersionCode() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            int Code = info.versionCode;
            return Code;


        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;


    }


}
