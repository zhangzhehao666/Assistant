package com.zzh.assistant.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.EntityImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdapterImageGrid extends BaseAdapter {
    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;
    private Context context;
    private LayoutInflater inflater;
    private List<EntityImage> listImage = new ArrayList<EntityImage>();
    private List<EntityImage> listSelectedImage = new ArrayList<EntityImage>();
    private int mItemSize;
    private boolean showCamera = true;
    private boolean showSelectIndicator = true;
    private GridView.LayoutParams mItemLayoutParams;

    public AdapterImageGrid(Context context, boolean showCamera) {
        this.context = context;
        this.showCamera = showCamera;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
    }

    /**
     * 显示选择指示器
     *
     * @param b
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public void setShowCamera(boolean b) {
        if (showCamera == b) return;

        showCamera = b;
        notifyDataSetChanged();
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    /**
     * 选择某个图片，改变选择状态
     *
     * @param image
     */
    public void select(EntityImage image) {
        if (listSelectedImage.contains(image)) {
            listSelectedImage.remove(image);
        } else {
            listSelectedImage.add(image);
        }
        notifyDataSetChanged();
    }

    private EntityImage getImageByPath(String path) {
        if (listImage != null && listImage.size() > 0) {
            for (EntityImage image : listImage) {
                if (image.getPathOriginal().equalsIgnoreCase(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<EntityImage> images) {

        if (images != null && images.size() > 0) {
            listImage = images;
        } else {
            listImage.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 重置每个Column的Size
     *
     * @param columnWidth
     */
    public void setItemSize(int columnWidth) {

        if (mItemSize == columnWidth) {
            return;
        }

        mItemSize = columnWidth;

        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);

        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return showCamera ? listImage.size() + 1 : listImage.size();
    }

    @Override
    public Object getItem(int i) {
        if (showCamera) {
            if (i == 0) {
                return null;
            }
            return listImage.get(i - 1);
        } else {
            return listImage.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        int type = getItemViewType(position);
        if (type == TYPE_CAMERA) {
            view = inflater.inflate(R.layout.list_item_camera, viewGroup, false);
            view.setTag(null);
        } else if (type == TYPE_NORMAL) {
            ViewHolde holde;
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_image, viewGroup, false);
                holde = new ViewHolde(view);
            } else {
                holde = (ViewHolde) view.getTag();
                if (holde == null) {
                    view = inflater.inflate(R.layout.list_item_image, viewGroup, false);
                    holde = new ViewHolde(view);
                }
            }
            if (holde != null) {
                holde.bindData((EntityImage) getItem(position));
            }
        }

        /** Fixed View Size */
        GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
        if (lp.height != mItemSize) {
            view.setLayoutParams(mItemLayoutParams);
        }

        return view;
    }

    class ViewHolde {
        ImageView image;
        ImageView indicator;
        View mask;

        ViewHolde(View view) {
            image = (ImageView) view.findViewById(R.id.image);
            indicator = (ImageView) view.findViewById(R.id.checkmark);
            mask = view.findViewById(R.id.mask);
            view.setTag(this);
        }

        void bindData(final EntityImage data) {
            if (data == null) return;
            // 处理单选和多选状态
            if (showSelectIndicator) {
                indicator.setVisibility(View.VISIBLE);
                if (listSelectedImage.contains(data)) {
                    // 设置选中状态
                    indicator.setImageResource(R.drawable.btn_selected);
                    mask.setVisibility(View.VISIBLE);
                } else {
                    // 未选择
                    indicator.setImageResource(R.drawable.btn_unselected);
                    mask.setVisibility(View.GONE);
                }
            } else {
                indicator.setVisibility(View.GONE);
            }
            File imageFile = new File(data.getPathOriginal());

            if (mItemSize > 0) {
                // 显示图片
                Picasso.with(context)
                        .load(imageFile)
                        .placeholder(R.drawable.error_image)
                                //.error(R.drawable.default_error)
                        .resize(mItemSize, mItemSize)
                        .centerCrop()
                        .into(image);
            }
        }
    }
}
