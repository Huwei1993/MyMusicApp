package com.haoxue.zixueplayer;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.haoxue.zixueplayer.musicfragment.NetEUSMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetHotMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetJSKMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetKTVMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetMTVMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetNATMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetNETMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetORIMusicListFragment;
import com.haoxue.zixueplayer.musicfragment.NetPURMusicListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/27.
 */
public class NetMusicListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private MainActivity mainActivity;
    private ListView listView_net;


    public NetMusicListFragment() {

    }
    ArrayList<Map<String, Object>> list = new ArrayList<>();
    HashMap<String, Object> title1 = new HashMap<>();
    HashMap<String, Object> title2 = new HashMap<>();
    HashMap<String, Object> title3 = new HashMap<>();
    HashMap<String, Object> title4 = new HashMap<>();
    HashMap<String, Object> title5 = new HashMap<>();
    HashMap<String, Object> title6 = new HashMap<>();
    HashMap<String, Object> title7 = new HashMap<>();
    HashMap<String, Object> title8 = new HashMap<>();
    HashMap<String, Object> title9 = new HashMap<>();

    public static NetMusicListFragment newInstance() {
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.net_music, null);
        //准备数据，每一个HashMap是一条记录
        listView_net = (ListView) view.findViewById(R.id.listView_net);

        title1.put("title", "华语榜");
        title1.put("icon", R.mipmap.app_logo);
        title2.put("title", "欧美榜");
        title2.put("icon", R.mipmap.app_logo);
        title3.put("title", "日韩榜");
        title3.put("icon", R.mipmap.app_logo);
        title4.put("title", "原创榜");
        title4.put("icon", R.mipmap.app_logo);
        title5.put("title", "影视榜");
        title5.put("icon", R.mipmap.app_logo);
        title6.put("title", "网络榜");
        title6.put("icon", R.mipmap.app_logo);
        title7.put("title", "民族榜");
        title7.put("icon", R.mipmap.app_logo);
        title8.put("title", "纯音乐榜");
        title8.put("icon", R.mipmap.app_logo);
        title9.put("title", "KTV点歌榜");
        title9.put("icon", R.mipmap.app_logo);


        list.add(title1);
        list.add(title2);
        list.add(title3);
        list.add(title4);
        list.add(title5);
        list.add(title6);
        list.add(title7);
        list.add(title8);
        list.add(title9);

        //把数据填充到Adapter
        SimpleAdapter sa = new SimpleAdapter(mainActivity, list, R.layout.net_music_item, new String[]{"title", "icon"}, new int[]{R.id.textView_title, R.id.imageView_icon});
        listView_net.setAdapter(sa);
        listView_net.setOnItemClickListener(this);

        //默认显示华语榜
        NetHotMusicListFragment netHotMusicListFragment = NetHotMusicListFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.net_list_frame, netHotMusicListFragment);

        //把当前Fragment添加到Activity栈
        ft.addToBackStack(null);
        ft.commit();
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (list.get(position).equals(title1)) {
            NetHotMusicListFragment netHotMusicListFragment = NetHotMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netHotMusicListFragment);
            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();

        } else if (list.get(position).equals(title2)) {
            NetEUSMusicListFragment netEUSMusicListFragment = NetEUSMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netEUSMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title3)) {
            NetJSKMusicListFragment netJSKMusicListFragment = NetJSKMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netJSKMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title4)) {
            NetORIMusicListFragment netORIMusicListFragment = NetORIMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netORIMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title5)) {
            NetMTVMusicListFragment netMTVMusicListFragment = NetMTVMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netMTVMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title6)) {
            NetNETMusicListFragment netNETMusicListFragment = NetNETMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netNETMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title7)) {
            NetNATMusicListFragment netNATMusicListFragment = NetNATMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netNATMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title8)) {
            NetPURMusicListFragment netPURMusicListFragment = NetPURMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netPURMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        } else if (list.get(position).equals(title9)) {
            NetKTVMusicListFragment netKTVMusicListFragment = NetKTVMusicListFragment.newInstance();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.net_list_frame, netKTVMusicListFragment);

            //把当前Fragment添加到Activity栈
            ft.addToBackStack(null);
            ft.commit();
        }
    }
}
