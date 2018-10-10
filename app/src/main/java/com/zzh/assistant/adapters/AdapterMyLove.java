package com.zzh.assistant.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.MusicUtil;

import java.util.List;

public class AdapterMyLove extends RecyclerView.Adapter<AdapterMyLove.ViewHolder>
        implements SectionIndexer {
    private Context context;
    private List<Music> lists;
    private OnItemClickListener mOnItemClickListener = null;

    public AdapterMyLove(Context context, List<Music> lists) {
        this.context = context;
        this.lists = lists;
    }

    public interface OnItemClickListener {
        void onClick(int position);

        void onClickMenu(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public AdapterMyLove.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_music, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterMyLove.ViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        holder.sing.setText(lists.get(position).getSingerName());
        holder.song.setText(lists.get(position).getSongName());

        int appbg = CommonUtil.getAttrColorValue(R.attr.colorAccent, 0xFFFA7298, context);
        int defaultTvColor = CommonUtil.getAttrColorValue(R.attr.text_color, R.color.grey700, context);
        if (lists.get(position).getId() == MusicUtil.getIntShared(context,Constant.KEY_ID)) {
            holder.song.setTextColor(appbg);
            holder.sing.setTextColor(appbg);
        } else {
            holder.song.setTextColor(defaultTvColor);
            holder.sing.setTextColor(context.getResources().getColor(R.color.grey700));
        }
        int section = getSectionForPosition(position);
        int firstPosition = getPositionForSection(section);
        if (firstPosition == position) {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setText("" + lists.get(position).getFirstLetter());
        } else {
            holder.header.setVisibility(View.GONE);
        }
        RequestOptions options = new RequestOptions()
                .priority(Priority.HIGH)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error_image);
        Glide.with(context).load(lists.get(position).getAlbumpic_small()).apply(options).into(holder.small);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onClick(position);
                }
            });
        }
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getLayoutPosition();
                mOnItemClickListener.onClickMenu(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView sing, song, header;
        private ImageView menu, small;

        public ViewHolder(View itemView) {
            super(itemView);
            sing = (TextView) itemView.findViewById(R.id.music_sing_name);
            song = (TextView) itemView.findViewById(R.id.music_song_name);
            menu = (ImageView) itemView.findViewById(R.id.music_menu);
            small = (ImageView) itemView.findViewById(R.id.music_small);
            header = (TextView) itemView.findViewById(R.id.music_head_tv);
        }
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的item的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            char firstChar = lists.get(i).getFirstLetter().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        return lists.get(position).getFirstLetter().charAt(0);
    }
}
