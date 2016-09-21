package com.haoxue.zixueplayer.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.haoxue.zixueplayer.MuyuPlayerApp;

/**
 * Created by Administrator on 2016/8/1.
 */
public class AppUtils {
    //隐藏输入法
    public static void hideInputMethod(View view){
        InputMethodManager imm= (InputMethodManager) MuyuPlayerApp.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
