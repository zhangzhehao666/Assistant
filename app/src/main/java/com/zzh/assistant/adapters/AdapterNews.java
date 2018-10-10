package com.zzh.assistant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzh.assistant.R;
import com.zzh.assistant.entities.News;

import java.util.ArrayList;
import java.util.List;

public class AdapterNews extends RecyclerView.Adapter<AdapterNews.ViewHolder> {
    private Context context;
    private List<News> lists;
    private OnItemClickListener mOnItemClickListener = null;

    public AdapterNews(Context context, List<News> lists) {
        this.context = context;
        this.lists = lists;
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public AdapterNews.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_news, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterNews.ViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        holder.title.setText(lists.get(position).getTitle());
        holder.name.setText(lists.get(position).getName());
        holder.time.setText(lists.get(position).getDate());
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onClick(position);
                }
            });
        }
        AdapterNewsImg adapterNewsImg=new AdapterNewsImg(context,lists.get(position).getImageList());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        holder.recyclerView.setAdapter(adapterNewsImg);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name, time, title;
        private RecyclerView recyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item_news_name);
            time = (TextView) itemView.findViewById(R.id.item_news_time);
            title = (TextView) itemView.findViewById(R.id.item_news_title);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.item_news_recycler);
        }
    }

}
