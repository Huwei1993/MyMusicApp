package com.haoxue.zixueplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.haoxue.zixueplayer.R;
import com.haoxue.zixueplayer.utils.MediaUtils;
import com.haoxue.zixueplayer.vo.Mp3Info;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/27.
 * 本地列表适配器
 */
public class MyMusicListAdapter extends BaseAdapter {

    private Context ctx;
    private ArrayList<Mp3Info> mp3Infos;
    private Mp3Info mp3Info;		//Mp3Info对象引用
    private int pos = -1;			//列表位置

    public MyMusicListAdapter(Context ctx, ArrayList<Mp3Info> mp3Infos) {
        this.ctx = ctx;
        this.mp3Infos = mp3Infos;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }
    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView= LayoutInflater.from(ctx).inflate(R.layout.item_music_list,null);
            vh=new ViewHolder();
            vh.textView1_title= (TextView) convertView.findViewById(R.id.textView1_title);
            vh.textView2_singer= (TextView) convertView.findViewById(R.id.textView2_singer);
            vh.textView3_time= (TextView) convertView.findViewById(R.id.textView3_time);
            convertView.setTag(vh);
        }else {
            vh= (ViewHolder) convertView.getTag();
        }
        Mp3Info mp3Info=mp3Infos.get(position);
        vh.textView1_title.setText(mp3Info.getTitle());
        vh.textView2_singer.setText(mp3Info.getArtist());
        vh.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));


        return convertView;
    }


    static class ViewHolder{
        TextView textView1_title;
        TextView textView2_singer;
        TextView textView3_time;
    }
}
