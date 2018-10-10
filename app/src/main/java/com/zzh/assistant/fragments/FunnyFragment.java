package com.zzh.assistant.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dengzq.simplerefreshlayout.SimpleRefreshLayout;
import com.zzh.assistant.R;
import com.zzh.assistant.activitys.BaseActivity;
import com.zzh.assistant.adapters.AdapterFunny;
import com.zzh.assistant.entities.Funny;
import com.zzh.assistant.http.CustomRequest;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.DialogProcess;
import com.zzh.assistant.utils.Toasts;
import com.zzh.assistant.views.SimpleBottomView;
import com.zzh.assistant.views.SimpleLoadView;
import com.zzh.assistant.views.SimpleRefreshView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zzh.assistant.http.ServerUrl.PictureUrl;
import static com.zzh.assistant.http.ServerUrl.Url;


public class FunnyFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private RecyclerView recyclerView;
    private AdapterFunny adapter;
    private List<Funny> list = new ArrayList<>();
    private int page = 1, lastOffset, lastPosition;
    private RequestQueue queue;
    private DialogProcess dialog;
    private SimpleRefreshLayout refreshLayout;
    private static final int STATUS_REFRESH = 1;
    private static final int STATUS_LOAD = 2;
    private int mDataStatus = STATUS_REFRESH;
    private boolean isLoadMore = false, isChooseTxt = true;
    private TextView chooseTxt, chooseGif;
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_funny, container, false);
            initView();
            initData();
            initEvent();
            getTxtData();
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }


    private void initView() {
        toolbar = (Toolbar) rootView.findViewById(R.id.funny_toolbar);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.joke_recycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        refreshLayout = (SimpleRefreshLayout) rootView.findViewById(R.id.funny_refresh);
        refreshLayout.setPullUpEnable(true);
        refreshLayout.setPullDownEnable(true);
        refreshLayout.setHeaderView(new SimpleRefreshView(getActivity()));
        refreshLayout.setFooterView(new SimpleLoadView(getActivity()));
        refreshLayout.setBottomView(new SimpleBottomView(getActivity()));
        refreshLayout.setEffectivePullDownRange(CommonUtil.Dp2Px(getActivity(), 70));
        chooseTxt = (TextView) rootView.findViewById(R.id.txt_funny_txt);
        chooseGif = (TextView) rootView.findViewById(R.id.txt_funny_gif);
    }

    private void initData() {
        setHasOptionsMenu(true);
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
        }
        queue = Volley.newRequestQueue(getActivity());
        dialog = new DialogProcess(getActivity(), R.style.TransparentDialog);
        chooseTxt.setBackgroundResource(R.drawable.bg_linear_borde_hover);
    }

    private void initEvent() {
        chooseTxt.setOnClickListener(this);
        chooseGif.setOnClickListener(this);
        refreshLayout.setOnSimpleRefreshListener(new SimpleRefreshLayout.OnSimpleRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                mDataStatus = STATUS_REFRESH;
                isLoadMore = false;
                if (isChooseTxt) {
                    getTxtData();
                } else {
                    getGifData();
                }
            }

            @Override
            public void onLoadMore() {
                page++;
                mDataStatus = STATUS_LOAD;
                isLoadMore = true;
                if (isChooseTxt) {
                    getTxtData();
                } else {
                    getGifData();
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView r, int newState) {
                super.onScrollStateChanged(r, newState);
                if (r.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_funny_txt:
                chooseTxt.setBackgroundResource(R.drawable.bg_linear_borde_hover);
                chooseGif.setBackgroundResource(R.drawable.bg_linear_borde);
                isChooseTxt = true;
                list = new ArrayList<>();
                getTxtData();
                break;
            case R.id.txt_funny_gif:
                chooseGif.setBackgroundResource(R.drawable.bg_linear_borde_hover);
                chooseTxt.setBackgroundResource(R.drawable.bg_linear_borde);
                isChooseTxt = false;
                list = new ArrayList<>();
                getGifData();
                break;
        }
    }

    public void getTxtData() {
        if (!isLoadMore) {
            dialog.show();
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "2");
        params.put("page", String.valueOf(page));
        CustomRequest request = new CustomRequest(Request.Method.POST, Url,
                params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                dialog.dismiss();
                switch (mDataStatus) {
                    case STATUS_REFRESH:
                        refreshLayout.onRefreshComplete();
                        break;
                    case STATUS_LOAD:
                        refreshLayout.onLoadMoreComplete();
                        break;
                }
                try {
                    JSONObject jsonObject = new JSONObject(s.toString());
                    if (jsonObject.getInt("code") == 200) {
                        JSONArray jsonArrayData = jsonObject.getJSONArray("data");
                        if (page == 1) {
                            list.clear();
                            refreshLayout.showNoMore(false);
                        }
                        if (jsonArrayData.length() == 0 && page != 1) {
                            Toasts.showShort(getActivity(), "没有更多了");
                            refreshLayout.showNoMore(true);
                        }
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            Funny funny = new Funny();
                            JSONObject object = jsonArrayData.getJSONObject(i);
                            funny.setName(object.getString("name"));
                            funny.setImage(object.getString("profile_image"));
                            funny.setContext(object.getString("text"));
                            funny.setTime(object.getString("t"));
                            list.add(funny);
                        }
                        showList(0);
                        switch (mDataStatus) {
                            case STATUS_LOAD:
                                CommonUtil.scrollToPosition(recyclerView, lastOffset, lastPosition);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getTxtData", error.toString());
            }
        });
        queue.add(request);
    }

    private void showList(int type) {
        adapter = new AdapterFunny(getActivity(), list);
        adapter.setType(type);
        if (type == 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL));
        }
        recyclerView.setAdapter(adapter);
    }

    private void getGifData() {
        if (!isLoadMore) {
            dialog.show();
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("showapi_appid", "59751");
        params.put("showapi_sign", "4c19b0f7d772496aba7a347db67a26db");
        params.put("maxResult", "10");
        params.put("type", "10");
        params.put("page", String.valueOf(page));
        CustomRequest request = new CustomRequest(Request.Method.POST, PictureUrl,
                params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                dialog.dismiss();
                switch (mDataStatus) {
                    case STATUS_REFRESH:
                        refreshLayout.onRefreshComplete();
                        break;
                    case STATUS_LOAD:
                        refreshLayout.onLoadMoreComplete();
                        break;
                }
                try {
                    JSONObject jsonObject = new JSONObject(s.toString());
                    if (jsonObject.getInt("showapi_res_code") == 0) {
                        jsonObject = jsonObject.getJSONObject("showapi_res_body");
                        JSONArray jsonArrayData = jsonObject.getJSONArray("contentlist");
                        if (page == 1) {
                            list.clear();
                            refreshLayout.showNoMore(false);
                        }
                        if (jsonArrayData.length() == 0 && page != 1) {
                            Toasts.showShort(getActivity(), "没有更多了");
                            refreshLayout.showNoMore(true);
                        }
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            Funny funny = new Funny();
                            JSONObject object = jsonArrayData.getJSONObject(i);
                            funny.setImage(object.getString("img"));
                            list.add(funny);
                        }
                        showList(1);
                        switch (mDataStatus) {
                            case STATUS_LOAD:
                                CommonUtil.scrollToPosition(recyclerView, lastOffset, lastPosition);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("getGifData", error.toString());
            }
        });
        queue.add(request);
    }

    //记录刷新之前的位置
    public void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //获取可视的第一个view
        View topView = layoutManager.getChildAt(0);
        if (topView != null) {
            //获取与该view的顶部的偏移量
            lastOffset = topView.getTop();
            //得到该View的数组位置
            lastPosition = layoutManager.getPosition(topView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dialog.dismiss();
    }
}
