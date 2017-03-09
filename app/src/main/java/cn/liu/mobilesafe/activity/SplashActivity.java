package cn.liu.mobilesafe.activity;

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
import org.xutils.http.RequestParams;
import org.xutils.x;

import cn.liu.mobilesafe.R;
import cn.liu.mobilesafe.utils.ContantValues;
import cn.liu.mobilesafe.utils.InputStreamUtils;
import cn.liu.mobilesafe.utils.SpUtils;
import cn.liu.mobilesafe.utils.ToastUtils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private TextView vn;
    private static final String TAG = "SplashActivity";
    protected static final int UPDATE_VERSION = 100;
    protected static final int ENTER_MAIN = 101;
    protected static final int IO_ERROR = 102;
    protected static final int JSON_ERROR = 103;
    protected static final int URL_ERROR = 104;
    private int mVersionCode;
    private String description;
    private String download_url;


    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ENTER_MAIN:
                    //进入主界面
                    enterHomeActivity();

                    break;
                case URL_ERROR:
                    ToastUtils.showToast(SplashActivity.this, "url错误");
                    enterHomeActivity();
                    break;
                case JSON_ERROR:
                    ToastUtils.showToast(SplashActivity.this, "json解析错误");
                    enterHomeActivity();
                    break;
                case UPDATE_VERSION:
                    boolean update = (boolean) SpUtils.get(getApplicationContext(), ContantValues.OPEN_UPDATE, false);
                    if (update){
                        UpdateApp();
                    }else {
                        //在handler发送消息四秒后处理ENTER_MAIN状态码对应的消息
                        mHandler.sendEmptyMessageDelayed(ENTER_MAIN,4000);
                    }


                    break;
                case IO_ERROR:
                    ToastUtils.showToast(SplashActivity.this, "io异常");
                    enterHomeActivity();
                    break;

                default:
                    break;
            }


        }



        /**
         *是否更新app
         */

        private  void  UpdateApp(){
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setTitle("是否更新软件");
            builder.setMessage(description);
            builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //请求更新
                    downloadApk();

                }
            });
            builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enterHomeActivity();
                }
            });
            builder.show();
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    enterHomeActivity();
                    dialog.dismiss();
                }
            });


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
    private RelativeLayout rlroot;

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


    //开启一个activity后，返回结果后调用的方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHomeActivity();
        super.onActivityResult(requestCode, resultCode, data);
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
        rlroot = (RelativeLayout) findViewById(R.id.rl_root);
        initData();
        StartAnimation();
    }

    private void StartAnimation() {
        //渐变动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(2000);
        //为控件设置动画
        rlroot.startAnimation(alphaAnimation);

    }


    //初始化数据
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

                    mHandler.sendMessage(msg);

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
