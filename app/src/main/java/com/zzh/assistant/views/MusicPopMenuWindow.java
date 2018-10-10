package com.zzh.assistant.views;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zzh.assistant.R;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.global.MyUser;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.utils.Toasts;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MusicPopMenuWindow extends PopupWindow {
    private View view;
    private Activity activity;
    private TextView nameTv;
    private LinearLayout ringLl;
    private LinearLayout addLl;
    private LinearLayout loveLl;
    private LinearLayout deleteLl;
    private LinearLayout cancelLl;
    private Music music;
    private boolean flag;

    public MusicPopMenuWindow(Activity activity, Music music, boolean flag) {
        super(activity);
        this.activity = activity;
        this.music = music;
        this.flag = flag;
        initView();
    }

    private void initView() {
        this.view = LayoutInflater.from(activity).inflate(R.layout.pop_window_menu, null);
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高,不设置显示不出来
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);
        // 设置外部可点击
        this.setOutsideTouchable(true);

        // 设置弹出窗体的背景
        this.setBackgroundDrawable(activity.getResources().getDrawable(R.color.white));

        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.pop_window_animation);


        // 添加OnTouchListener监听判断获取触屏位置，如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int height = view.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });


        nameTv = (TextView) view.findViewById(R.id.popwin_name_tv);
        addLl = (LinearLayout) view.findViewById(R.id.popwin_add_rl);
        loveLl = (LinearLayout) view.findViewById(R.id.popwin_love_ll);
        ringLl = (LinearLayout) view.findViewById(R.id.popwin_ring_ll);
        deleteLl = (LinearLayout) view.findViewById(R.id.popwin_delete_ll);
        cancelLl = (LinearLayout) view.findViewById(R.id.popwin_cancel_ll);
        nameTv.setText("歌曲： " + music.getSongName());
        addLl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DBManager.insert(music);
                Toasts.showShort(activity, "添加列表成功！");
            }
        });

        loveLl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onUpdateListener.onAddLoveUpdate();
                dismiss();
            }
        });

        ringLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                MusicUtil.setMyRingtone(activity, Environment.getExternalStorageDirectory().getPath()
                        + "/Assistant/music/" + music.getSongName() + ".mp3");
                dismiss();
            }
        });

        deleteLl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onUpdateListener.onDeleteUpdate();
                dismiss();
            }
        });

        cancelLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dismiss();
            }
        });
        if (flag) {
            loveLl.setVisibility(View.VISIBLE);
            deleteLl.setVisibility(View.GONE);
        } else {
            loveLl.setVisibility(View.GONE);
            deleteLl.setVisibility(View.VISIBLE);

        }
    }


    private OnUpdateListener onUpdateListener;

    public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
        this.onUpdateListener = onUpdateListener;
    }

    public interface OnUpdateListener {
        void onDeleteUpdate();

        void onAddLoveUpdate();
    }
}
