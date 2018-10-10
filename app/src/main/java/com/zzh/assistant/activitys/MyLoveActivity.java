package com.zzh.assistant.activitys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.zzh.assistant.R;
import com.zzh.assistant.adapters.AdapterMusicFragment;
import com.zzh.assistant.adapters.AdapterMyLove;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.fragments.PlayBarFragment;
import com.zzh.assistant.global.MyUser;
import com.zzh.assistant.http.CustomRequest;
import com.zzh.assistant.receiver.PlayerManagerReceiver;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.utils.Toasts;
import com.zzh.assistant.views.MusicPopMenuWindow;
import com.zzh.assistant.views.SideBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.zzh.assistant.http.ServerUrl.QQMusic;

public class MyLoveActivity extends BaseActivity {
    private Toolbar toolbar;
    private TextView txtSideBar;
    private RelativeLayout llNothing;
    private RecyclerView recyclerView;
    private AdapterMyLove adapterMusicFragment;
    private SideBar sideBar;
    private List<Music> musicList = new ArrayList<>();
    private PlayBarFragment playBarFragment;
    private UpdateReceiver mReceiver;
    private DBManager dbManager;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylove_music);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.mylove_music_toolbar);
        llNothing = (RelativeLayout) findViewById(R.id.mylove_nothing_ll);
        txtSideBar = (TextView) findViewById(R.id.mylove_music_sidebar_pre_tv);
        recyclerView = (RecyclerView) findViewById(R.id.mylove_music_recycler);
        sideBar = (SideBar) findViewById(R.id.mylove_music_sidebar);
    }

    private void initData() {
        queue = Volley.newRequestQueue(MyLoveActivity.this);
        dbManager = new DBManager(MyLoveActivity.this);
        register();
        getMusicList();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("我的喜欢");
        }
        sideBar.setTextView(txtSideBar);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (playBarFragment == null) {
            playBarFragment = PlayBarFragment.newInstance();
            ft.add(R.id.fragment_playbar, playBarFragment).commit();
        } else {
            ft.show(playBarFragment).commit();
        }
    }

    private void initEvent() {
        sideBar.setOnListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                //该字母首次出现的位置
                int position = adapterMusicFragment.getPositionForSection(letter.charAt(0));
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        });
    }

    private void getMusicList() {
        BmobQuery<Music> query = new BmobQuery<Music>();
        query.addWhereEqualTo("userId", MyUser.objectId);
        query.setLimit(500);
        query.findObjects(new FindListener<Music>() {
            @Override
            public void done(List<Music> object, BmobException e) {
                if (e == null) {
                    musicList = object;
                    if (musicList.size() == 0) {
                        llNothing.setVisibility(View.VISIBLE);
                        sideBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        llNothing.setVisibility(View.GONE);
                        sideBar.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    Collections.sort(musicList);
                    showListModel();
                } else {
                    Log.d("getMusicList", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    private void listModel(final int i) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("showapi_appid", "59751");
        params.put("showapi_sign", "4c19b0f7d772496aba7a347db67a26db");
        params.put("keyword", musicList.get(i).getSongName());
        params.put("page", "1");
        CustomRequest request = new CustomRequest(Request.Method.POST, QQMusic + "1",
                params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                try {
                    JSONObject jsonObject = new JSONObject(s.toString());
                    if (jsonObject.getInt("showapi_res_code") == 0) {
                        jsonObject = jsonObject.getJSONObject("showapi_res_body")
                                .getJSONObject("pagebean");
                        JSONArray jsonArrayData = jsonObject.getJSONArray("contentlist");
//                        showMusicList.clear();
                        Music m = new Music();
                        JSONObject object = jsonArrayData.getJSONObject(0);
                        m.setSingerName(object.getString("singername"));
                        m.setSongName(object.getString("songname"));
                        m.setUrl(object.getString("m4a"));
                        m.setDownUrl(object.getString("downUrl"));
                        m.setAlbumpic_small(object.getString("albumpic_small"));
                        m.setAlbumpic_big(object.getString("albumpic_big"));
                        m.setSongmid(object.getString("songmid"));

                        dbManager.insert(m);
                        String path = dbManager.getMusicMid(dbManager.getMaxId());
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                        intent.putExtra(Constant.KEY_PATH, path);
                        intent.putExtra("fileName",dbManager.getMusicName(dbManager.getMaxId()));
                        sendBroadcast(intent);
                        adapterMusicFragment.notifyDataSetChanged();
                        MusicUtil.setShared(MyLoveActivity.this, Constant.KEY_LIST, Constant.LIST_ALLMUSIC);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NewsFragment", error.toString());
            }
        });
        queue.add(request);
    }

    private void showListModel() {
        adapterMusicFragment = new AdapterMyLove(MyLoveActivity.this, musicList);
        recyclerView.setLayoutManager(new LinearLayoutManager(MyLoveActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(MyLoveActivity.this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapterMusicFragment);
        adapterMusicFragment.setOnItemClickListener(new AdapterMyLove.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                listModel(position);
//                Toasts.showShort(MyLoveActivity.this, "添加列表成功！");
            }

            @Override
            public void onClickMenu(int position) {
                showPopFormBottom(musicList.get(position), position);
            }
        });
    }

    public void showPopFormBottom(final Music m, final int position) {
        MusicPopMenuWindow menuPopupWindow = new MusicPopMenuWindow(MyLoveActivity.this, m, false);
//      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = MyLoveActivity.this.getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        MyLoveActivity.this.getWindow().setAttributes(params);
        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = MyLoveActivity.this.getWindow().getAttributes();
                params.alpha = 1f;
                MyLoveActivity.this.getWindow().setAttributes(params);
            }
        });
        menuPopupWindow.setOnUpdateListener(new MusicPopMenuWindow.OnUpdateListener() {
            @Override
            public void onDeleteUpdate() {
                musicList.remove(position);
                adapterMusicFragment.notifyDataSetChanged();
                MusicUtil.deleteOperate(m, MyLoveActivity.this);
            }

            @Override
            public void onAddLoveUpdate() {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.love_music_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.scan_local_menu) {
            Intent intent = new Intent(MyLoveActivity.this, DownLrcActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }


    private void register() {
        try {
            if (mReceiver != null) {
                this.unRegister();
            }
            mReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayerManagerReceiver.ACTION_UPDATE_UI_ADAPTER);
            this.registerReceiver(mReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (mReceiver != null) {
                this.unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            adapterMusicFragment.notifyDataSetChanged();
        }
    }
}
