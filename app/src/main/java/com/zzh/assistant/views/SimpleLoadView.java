package com.zzh.assistant.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dengzq.simplerefreshlayout.IFooterWrapper;
import com.zzh.assistant.R;

public class SimpleLoadView extends LinearLayout implements IFooterWrapper {

    private ImageView mIvLoad;
    private TextView mTvLoad;
    ProgressBar progressBar;

    public SimpleLoadView(Context context) {
        this(context, null);
    }

    public SimpleLoadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleLoadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_refresh_footer, this, false);
//        mIvLoad = (ImageView) view.findViewById(R.id.iv_load_image);
        progressBar=(ProgressBar)view.findViewById(R.id.footer_progress);
        mTvLoad = (TextView) view.findViewById(R.id.tv_load_text);
        addView(view);
    }

    @Override
    public View getFooterView() {
        return this;
    }

    @Override
    public void pullUp() {
        mTvLoad.setText("上拉加载");
    }

    @Override
    public void pullUpReleasable() {
        mTvLoad.setText("松开加载");
    }

    @Override
    public void pullUpRelease() {
        mTvLoad.setText("正在加载");
//        AnimationDrawable drawable = (AnimationDrawable) mIvLoad.getDrawable();
//        drawable.start();
    }

    @Override
    public void pullUpFinish() {
        //do anything you want
        //such as show a toast like "load more finish with 10 new messages"
    }
}
