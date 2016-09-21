package com.haoxue.zixueplayer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haoxue.zixueplayer.adapter.MyMusicListAdapter;
import com.haoxue.zixueplayer.vo.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/30.
 */
public class MyLikeMusicListActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView listView_like;
    private TextView textView1_no_data;
    private MuyuPlayerApp app;
    private MyMusicListAdapter myMusicListAdapter;
    private Mp3Info mp3Info;
    private ArrayList<Mp3Info> likeMp3Infos;
    private MyMusicListAdapter adapter;
    private boolean isChange = false;//表示当前播放列表是否为收藏列表

    List<Mp3Info> list;

    private Drawable oldBackground = null;
    private int currentColor = 0x98000000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MuyuPlayerApp) getApplication();
        setContentView(R.layout.activity_like_music_list);
        listView_like = (ListView) findViewById(R.id.listView_like);
        textView1_no_data = (TextView) findViewById(R.id.textView1_no_data);
        listView_like.setOnItemClickListener(this);
        registerForContextMenu(listView_like);

        changeColor(currentColor);
        initData();
        listView_like.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                showLikeDeleteDialog(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MyLikeMusicListActivity.this);
                final String[] items = {"取消收藏"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mp3Info=likeMp3Infos.get(position);
                        try {
                            System.out.println(mp3Info);
                            System.out.println(mp3Info.getIsLike());
                            mp3Info.setIsLike(0);
                            app.dbUtils.update(mp3Info, "isLike");
                            System.out.println(mp3Info.getIsLike());
                            System.out.println(mp3Info);
                            initData();
                            Toast.makeText(MyLikeMusicListActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定播放服务
        unbindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindPlayService();//解绑服务
    }

    private void initData() {
        try {
            list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike", "=", "1"));
            if (list == null || list.size() == 0) {
                textView1_no_data.setVisibility(View.VISIBLE);
                listView_like.setVisibility(View.GONE);
            } else {
                textView1_no_data.setVisibility(View.GONE);
                listView_like.setVisibility(View.VISIBLE);
                likeMp3Infos = (ArrayList<Mp3Info>) list;
                adapter = new MyMusicListAdapter(this, likeMp3Infos);
                listView_like.setAdapter(adapter);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    private void deleteData(){
        //查询收藏的记录
        try {
            list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike", "=", "1"));
            for (int i = 0; i <list.size() ; i++) {
                mp3Info=list.get(i);
                mp3Info.setIsLike(0);
                app.dbUtils.update(mp3Info, "isLike");
            }
            initData();
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.LIKE_MUSIC_LIST) {
            playService.setMp3Infos(likeMp3Infos);
            playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
        }
        playService.play(position);
        Mp3Info likeMp3Info = likeMp3Infos.get(position);//查出歌曲
        System.out.println("收藏列表 : " + likeMp3Info);
        //保存播放时间
        savePlayRecord();
    }

    //保存播放记录
    private void savePlayRecord() {
        //获取当前正在播放的音乐对象
        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            if (playRecordMp3Info == null) {
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                app.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
    private void changeColor(int newColor) {


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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.like_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.near_play_Play:
                intent = new Intent(this, PlayRecordListActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.delete_list:
                deleteData();
                break;
        }
        return true;
    }


}
