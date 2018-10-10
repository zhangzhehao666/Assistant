package com.zzh.assistant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzh.assistant.R;
import com.zzh.assistant.entities.Common;

import java.util.ArrayList;
import java.util.List;

public class AdapterTools extends RecyclerView.Adapter<AdapterTools.ViewHolder> {
    private Context context;
    private List<Common> list = new ArrayList<>();
    private AdapterNews.OnItemClickListener mOnItemClickListener = null;

    public AdapterTools(Context context, List<Common> list) {
        this.context = context;
        this.list = list;
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(AdapterNews.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tools, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.imageView.setImageResource(list.get(position).getImg());
        holder.textView.setText(list.get(position).getTitle());
        if (mOnItemClickListener != null) {
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        private LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_tools_img);
            textView = (TextView) itemView.findViewById(R.id.item_tools_txt);
            linearLayout=(LinearLayout)itemView.findViewById(R.id.item_tools_ll);
        }
    }
}
