package com.zzh.assistant.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.zzh.assistant.entities.Music;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.MusicUtil;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private MySQLiteHelper sqLiteHelper;
    private static SQLiteDatabase db;
    private static Context context;

    public DBManager(Context context) {
        this.context = context;
        sqLiteHelper = new MySQLiteHelper(context, "musicList.db", null, 1);
        db = sqLiteHelper.getReadableDatabase();
    }

    // 获取音乐表歌曲数量
    public static int getMusicCount() {
        int musicCount = 0;
        Cursor cursor = db.rawQuery("select * from musicList", null);
        if (cursor.moveToFirst()) {
            musicCount = cursor.getCount();
        }
        if (cursor != null) {
            cursor.close();
        }
        return musicCount;
    }

    //获取音乐
    public static List<Music> getPlayList() {
        List<Music> playList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from musicList", null);
        while (cursor.moveToNext()) {
            Music m = new Music();
            m.setId(cursor.getInt(cursor.getColumnIndex("id")));
            m.setSingerName(cursor.getString(cursor.getColumnIndex("singerName")));
            m.setSongName(cursor.getString(cursor.getColumnIndex("songName")));
            m.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            m.setDownUrl(cursor.getString(cursor.getColumnIndex("downUrl")));
            m.setAlbumpic_big(cursor.getString(cursor.getColumnIndex("albumpic_big")));
            m.setAlbumpic_small(cursor.getString(cursor.getColumnIndex("albumpic_small")));
            playList.add(m);
        }
        if (cursor != null) {
            cursor.close();
        }
        return playList;
    }

    public static int getMaxId() {
        int id = 0;
        ContentValues values;
        Cursor cursor = null;
        String sql = "select max(id) from musicList";
        cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        return id;
    }

    //添加歌曲到音乐表
    public static void insert(Music musicInfo) {
        ContentValues values;
        Cursor cursor = null;
        int id = 1;
        try {
            values = musicInfoToContentValues(musicInfo);
            String sql = "select max(id) from musicList";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                //设置新添加的ID为最大ID+1
                id = cursor.getInt(0) + 1;
            }
            values.put("id", id);
            db.insert("musicList", null, values);
            MusicUtil.setShared(context, Constant.KEY_ID, id);
        } catch (Exception e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //把MusicInfo对象转为ContentValues对象
    public static ContentValues musicInfoToContentValues(Music musicInfo) {
        ContentValues values = new ContentValues();
        try {
            values.put("singerName", musicInfo.getSingerName());
            values.put("songName", musicInfo.getSongName());
            values.put("url", musicInfo.getUrl());
            values.put("downUrl", musicInfo.getDownUrl());
            values.put("albumpic_big", musicInfo.getAlbumpic_big());
            values.put("albumpic_small", musicInfo.getAlbumpic_small());
            values.put("mid", musicInfo.getSongmid());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public void deleteID(int id) {
        db.delete("musicList", "id=?", new String[]{String.valueOf(id)});
    }

    //获取音乐表中的第一首音乐的ID
    public int getFirstId(int listNumber) {
        Cursor cursor = null;
        int id = -1;
        try {
            switch (listNumber) {
                case Constant.LIST_ALLMUSIC:
                    cursor = db.rawQuery("select min(id) from musicList", null);
                    break;
            }
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return id;
    }

    // 获取歌曲mid
    public String getMusicMid(int id) {
        if (id == -1) {
            return null;
        }
        String mid = null;
        Cursor cursor = null;
        try {
            cursor = db.query("musicList", null, "id = ?", new String[]{"" + id}, null, null, null);
            if (cursor.moveToFirst()) {
                mid = cursor.getString(cursor.getColumnIndex("mid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mid;
    }

    // 获取歌曲名字
    public String getMusicName(int id) {
        if (id == -1) {
            return null;
        }
        String mid = null;
        Cursor cursor = null;
        try {
            cursor = db.query("musicList", null, "id = ?", new String[]{"" + id}, null, null, null);
            if (cursor.moveToFirst()) {
                mid = cursor.getString(cursor.getColumnIndex("songName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mid;
    }

    // 获取歌曲
    public String getMusicInfo(int id, String type) {
        if (id == -1) {
            return null;
        }
        String mid = null;
        Cursor cursor = null;
        try {
            cursor = db.query("musicList", null, "id = ?", new String[]{"" + id}, null, null, null);
            if (cursor.moveToFirst()) {
                mid = cursor.getString(cursor.getColumnIndex(type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mid;
    }

    // 获取下一首歌曲(id)
    public int getNextMusic(ArrayList<Integer> musicList, int id, int playMode) {
        if (id == -1) {
            return -1;
        }
        //找到当前id在列表的第几个位置（i+1）
        int index = musicList.indexOf(id);
        if (index == -1) {
            return -1;
        }
        // 如果当前是最后一首
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                if ((index + 1) == musicList.size()) {
                    id = musicList.get(0);
                } else {
                    ++index;
                    id = musicList.get(index);
                }
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                break;
            case Constant.PLAYMODE_RANDOM:
                id = getRandomMusic(musicList, id);
                break;
        }
        return id;
    }

    // 获取上一首歌曲(id)
    public int getPreMusic(ArrayList<Integer> musicList, int id, int playMode) {
        if (id == -1) {
            return -1;
        }
        //找到当前id在列表的第几个位置（i+1）
        int index = musicList.indexOf(id);
        if (index == -1) {
            return -1;
        }
        // 如果当前是第一首则返回最后一首
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                if (index == 0) {
                    id = musicList.get(musicList.size() - 1);
                } else {
                    --index;
                    id = musicList.get(index);
                }
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                break;
            case Constant.PLAYMODE_RANDOM:
                id = getRandomMusic(musicList, id);
                break;
        }
        return id;
    }

    //获取随机歌曲
    public int getRandomMusic(ArrayList<Integer> list, int id) {
        int musicId;
        if (id == -1) {
            return -1;
        }
        if (list.isEmpty()) {
            return -1;
        }
        if (list.size() == 1) {
            return id;
        }
        do {
            int count = (int) (Math.random() * list.size());
            musicId = list.get(count);
        } while (musicId == id);

        return musicId;
    }

    // 获取歌曲详细信息
    public ArrayList<String> getMusicInfo(int id) {
        if (id == -1) {
            return null;
        }
        Cursor cursor = null;
        ArrayList<String> musicInfo = new ArrayList<String>();
        cursor = db.query("musicList", null, "id = ?", new String[]{"" + id}, null, null, null);
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                musicInfo.add(i, cursor.getString(i));
            }
        } else {
            musicInfo.add("0");
            musicInfo.add("听听音乐");
            musicInfo.add("好音质");
        }
        if (cursor != null) {
            cursor.close();
        }
        return musicInfo;
    }
}
