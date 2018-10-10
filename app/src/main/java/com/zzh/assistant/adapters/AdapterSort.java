package com.zzh.assistant.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzh.assistant.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterSort extends RecyclerView.Adapter<AdapterSort.ViewHolder> {
    private Context context;
    private List<String> lists;
    private OnItemClickListener mOnItemClickListener = null;
    private List<Boolean> isClicks;

    public AdapterSort(Context context, List<String> lists) {
        this.context = context;
        this.lists = lists;
        isClicks = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            isClicks.add(false);
        }
        isClicks.add(0, true);
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public AdapterSort.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_sort, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterSort.ViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        holder.textView.setText(lists.get(position) + "");
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    for (int i = 0; i < lists.size(); i++) {
                        isClicks.set(i, false);
                    }
                    isClicks.set(position, true);
                    notifyDataSetChanged();
                    mOnItemClickListener.onClick(position);
                }
            });
        }
        if (isClicks.get(position)) {
            holder.view.setVisibility(View.VISIBLE);
            holder.view.post(new Runnable() {
                @Override
                public void run() {
                    //获取要 textview的宽度
                    holder.textView.measure(0, 0);
                    int width = holder.textView.getWidth();
                    //获取 线 的 layout参数
                    ViewGroup.LayoutParams lineParams = holder.view.getLayoutParams();
                    //将 textview 的宽度  设置给  线的
                    lineParams.width = width - 6;
                    holder.view.setLayoutParams(lineParams);
                }
            });
            holder.textView.setTextColor(Color.parseColor("#0087cf"));
        } else {
            holder.view.setVisibility(View.INVISIBLE);
            holder.textView.setTextColor(Color.parseColor("#666666"));
        }
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private View view;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_news_txt);
            view = (View) itemView.findViewById(R.id.item_news_view);
        }
    }
}
