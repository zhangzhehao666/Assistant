package com.zzh.assistant.adapters;

import android.content.Context;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.Funny;
import com.zzh.assistant.utils.CustomTransformer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdapterFunny extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Funny> list;
    private int type;

    public AdapterFunny(Context context, List<Funny> list) {
        this.context = context;
        this.list = list;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getItemViewType(int position) {
        return type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder holder = null;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_funny_txt, parent, false);
            holder = new TxtViewHolder(view);
            return holder;
        } else if (viewType == 1) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fragment_funny_pic, parent, false);
            holder = new PicViewHolder(view);
            return holder;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (type) {
            case 0:
                final TxtViewHolder txtViewHolder = (TxtViewHolder) holder;
                if (list.get(position).getImage().equals("")) {
                    Picasso.with(context).load(R.drawable.default_img)
                            .transform(new CustomTransformer()).into(txtViewHolder.imageView);
                } else {
                    Handler handle = new Handler(Looper.getMainLooper());
                    handle.post(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.with(context).load(list.get(position).getImage()).error(R.drawable.load_error)
                                    .transform(new CustomTransformer()).into(txtViewHolder.imageView);
                        }
                    });
                }
                txtViewHolder.name.setText(list.get(position).getName());
                txtViewHolder.context.setText("\t\t\t\t" + list.get(position).getContext());
                long time = Long.parseLong(list.get(position).getTime());
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
                Date strToDate = new Date(time);
                String s = sdf.format(strToDate);
                txtViewHolder.time.setText(s);
                break;
            case 1:
                final PicViewHolder picViewHolder = (PicViewHolder) holder;
                final RequestOptions options = new RequestOptions()
                        .priority(Priority.HIGH)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.load_error);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        picViewHolder.imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(context).load(list.get(position).getImage())
                                        .apply(options).into(picViewHolder.imageView);
                            }
                        });
                    }
                }).start();

    /*            Handler handle = new Handler(Looper.getMainLooper());
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        picViewHolder.imageView.post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(context).load(list.get(position).getImage())
                                        .apply(options).into(picViewHolder.imageView);
                            }
                        });
                    }
                });*/
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class TxtViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView name, context, time;

        public TxtViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_funny_img);
            name = (TextView) itemView.findViewById(R.id.item_funny_name);
            context = (TextView) itemView.findViewById(R.id.item_funny_context);
            time = (TextView) itemView.findViewById(R.id.item_funny_time);
        }
    }

    public class PicViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public PicViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.item_funny_gif_img);
        }
    }
}
