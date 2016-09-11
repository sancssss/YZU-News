package com.liuliugeek.sanc.news;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SplashActivity extends Activity{
    private final static int UPDATE_INDEX_DATA = 0;
    private static final int SHOW_ERROR_NETWORK = 1;
    private MyHttp myHttp;
    private int indexDataId;
    private NewsDbManager manager;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_INDEX_DATA:

                    break;
                case SHOW_ERROR_NETWORK:
                    Toast.makeText(SplashActivity.this, "网络错误", Toast.LENGTH_LONG).show();
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        manager = new NewsDbManager(SplashActivity.this);
        //首页数据TYPEID= 3
        indexDataId = 3;
        handler.postDelayed(new SplashHandler(), 2000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(null != MyHttp.getActiveNetwork(SplashActivity.this)){
                    myHttp = new MyHttp("http://news.yzu.edu.cn/list.asp?TypeID="+3+"&Page=1","GBK");
                    myHttp.startCon();
                    String tempHtmlText = myHttp.getResult();
                    Message message = new Message();
                    message.what = UPDATE_INDEX_DATA;
                    message.obj = tempHtmlText;
                    message.arg1 = indexDataId;
                    handler.sendMessage(message);
                    ParseListDom parseListDom = new ParseListDom(tempHtmlText);
                    //parseListDom.getUrlList();
                   ArrayList<Data> datas = new ArrayList<>();
                    //Log.v("count", String.valueOf(parseListDom.getTitleList().size()));
                    for(int i = 0; i<21; i++){
                        //Log.v("getUrl",parseListDom.getUrlList().get(i));
                        //此处content放的是页面地址
                        //Data data = new Data("("+parseListDom.getTimeList().get(i).substring(5) + ")" + parseListDom.getTitleList().get(i),parseListDom.getUrlList().get(i));
                        Data data = new Data();
                        data.setNewTitle(parseListDom.getTitleList().get(i));
                        data.setNewContent(parseListDom.getUrlList().get(i));
                        data.setNewUrl(parseListDom.getUrlList().get(i));
                        data.setNewTypeid(indexDataId);
                        data.setNewArcid(Integer.valueOf(data.getNewContent().replaceAll(".*[^\\d](?=(\\d+))", "")));
                        data.setNewDate(parseListDom.getTimeList().get(i));
                        // Log.v("id", String.valueOf(data.getNewArcid()));
                        datas.add(data);
                    }
                    manager.deleteContent(indexDataId);
                    //将数据存进本地数据库
                    manager.add(datas);
                    manager.setBoardRefreshDate(indexDataId,getDate());
                }else{
                    Message message = new Message();
                    message.what = SHOW_ERROR_NETWORK;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }


    class SplashHandler implements Runnable{

        @Override
        public void run() {
            startActivity(new Intent(getApplication(), MainActivity.class));
            SplashActivity.this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private String getDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }
}
