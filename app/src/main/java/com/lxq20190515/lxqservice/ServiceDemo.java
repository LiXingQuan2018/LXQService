package com.lxq20190515.lxqservice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class ServiceDemo extends Service {

    private MainActivity mainActivity;
    protected static MediaPlayer mediaPlayer;
    private String path;
    private int minuted=0;//已播放多少分钟
    private int seconded=0;//已播放多少秒

    @Override
    public IBinder onBind(Intent intent) {
        path=intent.getStringExtra("path");
        Log.e("path",path);
        //LXQBinder lxqBinder=new LXQBinder();
        //mediaPlayer=new MediaPlayer();
        return new LXQBinder();
    }

    class LXQBinder extends Binder{
        public LXQBinder(){
            Log.e("tag","构造方法执行");
        }
        public void prepare(){
            mediaPlayer=new MediaPlayer();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mainActivity.songLong.setText(getDuration());
            update();
        }
        public void start(){
            mediaPlayer.start();
        }
        public void stop(){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        public void pause(){
            mediaPlayer.pause();
        }
        public int getDura(){
            return mediaPlayer.getDuration();
        }
        public int getCurrent(){
            return mediaPlayer.getCurrentPosition();
        }
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "服务已启动", Toast.LENGTH_SHORT).show();
        mainActivity=MainActivity.mainActivity;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        path=intent.getStringExtra("path");
        //mediaPlayer=new MediaPlayer();
        try {
            Toast.makeText(this, "正在播放："+path, Toast.LENGTH_SHORT).show();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("info",e.getMessage());
            onDestroy();
        }
        mainActivity.songLong.setText(getDuration());
        mediaPlayer.start();
        update();
        return super.onStartCommand(intent, flags, startId);
    }

    //结束服务
    @Override
    public void onDestroy() {
        //super.onDestroy();
        stopSelf();
        Toast.makeText(this, "服务已关闭", Toast.LENGTH_SHORT).show();
    }
    //获取歌曲的时长，格式0:00
    private String getDuration(){
        int time=mediaPlayer.getDuration()/1000;
        int minute=time/60;
        int seconds=time-minute*60;
        return minute+":"+seconds;
    }
    //数据更新
    private void update(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (mediaPlayer==null)return;
                        if (mediaPlayer.getCurrentPosition()>=mediaPlayer.getDuration()){//判断是否播放完毕
                            onDestroy();
                            return;
                        }
                        if (mediaPlayer.isPlaying()){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            getCurrent();
                            handler.sendEmptyMessage(1);
                        }
                }
            }
        }).start();
    }
    //更新activity
    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            if (msg.what==1){
                if (mediaPlayer==null)return;
                mainActivity.ed.setText(minuted+":"+(seconded>9?seconded:"0"+seconded));
                float progress=((float) mediaPlayer.getCurrentPosition()/1000)
                        /((float) mediaPlayer.getDuration()/1000);
                progress=progress*100;//转换为百分数
                mainActivity.seekBar.setProgress((int)progress);//设置拖动条的进度值
            }
        }
    };
    //获取当前播放的时间进度
    private void getCurrent(){
        if (mediaPlayer==null)return;
        minuted=mediaPlayer.getCurrentPosition()/1000/60;
        seconded=mediaPlayer.getCurrentPosition()/1000-minuted*60;
    }
}
