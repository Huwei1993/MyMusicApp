package com.haoxue.zixueplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.haoxue.zixueplayer.utils.Constant;
import com.haoxue.zixueplayer.utils.DownloadUtils;
import com.haoxue.zixueplayer.utils.MediaUtils;
import com.haoxue.zixueplayer.utils.SearchMusicUtils;
import com.haoxue.zixueplayer.vo.Mp3Info;
import com.haoxue.zixueplayer.vo.SearchResult;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.ILrcView;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;

/**
 * Created by Administrator on 2016/7/28.
 * 音乐播放界面
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,ViewPager.OnPageChangeListener {

    private TextView textView1_title, textView1_start_time, textView1_end_time, textView1_no_music;
    private ImageView iv_music_ablum,iv_music_ablum_reflection, imageView1_next, imageView2_play_pause, imageView3_previous, imageView1_play_mode, imageView1_favorite;
    private SeekBar seekBar1;
    private ViewPager viewPager;
    private LrcView lrcView;
    private static final int UPDATE_TIME = 0x10;//更新播放时间的标记
    private static final int UPDATE_LRC = 0x20;//更新歌词
    private ArrayList<Mp3Info> mp3Infos;
    private ArrayList<View> views = new ArrayList<>();

    private Drawable oldBackground = null;
    private int currentColor = 0x98000000;


    private MuyuPlayerApp app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        app = (MuyuPlayerApp) getApplication();
        changeColor(currentColor);

//        textView1_title = (TextView) findViewById(R.id.textView1_title);
        textView1_start_time = (TextView) findViewById(R.id.textView1_start_time);
        textView1_end_time = (TextView) findViewById(R.id.textView1_end_time);

//        imageView1_album = (ImageView) findViewById(R.id.imageView1_album);
        imageView1_next = (ImageView) findViewById(R.id.ImageView1_next);
        imageView2_play_pause = (ImageView) findViewById(R.id.ImageView2_play_pause);
        imageView3_previous = (ImageView) findViewById(R.id.ImageView3_previous);
        imageView1_play_mode = (ImageView) findViewById(R.id.ImageView1_play_mode);
        imageView1_favorite = (ImageView) findViewById(R.id.ImageView1_favorite);

        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        initViewPager();

        imageView2_play_pause.setOnClickListener(this);
        imageView1_next.setOnClickListener(this);
        imageView3_previous.setOnClickListener(this);
        imageView1_play_mode.setOnClickListener(this);
        imageView1_favorite.setOnClickListener(this);

        seekBar1.setOnSeekBarChangeListener(this);


//        mp3Infos = MediaUtils.getMp3Infos(this);
        myHandler = new MyHandler(this);

    }


    private void initViewPager() {
        View album_image_layout = getLayoutInflater().inflate(R.layout.album_image_layout, null);
        iv_music_ablum = (ImageView) album_image_layout.findViewById(R.id.iv_music_ablum);
        iv_music_ablum_reflection = (ImageView) album_image_layout.findViewById(R.id.iv_music_ablum_reflection);
        textView1_title = (TextView) album_image_layout.findViewById(R.id.textView1_title);
        views.add(album_image_layout);
        View lrc_layout=getLayoutInflater().inflate(R.layout.lrc_layout, null);
        //设置滚动事件
        lrcView= (LrcView) lrc_layout.findViewById(R.id.lrcView);
        lrcView.setListener(new ILrcView.LrcViewListener() {
            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (playService.isPlaying()){
                    playService.seekTo((int) row.time);
//                }else {
//                    playService.play(newPosition);
//                    playService.seekTo((int) row.time);
                }
            }
        });
        lrcView.setLoadingTipText("正在加载歌词");
        lrcView.setBackgroundResource(R.mipmap.jb_bg);
        lrcView.getBackground().setAlpha(150);
        views.add(lrc_layout);
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindPlayService();//解绑服务
    }

    private static MyHandler myHandler;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            playService.seekTo(progress);
            textView1_start_time.setText(MediaUtils.formatTime(progress));
        }
    }

    int tag;

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (playService.isPlaying()) {
            playService.pause();
            tag = 1;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (tag == 1) {
            playService.start();
        } else {
            playService.pause();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    //加载歌词
    private void loadLRC(File lrcFile){
        StringBuffer buf=new StringBuffer(1024*10);
        char[] chars=new char[1024];
        try {
            BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile)));
            int len=-1;
            while((len=in.read(chars))!=-1){
                buf.append(chars,0,len);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ILrcBuilder builder=new DefaultLrcBuilder();
        List<LrcRow> rows=builder.getLrcRows(buf.toString());
        lrcView.setLrc(rows);
        //加载专辑封面图片为背景的方法(实际使用,效果不理想)
//        long id = mp3Info.getMp3InfoId()==0?mp3Info.getId:mp3Info.getMp3InfoId();
//        Bitmap bg = MediaUtils.getArtwork(this, id ,mp3Info.getAlbumId(),false,false);
//        if(bg != null){
//            lrcView.getBackground(new BitmapDrawable(getResources(),bg));
//            lrcView.getBackground().setAlpha(120);
//        }
    }


    static class MyHandler extends Handler {
        private PlayActivity playActivity;
        private WeakReference<PlayActivity> weak;//弱引用

        public MyHandler(PlayActivity playActivity) {
            weak=new WeakReference<PlayActivity>(playActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            playActivity=weak.get();
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.textView1_start_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                    case UPDATE_LRC:
                        playActivity.lrcView.seekLrcToTime((int)msg.obj);
                        break;
                    case DownloadUtils.SUCCESS_LRC:
                        playActivity.loadLRC(new File((String)msg.obj));
                        break;
                    case DownloadUtils.FAILED_LRC:
                        Toast.makeText(playActivity, "歌词下载失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void publish(int progress) {

//        textView1_start_time.setText(MediaUtils.formatTime(progress));
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
//        myHandler.obtainMessage(UPDATE_TIME,progress).sendToTarget();
        seekBar1.setProgress(progress);
        myHandler.obtainMessage(UPDATE_LRC,progress).sendToTarget();
    }

    @Override
    public void change(int position) {
        Mp3Info mp3Info = playService.mp3Infos.get(position);
        textView1_title.setText(mp3Info.getTitle());

        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        Animation albumanim = AnimationUtils.loadAnimation(this, R.anim.album_replace);
        //开始播放动画效果
        iv_music_ablum.startAnimation(albumanim);
        iv_music_ablum.setImageBitmap(albumBitmap);
        iv_music_ablum_reflection.setImageBitmap(MediaUtils.createReflectionBitmapForSingle(albumBitmap));
        textView1_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        seekBar1.setProgress(0);
        seekBar1.setMax((int) mp3Info.getDuration());

        if (playService.isPlaying()) {
            imageView2_play_pause.setImageResource(R.mipmap.pause);
        } else {
            imageView2_play_pause.setImageResource(R.mipmap.play);
        }
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                imageView1_play_mode.setImageResource(R.mipmap.order);
                imageView1_play_mode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                imageView1_play_mode.setImageResource(R.mipmap.random);
                imageView1_play_mode.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                imageView1_play_mode.setImageResource(R.mipmap.single);
                imageView1_play_mode.setTag(PlayService.SINGLE_PLAY);
                break;
        }

        //初始化收藏状态
        try {
            Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", getId(mp3Info)));
            if (likeMp3Info != null) {
                int isLike = likeMp3Info.getIsLike();
                if (isLike == 1) {
                    imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                } else {
                    imageView1_favorite.setImageResource(R.mipmap.xin_bai);
                }
            } else {
                imageView1_favorite.setImageResource(R.mipmap.xin_bai);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        //歌词
        String songName=mp3Info.getTitle();
        String artistName = mp3Info.getArtist();
        String lrcPath= Environment.getExternalStorageDirectory()+ Constant.DIR_LRC+"/"+songName+".lrc";
        File lrcFile=new File(lrcPath);
        if (!lrcFile.exists()){
            //下载
            SearchMusicUtils.getsInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {
                @Override
                public void onSearchResult(ArrayList<SearchResult> results) {
                    SearchResult searchResult=results.get(0);
                    DownloadUtils.getsInstance().downloadLRC(searchResult.getMusicName(),searchResult.getArtist(),myHandler);
                }
            }).search(songName+""+artistName);
        }else {
            loadLRC(lrcFile);
        }

    }

    private long getId(Mp3Info mp3Info) {
        //初始收藏状态
        long id = 0;
        switch (playService.getChangePlayList()) {
            case PlayService.MY_MUSIC_LIST:
                id = mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id = mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ImageView2_play_pause: {
                if (playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.pause);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            }
            case R.id.ImageView1_next: {
                playService.next();
                break;
            }
            case R.id.ImageView3_previous: {
                playService.prev();
                break;
            }
            case R.id.ImageView1_play_mode: {
                int mode = (int) imageView1_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        imageView1_play_mode.setImageResource(R.mipmap.random);
                        imageView1_play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        imageView1_play_mode.setImageResource(R.mipmap.single);
                        imageView1_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        imageView1_play_mode.setImageResource(R.mipmap.order);
                        imageView1_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            }
            case R.id.ImageView1_favorite: {
                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                System.out.println(mp3Info);
                try {
                    Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", getId(mp3Info)));
                    System.out.println(likeMp3Info);
                    if (likeMp3Info == null) {
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
//                        System.out.println(mp3Info);
                        app.dbUtils.save(mp3Info);
                        System.out.println("save");
                        imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                        Toast.makeText(PlayActivity.this, "已添加收藏", Toast.LENGTH_SHORT).show();
                    } else {
                        int isLike = likeMp3Info.getIsLike();
                        if (isLike == 1) {
                            likeMp3Info.setIsLike(0);
                            imageView1_favorite.setImageResource(R.mipmap.xin_bai);
                            Toast.makeText(PlayActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
                        } else {
                            likeMp3Info.setIsLike(1);
                            imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                            Toast.makeText(PlayActivity.this, "已添加收藏", Toast.LENGTH_SHORT).show();
                        }
                        System.out.println("update");
                        app.dbUtils.update(likeMp3Info, "isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.play_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.ilike_play:
                intent = new Intent(this, MyLikeMusicListActivity.class);
                startActivity(intent);
                break;
            case R.id.near_play_Play:
                intent = new Intent(this, PlayRecordListActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    //适配器
    class MyPagerAdapter extends PagerAdapter {

        //获取总数
        @Override
        public int getCount() {
            return views.size();
        }

        //实例化选项卡
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            return v;
        }

        //删除选项卡
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        //判断视图是否为返回的对象
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
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
}
