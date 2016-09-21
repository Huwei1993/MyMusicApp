package com.haoxue.zixueplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/28.
 */
public abstract class BaseActivity extends FragmentActivity {

    protected PlayService playService;

    public ArrayList<Activity> list=new ArrayList<>();
    private boolean isBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list.add(this);

    }

    //退出功能
    public void  exit(){

        for (int i = 0; i <list.size() ; i++) {

            list.get(i).finish();
        }
        stopService(new Intent(BaseActivity.this, PlayService.class));
    }

    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder= (PlayService.PlayBinder) service;
            playService=playBinder.getPlayService();
            playService.setMusicUpdateListener(musicUpdateListener);
            musicUpdateListener.onChange(playService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService=null;
            isBound=false;
        }
    };

    private PlayService.MusicUpdateListener musicUpdateListener=new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int progress) {
            change(progress);
        }
    };

    public abstract void publish(int progress);
    public abstract void change(int progress);

    //绑定服务
    public void bindPlayService(){
        if (!isBound) {
            Intent intent=new Intent(this,PlayService.class);
            bindService(intent,conn, Context.BIND_AUTO_CREATE);
            isBound=true;
        }
    }
    //解除绑定服务
    public void unbindPlayService(){
        if (isBound) {
            unbindService(conn);
            isBound=false;
        }
    }
}
