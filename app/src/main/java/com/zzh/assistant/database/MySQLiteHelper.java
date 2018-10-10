package com.zzh.assistant.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.zzh.assistant.global.MyUser;

import java.io.File;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public MySQLiteHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, getMyDatabaseName(name), factory, version);
    }

    private static String getMyDatabaseName(String name) {
        String databasename = name;
        boolean isSdcardEnable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isSdcardEnable = true;
        }
        String dbPath = null;
        if (isSdcardEnable) {
            dbPath = Environment.getExternalStorageDirectory().getPath() + "/Assistant/database/";
        }
        File dbp = new File(dbPath);
        if (!dbp.exists()) {
            dbp.mkdirs();
        }
        databasename = dbPath + databasename;
        return databasename;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table musicList(id integer PRIMARY KEY AUTOINCREMENT,singerName varchar(100)" +
                ",songName varchar(100),url varchar(100),downUrl varchar(100),albumpic_big varchar(100)," +
                "albumpic_small varchar(100),mid varchar(100))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
