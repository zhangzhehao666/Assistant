package com.zzh.assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.fragments.PlayBarFragment;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.DownloadUtil;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.utils.UpdateUIThread;

import java.io.File;
import java.io.IOException;

public class PlayerManagerReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_UI_ADAPTER = "com.zzh.assistant.receiver.PlayerManagerReceiver";
    public static int status = Constant.STATUS_STOP;
    private Context context;
    private DBManager dbManager;
    private MediaPlayer mediaPlayer;
    private int threadNumber;
    private String filePath = Environment.getExternalStorageDirectory() + "/Assistant/music/";
    private String fileName;

    public PlayerManagerReceiver() {
    }

    public PlayerManagerReceiver(Context context) {
        super();
        this.context = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        dbManager = new DBManager(context);
        initMediaPlayer();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int cmd = intent.getIntExtra(Constant.COMMAND, Constant.COMMAND_INIT);
        switch (cmd) {
            case Constant.COMMAND_PLAY:
                status = Constant.STATUS_PLAY;
                String musicPath = intent.getStringExtra(Constant.KEY_PATH);
                fileName = intent.getStringExtra("fileName") + ".m4a";
                if (musicPath != null) {
                    playMusic(musicPath);
                } else {
                    mediaPlayer.start();
                    new UpdateUIThread(this, context, threadNumber).start();
                }
                break;
            case Constant.COMMAND_PAUSE:
                mediaPlayer.pause();
                status = Constant.STATUS_PAUSE;
                break;
            case Constant.COMMAND_STOP: //本程序停止状态都是删除当前播放音乐触发
                NumberRandom();
                status = Constant.STATUS_STOP;
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                initStopOperate();
                break;
            case Constant.COMMAND_PROGRESS://拖动进度
                int curProgress = intent.getIntExtra(Constant.KEY_CURRENT, 0);
                //异步的，可以设置完成监听来获取真正定位完成的时候
                mediaPlayer.seekTo(curProgress);
                break;
            case Constant.COMMAND_RELEASE:
                NumberRandom();
                status = Constant.STATUS_STOP;
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                break;
        }
        UpdateUI();
    }

    private void initStopOperate() {
        MusicUtil.setShared(context, Constant.KEY_ID, dbManager.getFirstId(Constant.LIST_ALLMUSIC));
    }

    private void initMediaPlayer() {

        NumberRandom(); // 改变线程号,使旧的播放线程停止

        int musicId = MusicUtil.getIntShared(context, Constant.KEY_ID);
        int current = MusicUtil.getIntShared(context, Constant.KEY_CURRENT);

        // 如果是没取到当前正在播放的音乐ID，则从数据库中获取第一首音乐的播放信息初始化
        if (musicId == -1) {
            return;
        }
        String path = dbManager.getMusicMid(musicId);
        if (path == null) {
            return;
        }
        if (current == 0) {
            status = Constant.STATUS_STOP; // 设置播放状态为停止
        } else {
            status = Constant.STATUS_PAUSE; // 设置播放状态为暂停
        }
        MusicUtil.setShared(context, Constant.KEY_ID, musicId);
        MusicUtil.setShared(context, Constant.KEY_PATH, path);

        UpdateUI();
    }

    //取一个（0，100）之间的不一样的随机数
    private void NumberRandom() {
        int count;
        do {
            count = (int) (Math.random() * 100);
        } while (count == threadNumber);
        threadNumber = count;
    }

    private void playMusic(final String mid) {
        NumberRandom();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                NumberRandom();                //切换线程
                onComplete();     //调用音乐切换模块，进行相应操作
                UpdateUI();                //更新界面
            }
        });

        //            mediaPlayer.setDataSource("http://ws.stream.qqmusic.qq.com/C100" + mid + ".m4a?fromtag=0&guid=126548448");
//            mediaPlayer.prepareAsync();
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mediaPlayer) {
//                    mediaPlayer.start();
//                }
//            });
        File file = new File(filePath + fileName);
        if (file.exists()) {
            try {
                mediaPlayer.setDataSource(file.getPath());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downFile("http://ws.stream.qqmusic.qq.com/C100" + mid + ".m4a?fromtag=0&guid=126548448");
                }
            }).start();
        }
    }

    private void downFile(String url) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        DownloadUtil.get().download(url, file.getPath(), fileName,
                new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        try {
                            mediaPlayer.setDataSource(filePath + fileName);
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    mediaPlayer.start();
                                    new UpdateUIThread(PlayerManagerReceiver.this, context, threadNumber).start();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDownloading(int progress) {
                        Log.d("qqq", String.valueOf(progress));
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        Log.d("qqq", e.toString());
                    }
                });
    }

    private void onComplete() {
        MusicUtil.playNextMusic(context);
    }

    private void UpdateUI() {
        Intent playBarintent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
        playBarintent.putExtra(Constant.STATUS, status);
        context.sendBroadcast(playBarintent);

        Intent intent = new Intent(ACTION_UPDATE_UI_ADAPTER);    //接收广播为所有歌曲列表的adapter
        context.sendBroadcast(intent);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getThreadNumber() {
        return threadNumber;
    }
}
