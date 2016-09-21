package com.haoxue.zixueplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.haoxue.zixueplayer.utils.Constant;
import com.lidroid.xutils.DbUtils;

/**
 * Created by Administrator on 2016/7/29.
 */
public class MuyuPlayerApp extends Application{
    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        sp=getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils=DbUtils.create(getApplicationContext(),Constant.DB_NAME);
        context=getApplicationContext();
    }
}
