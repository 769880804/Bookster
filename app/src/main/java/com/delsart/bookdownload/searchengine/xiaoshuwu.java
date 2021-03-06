package com.delsart.bookdownload.searchengine;


import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.delsart.bookdownload.listandadapter.mlist;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by Delsart on 2017/7/22.
 */

public class xiaoshuwu extends baseFragment {
    int page=1;

    public xiaoshuwu() {
        super();


    }

    public void get(String url) throws Exception {
        clean();
        getpage(url);
    }

    @Override
    public void clean() {
        super.clean();
        page=1;
    }

    @Override
    public void getpage(final String url) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setsearchingpage();

                    Document doc = Jsoup.connect(url).timeout(10000).data("query", "Java").userAgent("Mozilla").get();
                    //获得下一页数据
                    loadmore = "";
                    page++;
                    loadmore = doc.select("a[title*=页]:containsOwn("+page+")").attr("href");
                    //分析得到数据
                    Elements elements = doc.select("li:has(div.content)");
                    for (int i = 0; i < elements.size(); i++) {
                        //统计数目
                        ii++;
                        //
                        String t= elements.get(i).select("div.content").select("a").attr("title");
                        String t2= elements.get(i).select("div.info").text();
                        String name;
                        String time;
                        if (t.contains("（")) {
                             name= t.substring(0, t.lastIndexOf("（"));
                          time = "收录时间：" + t2.substring(0, t2.indexOf(" ·")) + "\n格式：" + t.substring(t.lastIndexOf("）") + 1, t.length()) + "\n标签：" + elements.get(i).select("div.cat").text();
                        }
                        else
                        {
                             name=t;
                             time = "收录时间：" + t2.substring(0, t2.indexOf(" ·")) +"\n标签：" + elements.get(i).select("div.cat").text();
                        }
                    getnext(name,time,elements.get(i).select("div.img").select("a").attr("href"));
                    }
                    ifnopage();
                } catch (Exception e) {
                    e.printStackTrace();
                    Message message = failload.obtainMessage();
                    message.sendToTarget();
                }

            }
        }).start();
    }

    public void getnext(final String name, final String time,final String url) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc2 = Jsoup.connect(url).timeout(10000).data("query", "Java").userAgent("Mozilla").get();
                    Elements element3 = doc2.select("div#content");
                    String info =element3.text().substring(0,element3.text().indexOf("下载地址"));
                    Message message = showlist.obtainMessage();
                    String durl=element3.select("a:containsOwn(点击下载)").attr("href");
                     message.obj = new mlist(name, time, info, durl);
                    message.sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
    Handler passwordh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(getContext(),"已复制："+msg.obj.toString(),Toast.LENGTH_SHORT).show();
            ClipboardManager cmb = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setText(msg.obj.toString());
        }
    } ;


    @Override
    public void downloadclick() throws Exception {
        getdownload(getClickdurl());
    }


    public void getdownload(final String url) throws Exception {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    //获得下载地址
                    Document doc3 = Jsoup.connect(url).get();
                    String durl =doc3.select("a:containsOwn(百度网盘)").attr("href");

                    Message message = passwordh.obtainMessage();

                    message.obj =  doc3.toString().substring(doc3.toString().indexOf("百度网盘密码："),doc3.toString().indexOf("&nbsp",doc3.toString().indexOf("百度网盘密码：")));
                    message.sendToTarget();
                    showdownload(durl);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

}
