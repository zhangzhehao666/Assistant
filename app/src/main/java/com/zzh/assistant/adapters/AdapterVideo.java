package com.zzh.assistant.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.Funny;
import com.zzh.assistant.entities.Video;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.CustomTransformer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.ViewHolder> {
    private Context context;
    private List<Video> list;

    public AdapterVideo(Context context, List<Video> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdapterVideo.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_video, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterVideo.ViewHolder holder, final int position) {
        holder.video.backButton.setVisibility(View.GONE);
        holder.video.setUp(list.get(position).getVideoUrl()
                , JZVideoPlayer.SCREEN_WINDOW_LIST, list.get(position).getTitle());
//        holder.video.thumbImageView.setImageBitmap(CommonUtil.getVideoThumbnail(list.get(position).getVideoUrl()));
        Handler handle = new Handler(Looper.getMainLooper());
        handle.post(new Runnable() {
            @Override
            public void run() {
                Picasso.with(context).load(list.get(position).getImage())
                        .error(R.drawable.load_error).into(holder.video.thumbImageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private JZVideoPlayerStandard video;

        public ViewHolder(View itemView) {
            super(itemView);
            video = (JZVideoPlayerStandard) itemView.findViewById(R.id.item_video);
        }
    }
}
