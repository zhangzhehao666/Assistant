package com.zzh.assistant.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zzh.assistant.R;
import com.zzh.assistant.activitys.PlayActivity;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.receiver.PlayerManagerReceiver;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.views.PlayingPopWindow;

import java.util.ArrayList;

import static com.zzh.assistant.receiver.PlayerManagerReceiver.status;

public class PlayBarFragment extends Fragment implements View.OnClickListener {
    public static final String ACTION_UPDATE_UI_PlayBar = "com.zzh.assistant.fragments.PlayBarFragment";
    private View rootView;
    private Context context;
    private TextView singName, songName;
    private ImageView imgPlay, imgNext, imgMenu;
    private LinearLayout linearLayout;
    private HomeReceiver mReceiver;
    private DBManager dbManager;

    public static synchronized PlayBarFragment newInstance() {
        return new PlayBarFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_playbar, container, false);
            initView();
            initData();
            initEvent();
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void initView() {
        singName = (TextView) rootView.findViewById(R.id.fragment_playbar_sing_name);
        songName = (TextView) rootView.findViewById(R.id.fragment_playbar_song_name);
        imgPlay = (ImageView) rootView.findViewById(R.id.fragment_playbar_play_iv);
        imgNext = (ImageView) rootView.findViewById(R.id.fragment_playbar_next_iv);
        imgMenu = (ImageView) rootView.findViewById(R.id.fragment_playbar_play_menu_iv);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.fragment_playbar_ll);
    }

    private void initData() {
        dbManager = new DBManager(getActivity());
        register();
        setFragmentBb();
        setMusicName();
        initPlayIv();
    }

    private void initEvent() {
        imgPlay.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        imgMenu.setOnClickListener(this);
        linearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_playbar_play_iv:
                int musicId = MusicUtil.getIntShared(getActivity(), Constant.KEY_ID);
                if (musicId == -1 || musicId == 0) {
                    Intent intent = new Intent(Constant.MP_FILTER);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    getActivity().sendBroadcast(intent);
                    Toast.makeText(getActivity(), "歌曲不存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                //如果当前媒体在播放音乐状态，则图片显示暂停图片，按下播放键，则发送暂停媒体命令，图片显示播放图片。以此类推。
                if (status == Constant.STATUS_PAUSE) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra("fileName", dbManager.getMusicName(musicId));
                    getActivity().sendBroadcast(intent);
                } else if (status == Constant.STATUS_PLAY) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                    getActivity().sendBroadcast(intent);
                } else {
                    //为停止状态时发送播放命令，并发送将要播放歌曲的路径
                    String path = dbManager.getMusicMid(musicId);
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    intent.putExtra("fileName", dbManager.getMusicName(musicId));
                    getActivity().sendBroadcast(intent);
                }

                break;
            case R.id.fragment_playbar_next_iv:
                MusicUtil.playNextMusic(getActivity());
                break;
            case R.id.fragment_playbar_play_menu_iv:
                showPopFormBottom();
                break;
            case R.id.fragment_playbar_ll:
                Intent intent = new Intent(getActivity(), PlayActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(getActivity());
//      设置Popupwindow显示位置（从底部弹出）
        playingPopWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getActivity().getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        playingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha = 1f;
                getActivity().getWindow().setAttributes(params);
            }
        });

    }

    public void setFragmentBb() {
        //获取播放控制栏颜色
        int defaultColor = 0xFFFFFF;
        int[] attrsArray = {R.attr.play_bar_color};
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        int color = typedArray.getColor(0, defaultColor);
        typedArray.recycle();
        linearLayout.setBackgroundColor(color);
    }

    private void setMusicName() {
        int musicId = MusicUtil.getIntShared(getActivity(), Constant.KEY_ID);
        if (musicId == -1) {
            songName.setText("QQ音乐");
            singName.setText("好音质");
        } else {
            songName.setText(dbManager.getMusicInfo(musicId).get(1));
            singName.setText(dbManager.getMusicInfo(musicId).get(2));
        }
    }

    private void register() {
        mReceiver = new HomeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_UI_PlayBar);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    private void initPlayIv() {
        int status = PlayerManagerReceiver.status;
        switch (status) {
            case Constant.STATUS_STOP:
                imgPlay.setSelected(false);
                break;
            case Constant.STATUS_PLAY:
                imgPlay.setSelected(true);
                break;
            case Constant.STATUS_PAUSE:
                imgPlay.setSelected(false);
                break;
            case Constant.STATUS_RUN:
                imgPlay.setSelected(true);
                break;
        }
    }

    class HomeReceiver extends BroadcastReceiver {
        int status;
        int duration;
        int current;

        @Override
        public void onReceive(Context context, Intent intent) {
            setMusicName();
            status = intent.getIntExtra(Constant.STATUS, 0);
            current = intent.getIntExtra(Constant.KEY_CURRENT, 0);
            duration = intent.getIntExtra(Constant.KEY_DURATION, 100);
            switch (status) {
                case Constant.STATUS_STOP:
                    imgPlay.setSelected(false);
                    break;
                case Constant.STATUS_PLAY:
                    imgPlay.setSelected(true);
                    break;
                case Constant.STATUS_PAUSE:
                    imgPlay.setSelected(false);
                    break;
                case Constant.STATUS_RUN:
                    imgPlay.setSelected(true);
                    break;
                default:
                    break;
            }
        }
    }
}
