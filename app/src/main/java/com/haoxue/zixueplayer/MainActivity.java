/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haoxue.zixueplayer;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.haoxue.zixueplayer.utils.CustomDialog;

/**
 * 主Activity
 * 由于本人艺术感太差，UI界面比较丑，请自行设计
 */
public class MainActivity extends BaseActivity implements DownloadDialogFragment.DownloadSuccessListener, MyMusicListFragment.SuccessListener {


    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;

    private Drawable oldBackground = null;
    private int currentColor = 0x98000000;

    private MyMusicListFragment myMusicListFragment;
    private NetMusicListFragment netMusicListFragment;

    public MuyuPlayerApp app;//取出全局对象 方便调用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MuyuPlayerApp) getApplication();
        setContentView(R.layout.activity_main);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        pager.setPageMargin(pageMargin);

        tabs.setViewPager(pager);
        initService();
        changeColor(currentColor);
    }

    @Override
    public void publish(int progress) {
        //更新进度条
    }

    @Override
    public void change(int position) {
        //切换状态播放位置
        if (pager.getCurrentItem() == 0) {
            myMusicListFragment.loadData();
            myMusicListFragment.changeUIStatusOnPlay(position);
        } else if (pager.getCurrentItem() == 1) {
        }

    }

    private void changeColor(int newColor) {

        tabs.setIndicatorColor(newColor);

        // change ActionBar color just if an ActionBar is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            Drawable colorDrawable = new ColorDrawable(newColor);
            Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
            LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});

            if (oldBackground == null) {

                getActionBar().setBackgroundDrawable(ld);

            } else {

                TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldBackground, ld});

                getActionBar().setBackgroundDrawable(td);

                td.startTransition(200);

            }

            oldBackground = ld;

            getActionBar().setDisplayShowTitleEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);

        }


    }


    @Override
    public void downloadSuccessListener(String isDownloadSuccess) {
        System.out.println("MainActivity.downloadSuccessListener = " + isDownloadSuccess);
        if (isDownloadSuccess.length() > 0) {
            //问题:下载完成后,确实通知媒体库更新了,但是马上初始化数据,可能媒体库还没有更新
            //临时解决:在ActionBar上,添加一个刷新按钮

            //更新本地音乐列表
            myMusicListFragment.loadData();//初始化数据
            myMusicListFragment.newInstance();//重新实例化一下本地音乐Fragment,加载新的数据
        }
    }

    @Override
    public void successListener(String isSuccess) {

        //问题:删除完成后,确实通知媒体库更新了,但是马上初始化数据,可能媒体库还没有更新
        //临时解决:在ActionBar上,添加一个刷新按钮
        myMusicListFragment.loadData();//初始化数据
        MyMusicListFragment.newInstance();//重新实例化一下本地音乐Fragment,加载新的数据

    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {getString(R.string.my_music), getString(R.string.net_music)};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (myMusicListFragment == null) {
                    myMusicListFragment = MyMusicListFragment.newInstance();
                }
                return myMusicListFragment;
            } else if (position == 1) {
                if (netMusicListFragment == null) {
                    netMusicListFragment = NetMusicListFragment.newInstance();
                }
                return netMusicListFragment;
            }
            return null;
        }

    }

    //菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.newMyMusic:
                myMusicListFragment.loadData();//初始化数据
                MyMusicListFragment.newInstance();//重新实例化一下本地音乐Fragment,加载新的数据
                break;
            case R.id.ilike:
                intent = new Intent(this, MyLikeMusicListActivity.class);
                startActivity(intent);
                break;
            case R.id.near_play:
                intent = new Intent(this, PlayRecordListActivity.class);
                startActivity(intent);
                break;
            case R.id.about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.exit:
                stopService(new Intent(this, PlayService.class));
                exit();
                break;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //保存当前播放的一些状态值
        MuyuPlayerApp app = (MuyuPlayerApp) getApplication();
        SharedPreferences.Editor editor = app.sp.edit();
        editor.putInt("currentPosition", playService.getCurrentPosition());
        editor.putInt("play_mode", playService.getPlay_mode());
        editor.commit();
    }


    /**
     * 按返回键弹出对话框确定退出
     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN )
//        {
//            // 创建退出对话框
//            AlertDialog isExit = new AlertDialog.Builder(this).create();
//            // 设置对话框标题
//            isExit.setTitle("系统提示");
//            // 设置对话框消息
//            isExit.setMessage("确定要退出吗");
//            // 添加选择按钮并注册监听
//            isExit.setButton("取消", listener);
//            isExit.setButton2("确定", listener);
//            // 显示对话框
//            isExit.show();
//
//        }
//
//        return false;
//
//    }
//    /**监听对话框里面的button点击事件*/
//    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
//    {
//        public void onClick(DialogInterface dialog, int which)
//        {
//            switch (which)
//            {
//                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
//
//                    break;
//                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
//                    exit();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };

    /**
     * 按返回键弹出对话框确定退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            new CustomDialog.Builder(MainActivity.this)
                    .setTitle(R.string.info)
                    .setMessage(R.string.dialog_messenge)
                    .setPositiveButton(R.string.confrim,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    exit();

                                }
                            }).setNeutralButton(R.string.cancel, null).show();
            return false;
        }
        return false;
    }

    /**
     * Notification管理
     */
    public NotificationManager mNotificationManager;

    /**
     * 初始化要用到的系统服务
     */
    private void initService() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
}