package cn.liu.mobilesafe.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by liu on 2017-03-08.
 */

public class ToastUtils {
    public static  void showToast(Context context,String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
