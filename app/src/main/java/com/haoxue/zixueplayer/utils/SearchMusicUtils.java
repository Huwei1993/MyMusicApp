package com.haoxue.zixueplayer.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.haoxue.zixueplayer.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

/**
 * 搜索音乐工具类
 * Created by Administrator on 2016/8/1.
 */
public class SearchMusicUtils {
    private static final String URL=Constant.MIGU_URL+Constant.MIGU1_SEARCH1;
    private static SearchMusicUtils sInstance;
    private OnSearchResultListener mListener;

    private ExecutorService mThreadPool;

    public synchronized static SearchMusicUtils getsInstance(){
        if (sInstance == null) {
            try {
                sInstance=new SearchMusicUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private SearchMusicUtils() throws ParserConfigurationException{
        mThreadPool=Executors.newSingleThreadExecutor();
    }

    public SearchMusicUtils setListener(OnSearchResultListener l){
        mListener=l;
        return this;
    }

    public void search(final String key){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Constant.SUCCESS:
                        if (mListener != null) mListener.onSearchResult((ArrayList<SearchResult>)msg.obj);
                        break;
                    case Constant.FAILED:
                        if (mListener != null) mListener.onSearchResult(null);
                        break;
                }
            }
        };

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<SearchResult> results=getMusicList(key);
                if (results == null) {
                    handler.sendEmptyMessage(Constant.FAILED);
                    return;
                }
                handler.obtainMessage(Constant.SUCCESS,results).sendToTarget();
            }
        });
    }

    //使用 Jsoup请求网络解析数据
    private ArrayList<SearchResult> getMusicList(final String key){
        final String pagenum="1";
        final String keytype="all";

        try {
            Document doc= Jsoup.connect(URL+key+Constant.MIGU1_SEARCH2)
                    .userAgent(Constant.USER_AGENT)
                    .timeout(60*1000).get();
            Elements songTitles=doc.select("[class=fl song_name]");
            Elements artists=doc.select("[class=fl singer_name mr5]");
            Elements albums=doc.select("[class=fl song_album]");
            ArrayList<SearchResult> searchResults=new ArrayList<>();
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
                searchResult.setAlbum("搜索榜");
                searchResults.add(searchResult);
            }
//            System.out.println(searchResults);
            return searchResults;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface OnSearchResultListener{
        public void onSearchResult(ArrayList<SearchResult> results);
    }
}
