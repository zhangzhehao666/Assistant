package com.zzh.assistant.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.zzh.assistant.activitys.ThemeActivity;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.global.MyUser;
import com.zzh.assistant.service.MusicPlayerService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MusicUtil {

    //得到主题
    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.THEME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("theme_select", 0);
    }

    //得到是否夜间模式
    public static boolean getNightMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.THEME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("night", false);
    }

    //得到上一次选择的主题，用于取消夜间模式时恢复用
    public static int getPreTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.THEME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("pre_theme_select", 0);
    }

    //设置夜间模式
    public static void setNightMode(Context context, boolean mode) {
        if (mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.THEME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("night", mode).apply();
    }

    //设置主题
    public static void setTheme(Context context, int position) {
        int preSelect = getTheme(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.THEME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("theme_select", position).commit();
        if (preSelect != ThemeActivity.THEME_SIZE - 1) {
            sharedPreferences.edit().putInt("pre_theme_select", preSelect).commit();
        }
    }

    //设置--铃声的具体方法
    public static void setMyRingtone(Context context, String path) {
        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        Uri newUri = context.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
        Toast.makeText(context, "设置铃声成功！", Toast.LENGTH_SHORT).show();
    }

    // 设置sharedPreferences
    public static void setShared(Context context, String key, int value) {
        SharedPreferences pref = context.getSharedPreferences("music", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setShared(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences("music", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // 获取sharedPreferences
    public static int getIntShared(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences("music", context.MODE_PRIVATE);
        int value;
        if (key.equals(Constant.KEY_CURRENT)) {
            value = pref.getInt(key, 0);
        } else {
            value = pref.getInt(key, -1);
        }
        return value;
    }
    public static void delete(Context context){
        SharedPreferences pref = context.getSharedPreferences("music", context.MODE_PRIVATE);
        pref.edit().clear().apply();
    }
    public static void deleteOperate(Music music, final Context context) {
        Music m = new Music();
        m.setObjectId(music.getObjectId());
        m.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toasts.showShort(context, "取消成功！");
                } else {
                    Toasts.showShort(context, "取消失败！");
                    Log.d("deleteOperate", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    public static void playNextMusic(Context context) {
        //获取下一首ID
        DBManager dbManager = new DBManager(context);
        int playMode = MusicUtil.getIntShared(context, Constant.KEY_MODE);
        int musicId = MusicUtil.getIntShared(context, Constant.KEY_ID);
        List<Music> musicList = dbManager.getPlayList();
        ArrayList<Integer> musicIdList = new ArrayList<>();
        for (Music info : musicList) {
            musicIdList.add(info.getId());
        }
        musicId = dbManager.getNextMusic(musicIdList, musicId, playMode);
        MusicUtil.setShared(context, Constant.KEY_ID, musicId);
        if (musicId == -1) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            context.sendBroadcast(intent);
            Toast.makeText(context, "歌曲不存在", Toast.LENGTH_LONG).show();
            return;
        }

        //获取播放歌曲路径
        String path = dbManager.getMusicMid(musicId);
        //发送播放请求
        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
        intent.putExtra(Constant.KEY_PATH, path);
        intent.putExtra("fileName",dbManager.getMusicName(musicId));
        context.sendBroadcast(intent);
    }

    public static void playPreMusic(Context context) {
        //获取下一首ID
        DBManager dbManager = new DBManager(context);
        int playMode = MusicUtil.getIntShared(context, Constant.KEY_MODE);
        int musicId = MusicUtil.getIntShared(context, Constant.KEY_ID);
        List<Music> musicList = dbManager.getPlayList();
        ArrayList<Integer> musicIdList = new ArrayList<>();
        for (Music info : musicList) {
            musicIdList.add(info.getId());
        }
        musicId = dbManager.getPreMusic(musicIdList, musicId, playMode);
        MusicUtil.setShared(context, Constant.KEY_ID, musicId);
        if (musicId == -1) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            context.sendBroadcast(intent);
            Toast.makeText(context, "歌曲不存在", Toast.LENGTH_LONG).show();
            return;
        }
        //获取播放歌曲路径
        String path = dbManager.getMusicMid(musicId);
        //发送播放请求
        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
        intent.putExtra(Constant.KEY_PATH, path);
        intent.putExtra("fileName",dbManager.getMusicName(musicId));
        context.sendBroadcast(intent);
    }

}
