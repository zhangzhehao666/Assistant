package com.zzh.assistant.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.zzh.assistant.entities.News;

import java.util.List;

public class AdapterMusicFragment extends RecyclerView.Adapter<AdapterMusicFragment.ViewHolder> {
    private Context context;
    private List<Music> lists;
    private OnItemClickListener mOnItemClickListener = null;

    public AdapterMusicFragment(Context context, List<Music> lists) {
        this.context = context;
        this.lists = lists;
    }

    public interface OnItemClickListener {
        void onClick(int position,ImageView iv);

        void onClickMenu(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public AdapterMusicFragment.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_music, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AdapterMusicFragment.ViewHolder holder, final int position) {
        holder.itemView.setTag(position);
        holder.sing.setText(lists.get(position).getSingerName());
        holder.song.setText(lists.get(position).getSongName());
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
                    mOnItemClickListener.onClick(position,holder.small);
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
        private TextView sing, song;
        private ImageView menu, small;

        public ViewHolder(View itemView) {
            super(itemView);
            sing = (TextView) itemView.findViewById(R.id.music_sing_name);
            song = (TextView) itemView.findViewById(R.id.music_song_name);
            menu = (ImageView) itemView.findViewById(R.id.music_menu);
            small = (ImageView) itemView.findViewById(R.id.music_small);
        }
    }
}
