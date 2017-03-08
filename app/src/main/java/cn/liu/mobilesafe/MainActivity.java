package cn.liu.mobilesafe;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import utils.InputStreamUtils;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private Handler handler=new Handler(){

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case ENTER_MAIN:
                    Log.d(tag, "llll");
                    //进入主界面
                    enterHomeActivity();

                    break;
                case URL_ERROR:

                    break;
                case JSON_ERROR:

                    break;
                case UPDATE_VERSION:

                    break;
                case IO_ERROR:

                    break;

                default:
                    break;
            }


        };

    };


    private void enterHomeActivity() {
        Log.i(tag, "enterHomeActivity: 跳转");
        Intent intent =new Intent(this,HomeActivity.class);
        startActivity(intent);
//        //结束导航页面
//        finish();

    }

    private TextView vn;
    private static final String tag = "MainActivity";
    protected static final int UPDATE_VERSION =100;
    protected static final int ENTER_MAIN = 101;
    protected static final int IO_ERROR = 102;
    protected static final int JSON_ERROR = 103;
    protected static final int URL_ERROR = 104;
    private int mVersionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vn = (TextView) findViewById(R.id.tv_version_name);
        initData();
    }

    private void initData() {
        String versionName = getVersionName();
          mVersionCode= getVersionCode();
        Log.i(tag, "mVersionCode "+mVersionCode);
        getDataFromServer();
        vn.setText("版本名称"+versionName);

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


                        HttpURLConnection  connection= (HttpURLConnection) url.openConnection();
                        connection.setConnectTimeout(2000);
                        connection.setReadTimeout(2000);
                        int code = connection.getResponseCode();
                        //请求成功
                        if (code==200) {

                            InputStream is = connection.getInputStream();
                            String json = InputStreamUtils.streamToString(is);
                            JSONObject jsonObject=new JSONObject(json);
                            Log.v(tag, json);
                            String versionName = jsonObject.getString("version_name");
                            String versionCode = jsonObject.getString("version_code");
                            String description = jsonObject.getString("description");
                            String download_url = jsonObject.getString("download_url");
                            Log.i(tag, "run: "+versionCode);
                            if (mVersionCode<Integer.parseInt(versionCode)) {
                                //弹出更新对话框
                                Log.d(tag, "run: hahahahahhaha"+versionCode+mVersionCode);
                                msg.what=UPDATE_VERSION;
                            }else {

                                //进入主界面
                                msg.what=ENTER_MAIN;

                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        msg.what=IO_ERROR;
                    } catch (JSONException e) {
                        msg.what=JSON_ERROR;
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    msg.what=URL_ERROR;
                    e.printStackTrace();
                }finally{
                    long endCurrentTimeMillis = System.currentTimeMillis();
                    long dit=endCurrentTimeMillis-startCurrentTimeMillis;
                    if (dit<4000) {
                        try {
                            Thread.sleep(4000-dit);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                    }

                    handler.sendMessage(msg);
                    Log.i(tag, "run: sdfjfjsdkf");
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
