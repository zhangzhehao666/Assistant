package com.zzh.assistant.adapters;

import android.content.Context;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.News;

import java.util.List;

public class AdapterNewsImg extends RecyclerView.Adapter<AdapterNewsImg.ViewHolder> {
    private Context context;
    private List<String> lists;

    public AdapterNewsImg(Context context, List<String> lists) {
        this.context = context;
        this.lists = lists;
    }

    @NonNull
    @Override
    public AdapterNewsImg.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_news_img, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterNewsImg.ViewHolder holder, final int position) {
        final RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH)
                .placeholder(R.drawable.loading)
                .error(R.drawable.load_error);
        Handler handle = new Handler(Looper.getMainLooper());
        handle.post(new Runnable() {
            @Override
            public void run() {
                Glide.with(context).load(lists.get(position)).apply(options).into(holder.imageView);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_news_img);
        }
    }
}
