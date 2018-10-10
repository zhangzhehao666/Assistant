package com.zzh.assistant.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zzh.assistant.R;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.MusicUtil;

import java.util.List;

public class PlayingPopWindow extends PopupWindow {

    private static final String TAG = PlayingPopWindow.class.getName();
    private View view;
    private Activity activity;
    private TextView countTv;
    private RelativeLayout closeRv;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<Music> musicInfoList;
    private DBManager dbManager;

    public PlayingPopWindow(Activity activity) {
        super(activity);
        this.activity = activity;
        dbManager = new DBManager(activity);
        musicInfoList = dbManager.getPlayList();
        initView();
    }

    private void initView() {
        this.view = LayoutInflater.from(activity).inflate(R.layout.playbar_menu_window, null);
        this.setContentView(this.view);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        int height = (int) (size.y * 0.5);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(height);

        this.setFocusable(true);
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

        recyclerView = (RecyclerView) view.findViewById(R.id.playing_list_rv);
        adapter = new Adapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        closeRv = (RelativeLayout) view.findViewById(R.id.playing_list_close_rv);
        countTv = (TextView) view.findViewById(R.id.playing_list_count_tv);
        countTv.setText("(" + musicInfoList.size() + ")");
        closeRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout contentLl;
            TextView nameTv;
            TextView singerTv;
            ImageView delete;

            public ViewHolder(View itemView) {
                super(itemView);
                this.contentLl = (RelativeLayout) itemView.findViewById(R.id.palybar_list_item_ll);
                this.nameTv = (TextView) itemView.findViewById(R.id.palybar_list_item_name_tv);
                this.singerTv = (TextView) itemView.findViewById(R.id.palybar_list_item_singer_tv);
                this.delete = (ImageView) itemView.findViewById(R.id.palybar_list_delete);
            }
        }

        @Override
        public int getItemCount() {
            return musicInfoList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_playbar_rv_list, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            Log.d(TAG, "onBindViewHolder: position = " + position);
            final Music musicInfo = musicInfoList.get(position);
            holder.nameTv.setText(musicInfo.getSongName());
            holder.singerTv.setText(musicInfo.getSingerName());

            if (musicInfo.getId() == MusicUtil.getIntShared(activity, Constant.KEY_ID)) {
                holder.nameTv.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                holder.singerTv.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            } else {
                holder.nameTv.setTextColor(activity.getResources().getColor(R.color.grey700));
                holder.singerTv.setTextColor(activity.getResources().getColor(R.color.grey500));
            }
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int curId = musicInfo.getId();
                    final int musicId = MusicUtil.getIntShared(activity, Constant.KEY_ID);
                    if (curId == musicId) {
                        //移除的是当前播放的音乐
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        activity.sendBroadcast(intent);
                    }
                    musicInfoList.remove(position);
//                    adapter.notifyItemRemoved(position);
                    dbManager.deleteID(musicInfo.getId());
                    adapter.notifyDataSetChanged();
                    countTv.setText("(" + musicInfoList.size() + ")");
                }
            });
            holder.contentLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = dbManager.getMusicMid(musicInfo.getId());
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    intent.putExtra("fileName",dbManager.getMusicName(musicInfo.getId()));
                    activity.sendBroadcast(intent);
                    MusicUtil.setShared(activity, Constant.KEY_ID, musicInfo.getId());
                    notifyDataSetChanged();
                    dismiss();
                }
            });
        }
    }
}
