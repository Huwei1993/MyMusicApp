package com.haoxue.zixueplayer.musicfragment;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haoxue.zixueplayer.DownloadDialogFragment;
import com.haoxue.zixueplayer.MainActivity;
import com.haoxue.zixueplayer.R;
import com.haoxue.zixueplayer.adapter.NetMusicAdapter;
import com.haoxue.zixueplayer.utils.AppUtils;
import com.haoxue.zixueplayer.utils.Constant;
import com.haoxue.zixueplayer.utils.SearchMusicUtils;
import com.haoxue.zixueplayer.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/27.
 */
public class NetEUSMusicListFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener{

    private MainActivity mainActivity;
    private ListView listView_net_music;
    private LinearLayout load_layout;
    private LinearLayout ll_search_btn_container;
    private LinearLayout ll_search_container;
    private ImageButton ib_search_btn;
    private EditText et_search_content;
    private ArrayList<SearchResult> searchResults=new ArrayList<>();
    private NetMusicAdapter netMusicAdapter;
//    private int page=1;//搜索音乐的页码

    private TextView title_title;

    public static NetEUSMusicListFragment newInstance() {
        NetEUSMusicListFragment net = new NetEUSMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        //UI组件的初始化
        View view=inflater.inflate(R.layout.net_music_list,null);
        listView_net_music= (ListView) view.findViewById(R.id.listView_net_music);
        load_layout= (LinearLayout) view.findViewById(R.id.load_layout);
        ll_search_btn_container= (LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ll_search_container= (LinearLayout) view.findViewById(R.id.ll_search_container);
        ib_search_btn= (ImageButton) view .findViewById(R.id.ib_search_btn);
        et_search_content= (EditText) view .findViewById(R.id.et_search_content);

        title_title= (TextView) view.findViewById(R.id.title_title);

        title_title.setText("欧美榜");

        listView_net_music.setOnItemClickListener(this);
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);
        loadNetData();//加载网络音乐
        return view;
    }

    private void loadNetData() {
        load_layout.setVisibility(View.VISIBLE);//显示
        //执行异步加载网络音乐的任务
        new LoadNetDataTask().execute(Constant.MIGU_URL+Constant.MIGU_DAYEUS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_search_btn_container:
                ll_search_btn_container.setVisibility(View.GONE);
                ll_search_container.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                //搜索事件处理
                searchMusic();
                break;
        }
    }

    //搜索音乐
    private void searchMusic() {
        //隐藏输入法
        AppUtils.hideInputMethod(et_search_content);
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        String key=et_search_content.getText().toString();
        if (TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity, "请输入歌名或歌手", Toast.LENGTH_SHORT).show();
            return;
        }
        load_layout.setVisibility(View.VISIBLE);

        SearchMusicUtils.getsInstance().setListener(new SearchMusicUtils.OnSearchResultListener(){
            @Override
            public void onSearchResult(ArrayList<SearchResult> results){
//                System.out.println(results);
                if (results == null) {
                    load_layout.setVisibility(View.GONE);
                    Toast.makeText(mainActivity, "没有搜索到相关歌曲", Toast.LENGTH_SHORT).show();
                }else {
                    ArrayList<SearchResult> sr=netMusicAdapter.getSearchResults();
                    sr.clear();
                    sr.addAll(results);
                    netMusicAdapter.notifyDataSetChanged();
                    load_layout.setVisibility(View.GONE);
                }
            }
        }).search(key);
    }


    //列表项的单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= netMusicAdapter.getSearchResults().size()||position<0) return;
        showDownloadDialog(position);

    }

    //下载弹窗
    private void showDownloadDialog(final int position) {
        DownloadDialogFragment downloadDialogFragment=DownloadDialogFragment.newInstance(searchResults.get(position));
        downloadDialogFragment.show(getFragmentManager(),"download");
    }

    /**
     * 加载网络音乐的异步任务
     */
    class LoadNetDataTask extends AsyncTask<String,Integer,Integer>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            load_layout.setVisibility(View.VISIBLE);
            title_title.setVisibility(View.GONE);
            listView_net_music.setVisibility(View.GONE);
            searchResults.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url=params[0];
            try {
                //使用Jsoup组件请求网络，并解析音乐数据
                Document doc= Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6*1000).get();
//                System.out.println(doc);

                Elements songTitles=doc.select("[class=fl song_name text_clip]");
                Elements artists=doc.select("[class=fl singer_name mr5 text_clip]");
//                System.out.println("songTitles"+songTitles.toString());
//                System.out.println("artists"+artists.toString());
                for (int i = 0; i <songTitles.size() ; i++) {
                    SearchResult searchResult=new SearchResult();
                    Elements urls=songTitles.get(i).getElementsByTag("a");
                    Elements artistElements=artists.get(i).getElementsByTag("a");
                    String URL=urls.get(0).attr("href");
                    String MusicName=urls.get(0).text();
                    String Artist=artistElements.get(0).text();
                    searchResult.setUrl(URL);
                    searchResult.setMusicName(MusicName);
                    searchResult.setArtist(Artist);
                    searchResult.setAlbum("欧美榜");
                    searchResults.add(searchResult);
                }
//                System.out.println("列表"+searchResults);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                netMusicAdapter=new NetMusicAdapter(mainActivity,searchResults);
//                System.out.println(searchResults);
                listView_net_music.setAdapter(netMusicAdapter);
                listView_net_music.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout,null));
            }
            load_layout.setVisibility(View.GONE);
            title_title.setVisibility(View.VISIBLE);
            listView_net_music.setVisibility(View.VISIBLE);
        }
    }
}
