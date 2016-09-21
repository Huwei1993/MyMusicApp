package com.haoxue.zixueplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.haoxue.zixueplayer.utils.BaseTools;
import com.haoxue.zixueplayer.utils.MediaUtils;
import com.haoxue.zixueplayer.vo.Mp3Info;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现的功能：
 * 1、播放
 * 2、暂停
 * 3、上一首
 * 4、下一首
 * 5、获取当前的播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer;
    private int currentPosition;//当前正在播放额歌曲的位置
    ArrayList<Mp3Info> mp3Infos;

    private MusicUpdateListener musicUpdateListener;

    private ExecutorService es = Executors.newSingleThreadExecutor();

    private boolean isPause = false;


    //切换播放列表
    public static final  int MY_MUSIC_LIST=1;//我的音乐列表
    public static final  int LIKE_MUSIC_LIST=2;//我喜欢的列表
    public static final  int PLAY_RECORD_MUSIC_LIST=3;//最近播放列表
    private int changePlayList=MY_MUSIC_LIST;

    //播放模式
    public static final int ORDER_PLAY = 1, RANDOM_PLAY = 2, SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;

    /**
     * @param play_mode ORDER_PLAY=1
     *                  RANDOM_PLAY=2 随机播放
     *                  SINGLE_PLAY=3 单曲循环
     */
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    public PlayService() {
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    private Random random = new Random();

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MuyuPlayerApp app = (MuyuPlayerApp) getApplication();
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        es.execute(updateStatusRunnable);
        initButtonReceiver();
        initService();
        sendNotification();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && !es.isShutdown()) {
            es.shutdown();
            es = null;
        }
        mNotificationManager.cancelAll();// 删除你发的所有通知
        pause();
        mPlayer=null;
        mp3Infos=null;
        musicUpdateListener=null;
    }

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && mPlayer != null && mPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getcurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //播放
    public void play(int position) {
        Mp3Info mp3Info = null;
            if (position < 0 || position >= mp3Infos.size()) {
            position = 0;
        }
        if (mp3Infos == null){
            return;
        }
        mp3Info = mp3Infos.get(position);
        try {
            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            mPlayer.prepare();
            mPlayer.start();
            currentPosition = position;
            sendNotification();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (musicUpdateListener != null) {
            musicUpdateListener.onChange(currentPosition);
        }

    }

    //暂停
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next() {
        if (currentPosition +1>= mp3Infos.size()) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //开始
    public void start() {
        if (mPlayer != null && !mPlayer.isPlaying()) {
            mPlayer.start();
            mPlayer.getDuration();
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public int getcurrentProgress() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        return mPlayer.getDuration();
    }

    public void seekTo(int msec) {
        mPlayer.seekTo(msec);
    }

    //更新状态的接口
    public interface MusicUpdateListener {
        public void onPublish(int progress);

        public void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }



    /** 通知栏按钮点击事件对应的ACTION */
    public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
    /** 通知栏按钮广播 */
    public ButtonBroadcastReceiver bReceiver;
    /** Notification管理 */
    public NotificationManager mNotificationManager;
    //Notification ID
    private int NID_1=0x1;


    public void sendNotification(){
        Mp3Info mp3InfoSend = this.mp3Infos.get(currentPosition);
        final NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
        //创建一个远程的视图
        RemoteViews views=new RemoteViews(getPackageName(),R.layout.custom_layout);
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3InfoSend.getId(), mp3InfoSend.getAlbumId(), false, false);
        if (albumBitmap!=null){
            views.setImageViewBitmap(R.id.custom_song_icon, albumBitmap);
        }else {
            views.setImageViewResource(R.id.custom_song_icon,R.mipmap.app_logo3);
        }
        views.setTextViewText(R.id.tv_custom_song_name,mp3InfoSend.getTitle());
        views.setTextViewText(R.id.tv_custom_song_singer,mp3InfoSend.getArtist());
        //如果版本号低于（3。0），那么不显示按钮
        if(BaseTools.getSystemVersion() <= 9){
            views.setViewVisibility(R.id.ll_custom_button, View.GONE);
        }else{
            views.setViewVisibility(R.id.ll_custom_button, View.VISIBLE);

            if(isPlaying()){
                views.setImageViewResource(R.id.btn_custom_play, R.mipmap.pause2);
            }else{
                views.setImageViewResource(R.id.btn_custom_play, R.mipmap.play2);
            }
        }
        Intent intent=new Intent(this,MainActivity.class);
        PendingIntent pi=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        //点击的事件处理
        Intent buttonIntent = new Intent(ACTION_BUTTON);
		/* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PLAY_ID);
        PendingIntent intent_play = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_custom_play, intent_play);
		/* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getBroadcast(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_custom_next, intent_next);
		/* 退出按钮 */
        buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_EXIT_ID);
        PendingIntent intent_exit = PendingIntent.getBroadcast(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_custom_exit, intent_exit);


        builder.setContent(views)
                .setContentIntent(pi)
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setTicker("木雨音乐")
                .setPriority(Notification.PRIORITY_MAX)// 设置该通知优先级
                .setOngoing(true)
                .setSmallIcon(R.mipmap.app_logo3);


        NotificationManager nm= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NID_1, builder.build());
    }


    /**
     * 初始化要用到的系统服务
     */
    private void initService() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    /** 带按钮的通知栏点击广播接收 */
    public void initButtonReceiver(){
        bReceiver = new ButtonBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BUTTON);
        registerReceiver(bReceiver, intentFilter);
    }

    public final static String INTENT_BUTTONID_TAG = "ButtonId";
    /** 播放/暂停 按钮点击 ID */
    public final static int BUTTON_PLAY_ID = 1;
    /** 下一首 按钮点击 ID */
    public final static int BUTTON_NEXT_ID = 2;
    /** 退出 按钮点击 ID */
    public final static int BUTTON_EXIT_ID = 3;
    /**
     *	 广播监听按钮点击时间
     */
    public class ButtonBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if(action.equals(ACTION_BUTTON)){
                //通过传递过来的ID判断按钮点击属性或者通过getResultCode()获得相应点击事件
                int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
                switch (buttonId) {

                    case BUTTON_PLAY_ID:
                        if (isPlaying()) {
                            pause();
                        } else {
                            if (isPause()) {
                                start();
                            } else {
                                play(getCurrentPosition());
                            }
                        }
                        sendNotification();
                        break;
                    case BUTTON_NEXT_ID:
                        next();
                        break;
                    case BUTTON_EXIT_ID:
                        mNotificationManager.cancelAll();// 删除你发的所有通知
                        // 为Intent设置Action属性
                        intent.setAction("com.muyu_Service");
                        stopService(intent);

                        int pid = android.os.Process.myPid();//获取当前应用程序的PID
                        android.os.Process.killProcess(pid);//杀死当前进程
                        break;

                    default:
                        break;
                }
            }
        }
    }
}
