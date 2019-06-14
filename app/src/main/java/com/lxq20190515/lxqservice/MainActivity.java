package com.lxq20190515.lxqservice;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private ImageView playerBtn;
    static TextView textPath;
    private Intent intent;
    protected TextView ed;
    protected TextView songLong;
    protected TextView seekToTv;
    static MainActivity mainActivity;
    protected SeekBar seekBar;
    static boolean isPlayer=false;
    static boolean isStarted=false;
    private ServiceConnection connection;
    private ServiceDemo.LXQBinder lxqBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerBtn=findViewById(R.id.player);
        ed=findViewById(R.id.ed);
        textPath=findViewById(R.id.path);
        seekBar=findViewById(R.id.seek);
        seekBar.setOnSeekBarChangeListener(listener);
        seekToTv=findViewById(R.id.seek_to_tv);
        seekToTv.setVisibility(View.INVISIBLE);
        songLong=findViewById(R.id.song_long);
        mainActivity=MainActivity.this;
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isStarted)return false;

                seekToTv.setVisibility(View.VISIBLE);
                seekToTv.setX(event.getX()-30);

                float progress=seekBar.getProgress()/(float)100;
                int dura=lxqBinder.getDura()/1000;
                float position=progress*100*dura*1000;
                Log.e("posi",position+"");
//                int seekMinute=position/1000/60;
//                Log.e("seeMi",seekMinute+"");
//                int seekSeconds=position/1000-seekMinute*60;
//                Log.e("seeS",seekSeconds+"");
//                seekToTv.setText(seekMinute+":"+seekSeconds);
                return false;
            }
        });
        intent=new Intent(MainActivity.this,ServiceDemo.class);
        intent.putExtra("path","/sdcard/download/I Really Like You - Carly Rae Jepsen.mp3");
        //单击播放或暂停
        playerBtn.setOnClickListener(new View.OnClickListener() {//单击播放
            @Override
            public void onClick(View v) {
                if (!isStarted){
                    //startService(intent);
                    //bindService(intent,connection,BIND_AUTO_CREATE);
                    isStarted=true;
                    isPlayer=true;
                    playerBtn.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
                    lxqBinder.prepare();
                    lxqBinder.start();
                    return;
                }
                if (isPlayer){
                    isPlayer=false;
                    //ServiceDemo.mediaPlayer.start();
                    lxqBinder.start();
                    playerBtn.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
                }else{
                    isPlayer=true;
                    //ServiceDemo.mediaPlayer.pause();
                    lxqBinder.pause();
                    playerBtn.setImageResource(R.mipmap.ic_play_circle_filled_white_black_48dp);
                }

            }
        });
        //长按停止
        playerBtn.setOnLongClickListener(new View.OnLongClickListener() {//长按停止
            @Override
            public boolean onLongClick(View v) {
                /*ServiceDemo.mediaPlayer.stop();
                ServiceDemo.mediaPlayer.release();
                ServiceDemo.mediaPlayer=null;
                stopService(intent);*/
                lxqBinder.stop();
                isStarted=false;
                playerBtn.setImageResource(R.mipmap.ic_play_circle_filled_white_black_48dp);
                return true;
            }
        });
        textPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPathDialog();
            }
        });
        connection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                lxqBinder=(ServiceDemo.LXQBinder)service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent,connection,BIND_AUTO_CREATE);//开始服务
    }
    public void setPathDialog(){
        final EditText textView=new EditText(MainActivity.this);
        textView.setText("/sdcard/download/I Really Like You - Carly Rae Jepsen.mp3");
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("音乐的播放路径")
                .setView(textView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textPath.setText(textView.getText().toString());
                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }

    SeekBar.OnSeekBarChangeListener listener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekToTv.setVisibility(View.INVISIBLE);
            if (!isStarted)return;
            if(ServiceDemo.mediaPlayer!=null){
                float progress=(float) seekBar.getProgress()/100;
                int duration=ServiceDemo.mediaPlayer.getDuration()/1000;
                float seekTo=duration*progress*1000;
                ServiceDemo.mediaPlayer.seekTo((int) seekTo);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
