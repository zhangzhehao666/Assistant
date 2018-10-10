package com.zzh.assistant.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.zzh.assistant.R;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.fragments.PlayBarFragment;
import com.zzh.assistant.http.CustomRequest;
import com.zzh.assistant.impl.DefaultLrcBuilder;
import com.zzh.assistant.impl.ILrcBuilder;
import com.zzh.assistant.impl.LrcRow;
import com.zzh.assistant.impl.LrcView;
import com.zzh.assistant.receiver.PlayerManagerReceiver;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.views.PlayingPopWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.zzh.assistant.http.ServerUrl.QQMusic;

public class PlayActivity extends BaseActivity implements View.OnClickListener {
    private ImageView back, mode, play, next, prev, menu, bgImg;
    private TextView singName, songName;
    private PlayReceiver mReceiver;
    private int mProgress;
    private int duration;
    private int current;
    private DBManager dbManager;
    private SeekBar seekBar;
    private TextView curTimeTv;
    private TextView totalTimeTv;
    private LrcView lrcView;
    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 1000;
    //更新歌词的定时器
    private Timer mTimer;
    //更新歌词的定时任务
    private TimerTask mTask;
    private RequestQueue queue;
    private String filePath = Environment.getExternalStorageDirectory() + "/Assistant/music/lrc/";
    private String fileName;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        back = (ImageView) findViewById(R.id.play_back);
        mode = (ImageView) findViewById(R.id.play_mode);
        play = (ImageView) findViewById(R.id.play_play);
        next = (ImageView) findViewById(R.id.play_next);
        prev = (ImageView) findViewById(R.id.play_prev);
        menu = (ImageView) findViewById(R.id.play_menu);
        singName = (TextView) findViewById(R.id.play_artist);
        songName = (TextView) findViewById(R.id.play_title);
        seekBar = (SeekBar) findViewById(R.id.play_seekbar);
        curTimeTv = (TextView) findViewById(R.id.play_current_time);
        totalTimeTv = (TextView) findViewById(R.id.play_total_time);
        lrcView = (LrcView) findViewById(R.id.play_lrcView);
        bgImg = (ImageView) findViewById(R.id.play_bg_img);
        relativeLayout = (RelativeLayout) findViewById(R.id.play_re);
    }

    private void initData() {
        queue = Volley.newRequestQueue(PlayActivity.this);
        dbManager = new DBManager(PlayActivity.this);
        setStyle();
        register();
        setSeekBarBg();
        initPlayMode();
        initTitle();
        initplay();
        getLrc();
    }

    private void initEvent() {
        back.setOnClickListener(this);
        mode.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        prev.setOnClickListener(this);
        menu.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int musicId = MusicUtil.getIntShared(PlayActivity.this, Constant.KEY_ID);
                if (musicId == -1) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra("cmd", Constant.COMMAND_STOP);
                    sendBroadcast(intent);
                    Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_LONG).show();
                    return;
                }
                //发送播放请求
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra("cmd", Constant.COMMAND_PROGRESS);
                intent.putExtra("current", mProgress);
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                mProgress = progress;
                initTime();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_back:
                onBackPressed();
                break;
            case R.id.play_mode:
                switchPlayMode();
                break;
            case R.id.play_play:
                play();
                break;
            case R.id.play_next:
                MusicUtil.playNextMusic(this);
                getLrc();
                break;
            case R.id.play_prev:
                MusicUtil.playPreMusic(this);
                getLrc();
                break;
            case R.id.play_menu:
                showPopFormBottom();
                break;
        }
    }

    private void getLrc() {
        int musicId = MusicUtil.getIntShared(PlayActivity.this, Constant.KEY_ID);
//        Glide.with(PlayActivity.this).load(dbManager.getMusicInfo(musicId, "albumpic_big")).into(bgImg);
        fileName = songName.getText().toString() + ".txt";
        File file = new File(filePath + fileName);
        if (!file.exists()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("showapi_appid", "59751");
            params.put("showapi_sign", "4c19b0f7d772496aba7a347db67a26db");
            params.put("musicid", dbManager.getMusicMid(musicId));
            CustomRequest request = new CustomRequest(Request.Method.POST, QQMusic + "2",
                    params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s.toString());
                        if (jsonObject.getInt("showapi_res_code") == 0) {
                            jsonObject = jsonObject.getJSONObject("showapi_res_body");
                            CommonUtil.writeTxtToFile(jsonObject.getString("lyric"), filePath,
                                    fileName);
                            String lrc = getFromPath(filePath + fileName);
                            //解析歌词构造器
                            ILrcBuilder builder = new DefaultLrcBuilder();
                            //解析歌词返回LrcRow集合
                            List<LrcRow> rows = builder.getLrcRows(lrc);
                            //将得到的歌词集合传给mLrcView用来展示
                            lrcView.setLrc(rows);
                            if (mTimer == null) {
                                mTimer = new Timer();
                                mTask = new LrcTask();
                                mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("NewsFragment", error.toString());
                }
            });
            queue.add(request);
        } else {
            String lrc = getFromPath(filePath + fileName);
            //解析歌词构造器
            ILrcBuilder builder = new DefaultLrcBuilder();
            //解析歌词返回LrcRow集合
            List<LrcRow> rows = builder.getLrcRows(lrc);
            //将得到的歌词集合传给mLrcView用来展示
            lrcView.setLrc(rows);
            if (mTimer == null) {
                mTimer = new Timer();
                mTask = new LrcTask();
                mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
            }
        }
    }

    public String getFromPath(String fileName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            InputStreamReader inputReader = new InputStreamReader(fis, "utf-8");
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                result += line + "\r\n";
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 停止展示歌曲
     */
    public void stopLrcPlay() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 展示歌曲的定时任务
     */
    class LrcTask extends TimerTask {
        @Override
        public void run() {
            //获取歌曲播放的位置
            final long timePassed = current;
            runOnUiThread(new Runnable() {
                public void run() {
                    //滚动歌词
                    lrcView.seekLrcToTime(timePassed);
                }
            });
        }
    }

    private void setSeekBarBg() {
        try {
            int progressColor = CommonUtil.getAttrColorValue(R.attr.colorPrimary, R.color.colorAccent, this);
            LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
            ScaleDrawable scaleDrawable = (ScaleDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
            GradientDrawable drawable = (GradientDrawable) scaleDrawable.getDrawable();
            drawable.setColor(progressColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTitle() {
        int musicId = MusicUtil.getIntShared(PlayActivity.this, Constant.KEY_ID);
        if (musicId == -1) {
            songName.setText("QQ音乐");
            singName.setText("好音质");

        } else {
            singName.setText(dbManager.getMusicInfo(musicId).get(1));
            songName.setText(dbManager.getMusicInfo(musicId).get(2));
        }
    }

    private void initPlayMode() {
        int playMode = MusicUtil.getIntShared(PlayActivity.this, Constant.KEY_MODE);
        if (playMode == -1) {
            playMode = 0;
        }
        mode.setImageLevel(playMode);
    }

    private void initplay() {
        int status = PlayerManagerReceiver.status;
        switch (status) {
            case Constant.STATUS_STOP:
                play.setSelected(false);
                break;
            case Constant.STATUS_PLAY:
                play.setSelected(true);
                break;
            case Constant.STATUS_PAUSE:
                play.setSelected(false);
                break;
            case Constant.STATUS_RUN:
                play.setSelected(true);
                break;
        }
    }

    private void initTime() {
        curTimeTv.setText(formatTime(current));
        totalTimeTv.setText(formatTime(duration));
    }

    private String formatTime(long time) {
        return formatTime("mm:ss", time);
    }

    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }

    private void switchPlayMode() {
        int playMode = MusicUtil.getIntShared(PlayActivity.this, Constant.KEY_MODE);
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                MusicUtil.setShared(PlayActivity.this, Constant.KEY_MODE, Constant.PLAYMODE_RANDOM);
                break;
            case Constant.PLAYMODE_RANDOM:
                MusicUtil.setShared(PlayActivity.this, Constant.KEY_MODE, Constant.PLAYMODE_SINGLE_REPEAT);
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                MusicUtil.setShared(PlayActivity.this, Constant.KEY_MODE, Constant.PLAYMODE_SEQUENCE);
                break;
        }
        initPlayMode();
    }

    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(PlayActivity.this);
        playingPopWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.7f;
        getWindow().setAttributes(params);

        playingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    private void register() {
        mReceiver = new PlayReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private void play() {
        int musicId;
        musicId = MusicUtil.getIntShared(PlayActivity.this, Constant.KEY_ID);
        if (musicId == -1 || musicId == 0) {
            musicId = dbManager.getFirstId(Constant.LIST_ALLMUSIC);
            Intent intent = new Intent(Constant.MP_FILTER);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            sendBroadcast(intent);
            Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        //如果当前媒体在播放音乐状态，则图片显示暂停图片，按下播放键，则发送暂停媒体命令，图片显示播放图片。以此类推。
        if (PlayerManagerReceiver.status == Constant.STATUS_PAUSE) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            intent.putExtra("fileName", dbManager.getMusicName(musicId));
            sendBroadcast(intent);
        } else if (PlayerManagerReceiver.status == Constant.STATUS_PLAY) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
            sendBroadcast(intent);
        } else {
            //为停止状态时发送播放命令，并发送将要播放歌曲的路径
            String path = dbManager.getMusicMid(musicId);
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            intent.putExtra(Constant.KEY_PATH, path);
            intent.putExtra("fileName", dbManager.getMusicName(musicId));
            sendBroadcast(intent);
        }
    }

    class PlayReceiver extends BroadcastReceiver {

        int status;

        @Override
        public void onReceive(Context context, Intent intent) {
            initTitle();
            status = intent.getIntExtra(Constant.STATUS, 0);
            current = intent.getIntExtra(Constant.KEY_CURRENT, 0);
            duration = intent.getIntExtra(Constant.KEY_DURATION, 100);
            switch (status) {
                case Constant.STATUS_STOP:
                    play.setSelected(false);
                    List<LrcRow> lrcRows = null;
                    lrcView.setLrc(lrcRows);
                    curTimeTv.setText("00:00");
                    totalTimeTv.setText("00:00");
                    seekBar.setProgress(0);
                    stopLrcPlay();
                    break;
                case Constant.STATUS_PLAY:
                    getLrc();
                    play.setSelected(true);
                    break;
                case Constant.STATUS_PAUSE:
                    play.setSelected(false);
                    stopLrcPlay();
                    break;
                case Constant.STATUS_RUN:
                    play.setSelected(true);
                    seekBar.setMax(duration);
                    seekBar.setProgress(current);
                    break;
                default:
                    break;
            }

        }
    }

    private void setStyle() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
