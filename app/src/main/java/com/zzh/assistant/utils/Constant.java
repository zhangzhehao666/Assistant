package com.zzh.assistant.utils;


public class Constant {
    //主题
    public static final String THEME = "theme";
    //播放状态
    public static final String STATUS = "status";
    public static final int STATUS_STOP = 0; //停止状态
    public static final int STATUS_PLAY = 1; //播放状态
    public static final int STATUS_PAUSE = 2; //暂停状态
    public static final int STATUS_RUN = 3;

    public static final String COMMAND = "cmd";

    public static final int COMMAND_INIT = 1; //初始化命令
    public static final int COMMAND_PLAY = 2; //播放命令
    public static final int COMMAND_PAUSE = 3; //暂停命令
    public static final int COMMAND_STOP = 4; //停止命令
    public static final int COMMAND_PROGRESS = 5; //改变进度命令
    public static final int COMMAND_RELEASE = 6; //退出程序时释放

    public static final String MP_FILTER = "com.zzh.assistant.start_mediaplayer";

    //播放模式
    public static final int PLAYMODE_SEQUENCE = -1;
    public static final int PLAYMODE_SINGLE_REPEAT = 1;
    public static final int PLAYMODE_RANDOM = 2;

    //歌曲列表常量
    public static final int LIST_ALLMUSIC = -1;

    //SharedPreferences key 常量
    public static final String KEY_ID = "id";
    public static final String KEY_PATH = "path";
    public static final String KEY_MODE = "mode";
    public static final String KEY_LIST = "list";
    public static final String KEY_LIST_ID = "list_id";
    public static final String KEY_CURRENT = "current";
    public static final String KEY_DURATION = "duration";
}
