package com.haoxue.zixueplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.haoxue.zixueplayer.utils.Constant;
import com.haoxue.zixueplayer.utils.DownloadUtils;
import com.haoxue.zixueplayer.vo.SearchResult;


/**
 * Created by Administrator on 2016/8/1.
 */
public class DownloadDialogFragment extends DialogFragment {

    private SearchResult searchResult;//当前要下载的歌曲对象
    private MainActivity mainActivity;

    public static DownloadDialogFragment newInstance(SearchResult searchResult) {
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.searchResult = searchResult;
        return downloadDialogFragment;
    }

    private String[] items;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        items = new String[]{"下载", "取消"};
    }

    //创建对话框的事件方法
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //执行下载
                        downloadMusic();
                        break;
                    case 1://取消
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();
    }




    // 回调接口
    public interface DownloadSuccessListener { // 下载是否成功.监听.按钮回调接口
        void downloadSuccessListener(String isDownloadSuccess); // 回传一个字符串
    }

    //下载音乐
    private void downloadMusic() {
        Toast.makeText(mainActivity, "正在下载：" + searchResult.getMusicName(), Toast.LENGTH_SHORT).show();
        DownloadUtils.getsInstance().setListener(new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownload(String mp3Url) {//下载成功
                Toast.makeText(mainActivity, "歌曲下载成功", Toast.LENGTH_SHORT).show();
                //扫描新下载的歌曲
//                Uri contentUri=Uri.fromFile((new File(mp3Url)));
//                Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
//                getContext().sendBroadcast(mediaScanIntent);

//                Uri contentUri=Uri.fromFile((new File(mp3Url)));
                String filename=Environment.getExternalStorageDirectory() + Constant.DIR_MUSIC + "/" + mp3Url;
//                System.out.println("我叫" + filename);
//                System.out.println("我是 " + contentUri);
//                System.out.println("好好好"+getContext());
//                System.out.println("你你你"+mainActivity);
                //更新媒体库
//                scanFile(mainActivity, filename);
                //更新 本地音乐列表 , 这个功能放在MainActivity实现

                //DownloadSuccessListener listener = (DownloadSuccessListener) getActivity(); // 空指针异常,因为Fragment已经销毁,所以getActivity()==null,需要使用下面的写法
                DownloadSuccessListener listener = mainActivity;  // 回调接口
                System.out.println("DownloadDialogFragment.downloadMusic.listener = " + listener);
                listener.downloadSuccessListener(mp3Url); // 回传一个字符串 ,回传什么都行 ,只是告诉MainActivity ,已经下载成功了新的歌曲

                updateGallery(filename);
            }

            @Override
            public void onFailed(String error) {//下载失败
                Toast.makeText(mainActivity, error, Toast.LENGTH_SHORT).show();
            }
        }).download(searchResult);
    }

    /**
     * 通知媒体库更新文件
     *
     */
//    public void scanFile(Context context, String filePath) {
//        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        scanIntent.setData(Uri.fromFile(new File(filePath)));
//        context.sendBroadcast(scanIntent);
//
//
//    }

    private void updateGallery(String filename)//filename是我们的文件全名，包括后缀哦
    {
        MediaScannerConnection.scanFile(mainActivity,
                new String[] { filename }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

}
