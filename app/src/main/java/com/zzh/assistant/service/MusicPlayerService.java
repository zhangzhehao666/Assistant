package com.zzh.assistant.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zzh.assistant.receiver.PlayerManagerReceiver;

public class MusicPlayerService extends Service {
    public static final String PLAYER_MANAGER_ACTION = "com.zzh.assistant.service.MusicPlayerService";
    private PlayerManagerReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        register();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    private void register() {
        mReceiver = new PlayerManagerReceiver(MusicPlayerService.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAYER_MANAGER_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}
