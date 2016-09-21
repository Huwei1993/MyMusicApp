package com.haoxue.zixueplayer;

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

import com.haoxue.zixueplayer.adapter.MyMusicListAdapter;
import com.haoxue.zixueplayer.utils.Constant;
import com.haoxue.zixueplayer.vo.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/30.
 */
public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView listView_paly_record;
    private TextView textView2_no_data;
    private MuyuPlayerApp app;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter adapter;
    private Mp3Info mp3Info;

    private Drawable oldBackground = null;
    private int currentColor = 0x98000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app= (MuyuPlayerApp) getApplication();
        setContentView(R.layout.activity_play_record_list);
        listView_paly_record= (ListView) findViewById(R.id.listView_paly_record);
        textView2_no_data= (TextView) findViewById(R.id.textView2_no_data);
        listView_paly_record.setOnItemClickListener(this);
        initData();
        changeColor(currentColor);
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

    //初始化最近播放的数据
    private void initData() {
        try {
            //查询最近播放的记录
            List<Mp3Info> list=app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime","!=",0).orderBy("playTime",true).limit(Constant.PLAY_RECORD_NUM));
//            System.out.println(list);
            if (list == null || list.size()==0) {
                textView2_no_data.setVisibility(View.VISIBLE);
                listView_paly_record.setVisibility(View.GONE);
            }else {
                textView2_no_data.setVisibility(View.GONE);
                listView_paly_record.setVisibility(View.VISIBLE);
                mp3Infos= (ArrayList<Mp3Info>) list;
                adapter = new MyMusicListAdapter(this, mp3Infos);
                listView_paly_record.setAdapter(adapter);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    private void deleteData(){
        //查询最近播放的记录
        try {
            List<Mp3Info> list=app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime","!=",0));
            for (int i = 0; i <list.size() ; i++) {
                mp3Info=list.get(i);
                mp3Info.setPlayTime(0);
                app.dbUtils.update(mp3Info, "playTime");
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
        if (playService.getChangePlayList()!=PlayService.PLAY_RECORD_MUSIC_LIST) {
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }
        playService.play(position);
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
        getMenuInflater().inflate(R.menu.record_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.ilike_play:
                intent = new Intent(this, MyLikeMusicListActivity.class);
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
