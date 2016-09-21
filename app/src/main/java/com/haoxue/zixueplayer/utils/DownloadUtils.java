package com.haoxue.zixueplayer.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.haoxue.zixueplayer.vo.Mp3Info;
import com.haoxue.zixueplayer.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/8/2.
 */
public class DownloadUtils {

    public static final int SUCCESS_LRC = 1;//下载歌词成功
    public static final int FAILED_LRC = 2;//下载歌词失败
    public static final int SUCCESS_MP3 = 3;//下载MP3成功
    public static final int FAILED_MP3 = 4;//下载MP3失败
    public static final int GET_MP3_URL = 5;//获取MP3 URL成功
    public static final int GET_FAILED_MP3_URL = 6;//获取MP3 URL失败
    public static final int MUSIC_EXISTS = 7;//音乐已存在

    private static DownloadUtils sInstance;
    private OnDownloadListener mListener;

    private ExecutorService mThreadPool;

    private Mp3Info mp3Info;

    //设置回调的监听器对象
    public DownloadUtils setListener(OnDownloadListener mListener) {
        this.mListener = mListener;
        return this;
    }

    //获取下载功能的实例
    public synchronized static DownloadUtils getsInstance() {
        if (sInstance == null) {
            try {
                sInstance = new DownloadUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private DownloadUtils() throws ParserConfigurationException {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    //下载的具体业务方法
    public void download(final SearchResult searchResult) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SUCCESS_LRC:
                        if (mListener != null) mListener.onDownload(searchResult.getMusicName()+"——"+searchResult.getArtist() + ".mp3");
                        break;
                    case FAILED_LRC:
                        if (mListener != null) mListener.onFailed(searchResult.getMusicName()+"的歌词下载失败");
                        break;
                    case GET_MP3_URL:
                        System.out.println(msg.obj);
                        downloadMusic(searchResult, (String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener != null) mListener.onFailed(searchResult.getMusicName()+"的MP3下载失败");
                        break;
                    case SUCCESS_MP3:
                        if (mListener != null) mListener.onDownload(searchResult.getMusicName()+"——"+searchResult.getArtist() + ".mp3");
//                        String url = Constant.MIGU_URL + searchResult.getUrl();
//                        System.out.println("download lrc:" + url);
                        downloadLRC(searchResult.getMusicName(), searchResult.getArtist(), this);
                        break;
                    case FAILED_MP3:
                        if (mListener != null)
                            mListener.onFailed(searchResult.getMusicName()+"的MP3下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener != null) mListener.onFailed(searchResult.getMusicName()+"已存在");
                        break;
                }
            }
        };

        getDownloadMusicURL(searchResult, handler);
    }

    //下载MP3音乐的具体方法
    private void downloadMusic(final SearchResult searchResult, final String url, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_MUSIC);
                if (!musicDirFile.exists()) {
                    musicDirFile.mkdir();
                }
                String mp3url = url;
                String target = musicDirFile + "/" + searchResult.getMusicName()+"——"+searchResult.getArtist() + ".mp3";
//                System.out.println(mp3url);
//                System.out.println(target);
                File fileTarget = new File(target);
                if (fileTarget.exists()) {
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                } else {
                    //使用OkHttpClient
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3url).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });
    }





    //下载歌词
    public void downloadLRC(final String musicName, final String artistName, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {


                //从浏览器复制出来的Url是这样的,"http://music.baidu.com/search?key=%E6%B2%A1%E6%9C%89";
                //汉字经过utf8编码,比如 冰雨 == %E5%86%B0%E9%9B%A8;
                //经过测试 获取页面 使用"http://music.baidu.com/search?key=冰雨";无法打开正确连接
                //比如使用URLEncoder.encode转码,转为utf8
                //实际使用 获取页面 使用"http://music.baidu.com/search?key=%E6%B2%A1%E6%9C%89";
                try {
//                    String musicNameEn = URLEncoder.encode(musicName, "utf8");
//                    String artistNameEn = URLEncoder.encode(artistName, "utf8");
//                    String url = Constant.BAIDU_LRC_SEARCH_HEAD + searchResult.getMusicName() + " " + searchResult.getArtist();
                    String url = Constant.BAIDU_LRC_SEARCH_HEAD + musicName + "+" + artistName;
//                    System.out.println("歌词下载页面url = " + url);

                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    //System.out.println("歌词下载页面 doc : " + doc);

                    Elements lrcUrls = doc.select("span.lyric-action");
                    System.out.println(lrcUrls);

                    for (int i = 0; i < lrcUrls.size(); i++) {
                        Elements urlsa = lrcUrls.get(i).getElementsByTag("a");
                        System.out.println("tag a urlsa : " + urlsa);
                        for (int a = 0; a < urlsa.size(); a++) {
                            //System.out.println("----" + urlsa.get(a).toString());
                            String urla = urlsa.get(a).toString();
                            System.out.println("-----" + urla);
                            //-----<a class="down-lrc-btn { 'href':'/data2/lrc/14488216/14488216.lrc' }" href="#">下载LRC歌词</a>
                            if (urla.indexOf("'href':'") > 0) {
                                String[] uu = urla.split("'href':'");
                                System.out.println("uu1 : " + uu[1]);
                                //uu1 : /data2/lrc/14488216/14488216.lrc' }" href="#">下载LRC歌词</a>
                                String[] uuu = uu[1].split(".lrc'");
                                System.out.println("uuu0 : " + uuu[0]);
                                //uuu0 : /data2/lrc/246970367/246970367.lrc
                                String lrcDwonUrl = "http://music.baidu.com" + uuu[0] + ".lrc";
                                System.out.println("lrcDwonUrl : " + lrcDwonUrl);
                                //result :  http://music.baidu.com/data2/lrc/14488216/14488216.lrc


                                //File LrcDirFile = new File(Environment.getExternalStorageDirectory() + "/drm_music");
                                File LrcDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_LRC);
                                System.out.println("LrcDirFile : " + LrcDirFile);
                                if (!LrcDirFile.exists()) {
                                    LrcDirFile.mkdirs();
                                }
                                String target = LrcDirFile + "/" + musicName +"——"+artistName+ ".lrc";
                                System.out.println("lrcDwonUrl : " + lrcDwonUrl);
                                System.out.println("target : " + target);
                                File fileTarget = new File(target);
                                if (fileTarget.exists()) {
                                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                                    return;
                                } else {
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder().url(lrcDwonUrl).build();
                                    try {
                                        Response response = client.newCall(request).execute();
                                        if (response.isSuccessful()) {
                                            PrintStream ps = new PrintStream(new File(target));
                                            byte[] bytes = response.body().bytes();
                                            ps.write(bytes, 0, bytes.length);
                                            ps.close();
                                            handler.obtainMessage(SUCCESS_LRC, target).sendToTarget();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        handler.obtainMessage(FAILED_LRC).sendToTarget();
                                    }

                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    //获取下载音乐的URL
    private void getDownloadMusicURL(final SearchResult searchResult, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] aa = searchResult.getUrl().split("/");
                    String sn = aa[5];
                    String url = Constant.MIGU_DOWN_HEAD + sn + Constant.MIGU_DOWN_FOOT;
                    System.out.println(url);
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();

//                    System.out.println("doc.toString() = " + doc.toString());
                    String[] bb = doc.toString().split("song");//把 下载页面源码 按照"song"分割
                    for (int i = 0; i < bb.length; i++) {
                        System.out.println("bb[" + i + "] = " + bb[i]);
                        if (bb[i].indexOf("mp3?msisdn") > 0) {
                            System.out.println("mp3?msisdn = " + bb[i]);
                            String initMp3Url = bb[i];//initMp3Url 初始Mp3下载链接,如下
                            //mp3?msisdn = ":"http://tyst.migu.cn/public/ringmaker01/10月31日中文延期/文件/全套格式/9000首/全曲试听/Mp3_128_44_16/一起走过的日子-刘德华.mp3?msisdn\u003d7b609763f0ff","

                            String[] arrayHttp = initMp3Url.split("http");//把 初始Mp3下载链接 按照"http"分割
                            String[] arrayMp3 = arrayHttp[1].split(".mp3");//把 arrayHttp 按照".mp3"分割
                            String result = "http" + arrayMp3[0] + ".mp3";//把分割去掉的"http"和".mp3",组合回来
//                            System.out.println("DownloadUtils.getDownloadMusicURL.result = " + result);

                            //String result = "http://tyst.migu.cn/public/ringmaker01/10月31日中文延期/文件/全套格式/9000首/全曲试听/Mp3_128_44_16/一起走过的日子-刘德华.mp3";
                            Message msg = handler.obtainMessage(GET_MP3_URL, result);
                            msg.sendToTarget();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }
            }
        });
    }


    //自定义下载事件监听器
    public interface OnDownloadListener {
        public void onDownload(String mp3Url);

        public void onFailed(String error);
    }
}
