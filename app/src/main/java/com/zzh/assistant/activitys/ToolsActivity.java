package com.zzh.assistant.activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.zzh.assistant.R;
import com.zzh.assistant.adapters.AdapterNews;
import com.zzh.assistant.adapters.AdapterTools;
import com.zzh.assistant.entities.Common;
import com.zzh.assistant.utils.DividerGridItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ToolsActivity extends Activity {
    private RecyclerView recyclerView;
    private AdapterTools adapterTools;
    private ImageView back;
    private String[] title = {"文字识别", "百度地图", "二维码生成"};
    private int[] img = {R.drawable.ocr, R.drawable.map, R.drawable.qr_code};
    private List<Common> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.activity_tools);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.tools_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(ToolsActivity.this, 3));
//        recyclerView.addItemDecoration(new DividerItemDecoration(ToolsActivity.this, DividerItemDecoration.VERTICAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(ToolsActivity.this, DividerItemDecoration.HORIZONTAL));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(ToolsActivity.this,R.color.grey500));
        back = (ImageView) findViewById(R.id.tools_back);
    }

    private void initData() {
        Common c;
        for (int i = 0; i < title.length; i++) {
            c = new Common();
            c.setTitle(title[i]);
            c.setImg(img[i]);
            list.add(c);
        }
        adapterTools = new AdapterTools(ToolsActivity.this, list);
        recyclerView.setAdapter(adapterTools);
    }

    private void initEvent() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        adapterTools.setOnItemClickListener(new AdapterNews.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                switch (position) {
                    case 0:
                        startActivity(new Intent(ToolsActivity.this, OCRActivity.class));
                        break;
                    case 1:
                        startActivity(new Intent(ToolsActivity.this, MapActivity.class));
                        break;
                    case 2:
                        startActivity(new Intent(ToolsActivity.this, QRCodeActivity.class));
                        break;
                }
            }
        });
    }
}
