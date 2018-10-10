package com.zzh.assistant.activitys;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzh.assistant.R;


public class WebViewDetailActivity extends BaseActivity implements View.OnClickListener {
    private WebView webView;
    private ImageView imgBack;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_webview);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        webView = (WebView) findViewById(R.id.show_webView);
        imgBack = (ImageView) findViewById(R.id.show_webView_back);
        txtTitle = (TextView) findViewById(R.id.show_webView_name);
    }

    private void initData() {
        txtTitle.setText(getIntent().getStringExtra("name"));
        webView.loadUrl(getIntent().getStringExtra("url"));
    }

    private void initEvent() {
        imgBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_webView_back:
                finish();
                break;
        }
    }
}
