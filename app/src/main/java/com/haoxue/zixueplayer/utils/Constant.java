package com.haoxue.zixueplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;


import java.io.File;

/**
 * Created by Administrator on 2016/7/29.
 */
public class Constant {

    public static String SP_NAME="MuyuMusic";
    public static final String DB_NAME="MuyuPlayer.db";
    public static final int PLAY_RECORD_NUM=10;//最劲播放显示的最大条数

    //咪咕音乐地址
    public static final String MIGU_URL="http://music.migu.cn/";

    //华语榜
    public static final String MIGU_DAYHOT="rank/184_98.html?loc=P4Z1Y4L1N1&locno=0";
    //欧美榜
    public static final String MIGU_DAYEUS="rank/184_101.html?loc=P4Z1Y5L1N1&locno=0";
    //日韩榜
    public static final String MIGU_DAYJSK="rank/184_100.html?loc=P4Z1Y6L1N1&locno=0";
    //原创榜
    public static final String MIGU_DAYORI="rank/184_104.html?loc=P4Z1Y7L1N1&locno=0";
    //影视榜
    public static final String MIGU_DAYMTV="rank/184_103.html?loc=P4Z1Y8L1N1&locno=0";
    //网络榜
    public static final String MIGU_DAYNET="rank/184_106.html?loc=P4Z1Y9L1N1&locno=0";
    //民族榜
    public static final String MIGU_DAYNAT="rank/184_108.html?loc=P4Z1Y10L1N1&locno=0";
    //纯音乐榜
    public static final String MIGU_DAYPUR="rank/184_107.html?loc=P4Z1Y11L1N1&locno=0";
    //KTV点歌榜
    public static final String MIGU_DAYKTV="rank/184_109.html?loc=P4Z1Y12L1N1&locno=0";

    //搜索
    public static final String MIGU1_SEARCH1="webfront/searchNew/searchAll.do?keyword=";
    public static final String MIGU1_SEARCH2="&keytype=all&pagesize=200&pagenum=1";

    //下载
    public static final String MIGU_DOWN_HEAD = "http://music.migu.cn/order/";
    public static final String MIGU_DOWN_FOOT = "/down/self/P2Z3Y12L1N2/3/001002A/1003215279";
    //歌词
    public static final String BAIDU_LRC_SEARCH_HEAD = "http://music.baidu.com/search/lrc?key=";

    public static final String USER_AGENT="Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";

    //成功标记
    public static final int SUCCESS=1;
    //失败标记
    public static final int FAILED=2;


    public static final String DIR_MUSIC="/Music";
    public static final String DIR_LRC="/Music/lrc/";

    /***判断网络是否正常**/
    public static boolean getNetIsAvailable(Context context){
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connManager.getActiveNetworkInfo();
        if(networkInfo==null){
            return false;
        }
        return networkInfo.isAvailable();
    }
    /**
     * 提示消息
     * */
    public static Toast showMessage(Toast toastMsg, Context context, String msg) {
        if (toastMsg == null) {
            toastMsg = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        } else {
            toastMsg.setText(msg);
        }
        toastMsg.show();
        return toastMsg;
    }
    /**
     * 删除文件并删除媒体库中数据
     * */
    public static boolean deleteFile(Context context,String filePath){
        new File(filePath).delete();
        ContentResolver cr=context.getContentResolver();
        int id=-1;
        Cursor cursor=cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID}
                , MediaStore.Audio.Media.DATA+"=?", new String[]{filePath}, null);
        if(cursor.moveToNext()){
            id=cursor.getInt(0);
        }
        cursor.close();
        if(id!=-1){
            return cr.delete(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id), null, null)>0;
        }
        return false;
    }


    /**
     * 获取屏幕的大小0：宽度 1：高度
     * */
    public static int[] getScreen(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        return new int[] { (int) (outMetrics.density * outMetrics.widthPixels),
                (int) (outMetrics.density * outMetrics.heightPixels) };
    }
    /**
     * 获取文件的后缀名，返回大写
     * */
    public static String getSuffix(String str) {
        int i = str.lastIndexOf('.');
        if (i != -1) {
            return str.substring(i + 1).toUpperCase();
        }
        return str;
    }
    /**
     * 格式化毫秒->00:00
     * */
    public static String formatSecondTime(int millisecond) {
        if (millisecond == 0) {
            return "00:00";
        }
        millisecond = millisecond / 1000;
        int m = millisecond / 60 % 60;
        int s = millisecond % 60;
        return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
    }

    /**
     * 格式化文件大小 Byte->MB
     * */
    public static String formatByteToMB(long l){
        float mb=l/1024f/1024f;
        return String.format("%.2f",mb);
    }
    /**
     * 根据文件路径获取文件目录
     * */
    public static String clearFileName(String str) {
        int i = str.lastIndexOf(File.separator);
        if (i != -1) {
            return str.substring(0, i + 1);
        }
        return str;
    }
    /**
     * 根据文件名获取不带后缀名的文件名
     * */
    public static String clearSuffix(String str) {
        int i = str.lastIndexOf(".");
        if (i != -1) {
            return str.substring(0, i);
        }
        return str;
    }
    /**
     * 根据文件路径获取不带后缀名的文件名
     * */
    public static String clearDirectory(String str) {
        int i = str.lastIndexOf(File.separator);
        if (i != -1) {
            return clearSuffix(str.substring(i + 1, str.length()));
        }
        return str;
    }
    /**
     * 检查SD卡是否已装载
     * */
    public static boolean isExistSdCard(){
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    /**
     * 获得SD目录路径
     * */
    public static String getSdCardPath(){
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 判断文件是否存在
     * */
    public static boolean isExistFile(String file){
        return new File(file).exists();
    }
    /**
     * 判断目录是否存在，不在则创建
     * */
    public static void isExistDirectory(String directoryName) {
        File file = new File(directoryName);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    /**
     * 修改文件名
     * */
    public static String renameFileName(String str){
        int i=str.lastIndexOf('.');
        if(i!=-1){
            File file=new File(str);
            file.renameTo(new File(str.substring(0,i)));
            return str.substring(0,i);
        }
        return str;
    }
}
