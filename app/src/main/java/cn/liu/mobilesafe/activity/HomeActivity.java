package cn.liu.mobilesafe.activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import cn.liu.mobilesafe.R;
import cn.liu.mobilesafe.utils.ContantValues;
import cn.liu.mobilesafe.utils.Md5Util;
import cn.liu.mobilesafe.utils.SpUtils;
import cn.liu.mobilesafe.utils.ToastUtils;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private Context mContext;
    private String[] mTitleStr;
    private int[] mImgId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = this;
        initData();
        initUI();


    }

    /**
     * 初始化数据
     */
    private void initData() {
        mTitleStr = new String[]{"手机防盗", "通信卫士", "软件管理", "进程管理", "流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心"};
        mImgId = new int[]{R.drawable.home_safe, R.drawable.home_callmsgsafe,
                R.drawable.home_apps, R.drawable.home_taskmanager,
                R.drawable.home_netmanager, R.drawable.home_trojan,
                R.drawable.home_sysoptimize, R.drawable.home_tools, R.drawable.home_settings};
    }

    /**
     * 初始化布局
     */
    private void initUI() {
        GridView gv_home = (GridView) findViewById(R.id.gv_home);
        gv_home.setAdapter(new MyAdapter());
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        showDialog();
                        Log.i(TAG, "onItemClick:00");
                        break;
                    case 8:
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);

                        break;
                    default:
                        break;


                }
            }
        });
    }


    //第一次进入安全中心的对话框
    private void showDialog() {
        String pwd = (String) SpUtils.get(getApplicationContext(), ContantValues.SAFE_PWD, "");
        if (TextUtils.isEmpty(pwd)) {
            showFirstDialog();
        }
        else {
            showOtherDialog();
        }

    }


    private void showOtherDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(mContext, R.layout.dialog_other, null);
        dialog.setView(view);
        dialog.show();
        Button bt_confirm_other= (Button) view.findViewById(R.id.bt_confirm_other);
        Button bt_cancel_other = (Button) view.findViewById(R.id.bt_cancel_other);
        bt_confirm_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_other_pwd = (EditText) view.findViewById(R.id.et_other_pwd);
                String pwd=et_other_pwd.getText().toString().trim();
                //sp保存的密码
                String savePwd = (String) SpUtils.get(mContext, ContantValues.SAFE_PWD, "");
                if (!TextUtils.isEmpty(pwd)){
                    if (Md5Util.encoder(pwd).equals(savePwd)){
                        Intent intent = new Intent(mContext, SafeCenterActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }else {
                        ToastUtils.showToast(mContext,"密码不正确");
                    }

                }
                else {
                    ToastUtils.showToast(mContext,"密码不能为空");
                }

            }
        });


    }

    private void showFirstDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(mContext, R.layout.dialog_first, null);
        dialog.setView(view);
        dialog.show();
        Button bt_confirm = (Button) view.findViewById(R.id.bt_confirm);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        //对话框设置确认按钮的点击事件
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_first_pwd = (EditText) view.findViewById(R.id.et_first_pwd);
                EditText et_confirm_pwd = (EditText) view.findViewById(R.id.et_confirm_pwd);
                String first_pwd = et_first_pwd.getText().toString().trim();
                String confirm_pwd = et_confirm_pwd.getText().toString().trim();
                if (!TextUtils.isEmpty(first_pwd)&&!TextUtils.isEmpty(confirm_pwd)){
                    //判断两次密码是否一致
                    if (first_pwd.equals(confirm_pwd)){
                        //进入安全中心
                        SpUtils.set(mContext,ContantValues.SAFE_PWD,Md5Util.encoder(first_pwd));
                        Intent intent = new Intent(mContext, SafeCenterActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                    else {
                        ToastUtils.showToast(mContext,"密码不一致");
                    }
                }
                else {
                    ToastUtils.showToast(mContext,"密码不能为空");
                }

            }
        });
        //对话框设置取消按钮点击事件
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    //gridView适配器
    class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            //条目的总数	文字组数 == 图片张数
            return mTitleStr.length;
        }

        @Override
        public Object getItem(int position) {
            return mTitleStr[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.gridview_item, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_homegride);
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_homegride);
            tv_title.setText(mTitleStr[position]);
            iv_icon.setImageResource(mImgId[position]);

            return view;
        }
    }


}
