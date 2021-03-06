package com.delsart.bookdownload.searchengine;


import android.os.Message;
import android.util.Log;

import com.delsart.bookdownload.listandadapter.mlist;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by Delsart on 2017/7/22.
 */

public class m360d extends baseFragment {
    String url;
    byte page = 1;

    public m360d() {
        super();
        Log.d("a", "m360d: ");
    }

    public void get(String url) throws Exception {
        clean();
        this.url = url;
        getpage(url);
    }

    @Override
    public void clean() {
        super.clean();
        page = 1;
    }


    @Override
    public void getpage(final String url) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setsearchingpage();
                    Document doc = Jsoup.connect(url).data("page", "" + page).data("query", "Java").userAgent("Mozilla").timeout(10000).get();
                    //分析得到数据
                    Elements elements = doc.select("div[itemtype=http://schema.org/Novel].am-thumbnail");
                    for (int i = 0; i < elements.size(); i++) {
                        //统计数目
                        ii++;
                        //
                        String name = "《" + elements.get(i).select("a[itemprop=name]").text() + "》作者：" + elements.get(i).select("a[itemprop=author]").text();
                        String time = "更新时间：" + elements.get(i).select("span[itemprop=dateModified]").text() + "\n分类：" + elements.get(i).select("a[itemprop=genre]").text() + "\n状态：" + elements.get(i).select("span[itemprop=updataStatus]").text();
                        String info = elements.get(i).select("div[itemprop=description]").text();
                        String durl = elements.get(i).select("a[itemprop=name]").attr("href");
                        Message message = showlist.obtainMessage();
                        message.obj = new mlist(name, time, info, durl);
                        message.sendToTarget();
                    }
                    ifnopage();

                    if (doc.select("a:contains(下一页)").attr("href").length() > 5)
                        page++;
                    else
                        page = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = failload.obtainMessage();
                    message.sendToTarget();
                }

            }
        }).start();
    }

    @Override
    public void downloadclick() throws Exception {
        getdownload(getClickdurl());
    }

    @Override
    public boolean getifnextpage() {
        return page > 1;
    }

    @Override
    public String getloadmore() {
        return url;
    }

    public void getdownload(final String url) throws Exception {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    //获得下载地址
                    Log.d("a", "run: ]" + url);
                    Document download = Jsoup.connect("http:" + url).data("query", "Java").userAgent("Mozilla").get();
                    download = Jsoup.connect(download.select("a:contains(全文下载)").attr("href")).get();
                    String durl = "http:" + download.select("a:contains(简体TXT (UTF8编码))").attr("href");
                    Log.d("a", "run: get");
                    showdownload(durl);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }


}
