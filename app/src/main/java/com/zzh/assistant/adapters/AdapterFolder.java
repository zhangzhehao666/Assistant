package com.zzh.assistant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.EntityFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdapterFolder extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<EntityFolder> mFolders = new ArrayList<EntityFolder>();
    private int mImageSize;
    private int lastSelected = 0;

    public AdapterFolder(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
    }

    /**
     * 设置数据集
     *
     * @param folders
     */
    public void setData(List<EntityFolder> folders) {
        if (folders != null && folders.size() > 0) {
            mFolders = folders;
        } else {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size() + 1;
    }

    @Override
    public EntityFolder getItem(int i) {
        if (i == 0) return null;
        return mFolders.get(i - 1);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (holder != null) {
            if (i == 0) {
                holder.name.setText("所有图片");
                holder.size.setText(getTotalImageSize() + "张");
                if (mFolders.size() > 0) {
                    EntityFolder f = mFolders.get(0);
                    Picasso.with(mContext)
                            .load(new File(f.cover.getPathOriginal()))
                            .error(R.drawable.error_image)
                            .resize(mImageSize, mImageSize)
                            .centerCrop()
                            .into(holder.cover);
                }
            } else {
                holder.bindData(getItem(i));
            }
            if (lastSelected == i) {
                holder.indicator.setVisibility(View.VISIBLE);
            } else {
                holder.indicator.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    private int getTotalImageSize() {
        int result = 0;
        if (mFolders != null && mFolders.size() > 0) {
            for (EntityFolder f : mFolders) {
                result += f.images.size();
            }
        }
        return result;
    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) return;

        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    class ViewHolder {
        ImageView cover;
        TextView name;
        TextView size;
        ImageView indicator;

        ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(EntityFolder data) {
            name.setText(data.name);
            size.setText(data.images.size() + "张");
            // 显示图片
            Picasso.with(mContext)
                    .load(new File(data.cover.getPathOriginal()))
                    .placeholder(R.drawable.error_image)
                    .resize(mImageSize, mImageSize)
                    .centerCrop()
                    .into(cover);
            // TODO 选择标识
        }
    }
}
