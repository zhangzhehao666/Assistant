package com.zzh.assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zzh.assistant.fragments.PlayBarFragment;
import com.zzh.assistant.receiver.PlayerManagerReceiver;

//此线程只是用于循环发送广播，通知更改歌曲播放进度。
public class UpdateUIThread extends Thread {

    private int threadNumber;
    private Context context;
    private PlayerManagerReceiver playerManagerReceiver;
    private int duration;
    private int curPosition;

    public UpdateUIThread(PlayerManagerReceiver playerManagerReceiver, Context context, int threadNumber) {
        this.playerManagerReceiver = playerManagerReceiver;
        this.context = context;
        this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
        try {
            while (playerManagerReceiver.getThreadNumber() == this.threadNumber) {
                if (playerManagerReceiver.status == Constant.STATUS_STOP) {
                    break;
                }
                if (playerManagerReceiver.status == Constant.STATUS_PLAY ||
                        playerManagerReceiver.status == Constant.STATUS_PAUSE) {
                    if (!playerManagerReceiver.getMediaPlayer().isPlaying()) {
                        break;
                    }
                    duration = playerManagerReceiver.getMediaPlayer().getDuration();
                    curPosition = playerManagerReceiver.getMediaPlayer().getCurrentPosition();
                    Intent intent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
                    intent.putExtra(Constant.STATUS, Constant.STATUS_RUN);
                    intent.putExtra(Constant.KEY_DURATION, duration);
                    intent.putExtra(Constant.KEY_CURRENT, curPosition);
                    context.sendBroadcast(intent);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

