package com.haoxue.zixueplayer;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.haoxue.zixueplayer.adapter.MyMusicListAdapter;
import com.haoxue.zixueplayer.utils.Constant;
import com.haoxue.zixueplayer.utils.ConstantUtil;
import com.haoxue.zixueplayer.utils.CustomDialog;
import com.haoxue.zixueplayer.utils.MediaUtils;
import com.haoxue.zixueplayer.vo.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/27.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener,AdapterView.OnItemLongClickListener {

    private ListView listView_my_music;
    private ImageView imageView_album;
    private TextView textView_songName, textView2_singer;
    private ImageView imageView2_play_pause, imageView3_next;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter myMusicListAdapter;

    private MuyuPlayerApp app;
    private MainActivity mainActivity;

    private int listposition = 0;// 标识列表位置



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout, null);
        listView_my_music = (ListView) view.findViewById(R.id.listView_my_music);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView2_play_pause = (ImageView) view.findViewById(R.id.imageView2_play_pause);
        imageView3_next = (ImageView) view.findViewById(R.id.imageView3_next);
        textView_songName = (TextView) view.findViewById(R.id.textView_songName);
        textView2_singer = (TextView) view.findViewById(R.id.textView2_singer);

        listView_my_music.setOnItemClickListener(this);
        listView_my_music.setOnItemLongClickListener(this);
        imageView2_play_pause.setOnClickListener(this);
        imageView3_next.setOnClickListener(this);
        imageView_album.setOnClickListener(this);


//        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除绑定播放服务
        mainActivity.unbindPlayService();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //暂改
        mainActivity.unbindPlayService();
    }

    /**
     * 加载本地音乐列表
     */
    public void loadData() {
        mp3Infos = MediaUtils.getMp3Infos(mainActivity);
        //mp3Infos=mainActivity.playService.mp3Infos;
        myMusicListAdapter = new MyMusicListAdapter(mainActivity, mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList() != PlayService.MY_MUSIC_LIST) {
            mp3Infos = MediaUtils.getMp3Infos(mainActivity);//获取Mp3列表
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        this.listposition=position;
        mainActivity.playService.play(listposition);

        Mp3Info mp3Info = mp3Infos.get(position);
        System.out.println("本地列表 : " + mp3Info);
        //保存播放时间
        savePlayRecord();
    }

    //保存播放记录
    private void savePlayRecord() {
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = mainActivity.app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getId()));
            if (playRecordMp3Info == null) {
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                mainActivity.app.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    //回调播放状态下的UI设置
    public void changeUIStatusOnPlay(int position) {
        if (position >= 0 && position < mainActivity.playService.mp3Infos.size()) {
            Mp3Info mp3Info = mainActivity.playService.mp3Infos.get(position);
            textView_songName.setText(mp3Info.getTitle());
            textView2_singer.setText(mp3Info.getArtist());
            if (mainActivity.playService.isPlaying()) {
                imageView2_play_pause.setImageResource(R.mipmap.pause);
            } else {
                imageView2_play_pause.setImageResource(R.mipmap.play);
            }
            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);
            this.listposition = position;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView2_play_pause: {
                if (mainActivity.playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();
                } else {
                    if (mainActivity.playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                    } else {
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
                break;
            }
            case R.id.imageView3_next: {
                mainActivity.playService.next();
                break;
            }
            case R.id.imageView_album: {
                Intent intent = new Intent(mainActivity, PlayActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        this.listposition = position;
        musicListItemDialog();
        return true;
    }

    /**
     * 自定义对话框
     */
    public void musicListItemDialog() {
        String[] menuItems = new String[]{"播放音乐", "设为铃声", "查看详情","删除音乐"};
        ListView menuList = new ListView(mainActivity);
        menuList.setCacheColorHint(Color.TRANSPARENT);
        menuList.setDividerHeight(1);
        menuList.setAdapter(new ArrayAdapter<String>(mainActivity, R.layout.context_dialog_layout, R.id.dialogText, menuItems));
        menuList.setLayoutParams(new ViewGroup.LayoutParams(ConstantUtil
                .getScreen(mainActivity)[0] / 2, ViewGroup.LayoutParams.WRAP_CONTENT));

        final CustomDialog customDialog = new CustomDialog.Builder(mainActivity).setTitle(R.string.operation)
                .setView(menuList).create();
        customDialog.show();

        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                customDialog.cancel();
                customDialog.dismiss();
                if (position == 0) {
                    mainActivity.playService.play(listposition);
                } else if (position == 1) {
                    setRing();
                } else if (position == 2) {
                    showMusicInfo(listposition);
                } else if (position == 3) {
                    deleteMusic(listposition);
                }
            }

        });
    }

    private Mp3Info mp3Info = new Mp3Info();
    private void deleteMusic(int listposition) {
        mp3Info=mp3Infos.get(listposition);
        final String name = mp3Info.getDisplayName();
        final String url=mp3Info.getUrl();


        new CustomDialog.Builder(mainActivity)
                .setTitle(R.string.prompt)
                .setMessage("你确定彻底删除“"+name+"”吗？")
                .setPositiveButton(R.string.confrim,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                dialog.dismiss();
                                File mf = new File(url);
                                if(mf.exists()){
                                    mf.delete();
                                    System.out.println(url);
                                    Toast.makeText(mainActivity, "已彻底成功删除", Toast.LENGTH_LONG).show();
                                    SuccessListener listener =mainActivity;  // 回调接口
                                    listener.successListener(String.valueOf(mp3Infos.size())); // 回传一个字符串 ,回传什么都行 ,只是告诉MainActivity ,已经下载成功了新的歌曲

                                    updateGallery(url);
                                }else {

                                    Toast.makeText(mainActivity, "该文件不存在", Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNeutralButton(R.string.cancel,null).show();
    }



    // 回调接口
    public interface SuccessListener {
        void successListener(String isSuccess); // 回传一个字符串
    }

    /**
     * 通知媒体库更新文件
     *
     */
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

    /**
     * 设置铃声
     */
    protected void setRing() {
        RadioGroup rg_ring = new RadioGroup(mainActivity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rg_ring.setLayoutParams(params);
        // 第一个单选按钮，来电铃声
        final RadioButton rbtn_ringtones = new RadioButton(mainActivity);
        rbtn_ringtones.setText("来电铃声");
        rbtn_ringtones.setTextColor(0xffffffff);
        rg_ring.addView(rbtn_ringtones, params);
        // 第二个单选按钮，闹铃铃声
        final RadioButton rbtn_alarms = new RadioButton(mainActivity);
        rbtn_alarms.setText("闹铃铃声");
        rbtn_alarms.setTextColor(0xffffffff);
        rg_ring.addView(rbtn_alarms, params);
        // 第三个单选按钮，通知铃声
        final RadioButton rbtn_notifications = new RadioButton(
                mainActivity);
        rbtn_notifications.setText("通知铃声");
        rbtn_notifications.setTextColor(0xffffffff);
        rg_ring.addView(rbtn_notifications, params);
        new CustomDialog.Builder(mainActivity).setTitle("设置铃声")
                .setView(rg_ring)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                        if (rbtn_ringtones.isChecked()) {
                            try {
                                // 设置来电铃声
                                setRingtone(listposition);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (rbtn_alarms.isChecked()) {
                            try {
                                // 设置闹铃
                                setAlarm(listposition);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (rbtn_notifications.isChecked()) {
                            try {
                                // 设置通知铃声
                                setNotifaction(listposition);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }

    /**
     * 设置提示音
     *
     * @param position
     */
    protected void setNotifaction(int position) {
        Mp3Info mp3Info = mp3Infos.get(position);
        File sdfile = new File(mp3Info.getUrl().substring(4));
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
                .getAbsolutePath());
        Uri newUri = mainActivity.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(mainActivity,
                RingtoneManager.TYPE_NOTIFICATION, newUri);
        Toast.makeText(mainActivity, "设置通知铃声成功！", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * 设置闹铃
     *
     * @param position
     */
    protected void setAlarm(int position) {
        Mp3Info mp3Info = mp3Infos.get(position);
        File sdfile = new File(mp3Info.getUrl().substring(4));
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
                .getAbsolutePath());
        Uri newUri = mainActivity.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(mainActivity,
                RingtoneManager.TYPE_ALARM, newUri);
        Toast.makeText(mainActivity, "设置闹钟铃声成功！", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * 设置来电铃声
     *
     * @param position
     */
    protected void setRingtone(int position) {
        Mp3Info mp3Info = mp3Infos.get(position);
        File sdfile = new File(mp3Info.getUrl().substring(4));
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, false);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile
                .getAbsolutePath());
        Uri newUri = mainActivity.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(mainActivity,
                RingtoneManager.TYPE_RINGTONE, newUri);
        Toast.makeText(mainActivity, "设置来电铃声成功！", Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * 显示音乐详细信息
     *
     * @param position
     */
    private void showMusicInfo(int position) {
        Mp3Info mp3Info = mp3Infos.get(position);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(mainActivity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.music_info_layout, null);
        ((TextView) view.findViewById(R.id.tv_song_title)).setText(mp3Info
                .getTitle());
        ((TextView) view.findViewById(R.id.tv_song_artist)).setText(mp3Info
                .getArtist());
        ((TextView) view.findViewById(R.id.tv_song_album)).setText(mp3Info
                .getAlbum());
        ((TextView) view.findViewById(R.id.tv_song_filepath)).setText(mp3Info
                .getUrl());
        ((TextView) view.findViewById(R.id.tv_song_duration)).setText(MediaUtils
                .formatTime(mp3Info.getDuration()));
        ((TextView) view.findViewById(R.id.tv_song_format)).setText(Constant
                .getSuffix(mp3Info.getDisplayName()));
        ((TextView) view.findViewById(R.id.tv_song_size)).setText(Constant
                .formatByteToMB(mp3Info.getSize()) + "MB");
        new CustomDialog.Builder(mainActivity).setTitle("歌曲详细信息:")
                .setNeutralButton("确定", null).setView(view).create().show();
    }

}
