package com.zzh.assistant.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dengzq.simplerefreshlayout.IHeaderWrapper;
import com.zzh.assistant.R;


public class SimpleRefreshView extends LinearLayout implements IHeaderWrapper {

    ImageView mIvRefresh;
    TextView mTvRefresh;
    ProgressBar progressBar;

    public SimpleRefreshView(Context context) {
        this(context, null);
    }

    public SimpleRefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_refresh_header, this, false);
//        mIvRefresh = (ImageView) view.findViewById(R.id.iv_refresh_image);
        mTvRefresh = (TextView) view.findViewById(R.id.tv_refresh_text);
        progressBar=(ProgressBar)view.findViewById(R.id.header_progress);
        LayoutParams params =new LayoutParams(-1,-2);
        params.gravity= Gravity.BOTTOM;
        addView(view,params);
    }

    @Override
    public View getHeaderView() {
        return this;
    }

    @Override
    public void pullDown() {
        mTvRefresh.setText("下拉刷新");
    }

    @Override
    public void pullDownReleasable() {
        mTvRefresh.setText("松开刷新");
    }

    @Override
    public void pullDownRelease() {
        mTvRefresh.setText("正在刷新");
//        AnimationDrawable drawable = (AnimationDrawable) mIvRefresh.getDrawable();
//        drawable.start();
    }
}
