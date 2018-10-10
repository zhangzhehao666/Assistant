package com.zzh.assistant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dengzq.simplerefreshlayout.SimpleRefreshLayout;
import com.zzh.assistant.R;
import com.zzh.assistant.activitys.WebViewDetailActivity;
import com.zzh.assistant.adapters.AdapterNews;
import com.zzh.assistant.adapters.AdapterSort;
import com.zzh.assistant.entities.News;
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

import static com.zzh.assistant.http.ServerUrl.NewsUrl;

public class NewsFragment extends Fragment {
    private View rootView;
    private RecyclerView recyclerViewSort, recyclerView;
    private List<String> list, imageList;
    private AdapterSort adapterNewsSort;
    private String[] title = {"头条", "社会", "国内", "国际", "娱乐", "体育", "军事", "科技", "财经", "时尚"};
    private List<News> newsList = new ArrayList<>();
    private int page = 1, lastOffset, lastPosition;
    private RequestQueue queue;
    private DialogProcess dialog;
    private SimpleRefreshLayout refreshLayout;
    private static final int STATUS_REFRESH = 1;
    private static final int STATUS_LOAD = 2;
    private int mDataStatus = STATUS_REFRESH;
    private boolean isLoadMore = false;
    private AdapterNews adapterNews;
    private String type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_news, container, false);
            initView();
            initData();
            initEvent();
            showListSort();
            getData();
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void initView() {
        recyclerViewSort = (RecyclerView) rootView.findViewById(R.id.news_recycler_sort);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.news_recycler);
        refreshLayout = (SimpleRefreshLayout) rootView.findViewById(R.id.news_refresh);
        refreshLayout.setPullUpEnable(true);
        refreshLayout.setPullDownEnable(true);
        refreshLayout.setHeaderView(new SimpleRefreshView(getActivity()));
        refreshLayout.setFooterView(new SimpleLoadView(getActivity()));
        refreshLayout.setBottomView(new SimpleBottomView(getActivity()));
        refreshLayout.setEffectivePullDownRange(CommonUtil.Dp2Px(getActivity(), 70));
    }

    private void initData() {
        type = "toutiao";
        queue = Volley.newRequestQueue(getActivity());
        dialog = new DialogProcess(getActivity(), R.style.TransparentDialog);
        list = new ArrayList<>();
        for (int i = 0; i < title.length; i++) {
            list.add(title[i]);
        }
    }

    private void initEvent() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView r, int newState) {
                super.onScrollStateChanged(r, newState);
                if (r.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });
        refreshLayout.setOnSimpleRefreshListener(new SimpleRefreshLayout.OnSimpleRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                mDataStatus = STATUS_REFRESH;
                isLoadMore = false;
                getData();
            }

            @Override
            public void onLoadMore() {
                page++;
                mDataStatus = STATUS_LOAD;
                isLoadMore = true;
                getData();
            }
        });
    }

    private void showListSort() {
        adapterNewsSort = new AdapterSort(getActivity(), list);
        recyclerViewSort.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSort.setAdapter(adapterNewsSort);
        adapterNewsSort.setOnItemClickListener(new AdapterSort.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                RecyclerView.LayoutManager layoutManager = recyclerViewSort.getLayoutManager();
                LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                int firstPosition = linearManager.findFirstVisibleItemPosition();
                int lastPosition = linearManager.findLastVisibleItemPosition();
                int left = recyclerViewSort.getChildAt(position - firstPosition).getLeft();
                int right = recyclerViewSort.getChildAt(lastPosition - position).getLeft();
                recyclerViewSort.scrollBy((left - right) / 2, 0);
                type = CommonUtil.stringToPinyin(list.get(position));
                getData();
            }
        });
    }

    private void getData() {
        if (!isLoadMore) {
            dialog.show();
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        params.put("key", "7792bb21683582bcd46d5877ce2df4d8");
        params.put("page", String.valueOf(page));
        CustomRequest request = new CustomRequest(Request.Method.POST, NewsUrl,
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
                    if (jsonObject.getString("reason").equals("成功的返回")) {
                        jsonObject=jsonObject.getJSONObject("result");
                        JSONArray jsonArrayData = jsonObject.getJSONArray("data");
                        if (page == 1) {
                            newsList.clear();
                            refreshLayout.showNoMore(false);
                        }
                        if (jsonArrayData.length() == 0 && page != 1) {
                            Toasts.showShort(getActivity(), "没有更多了");
                            refreshLayout.showNoMore(true);
                        }
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            News news = new News();
                            JSONObject object = jsonArrayData.getJSONObject(i);
                            news.setName(object.getString("author_name"));
                            news.setTitle(object.getString("title"));
                            news.setDate(object.getString("date"));
                            news.setUrl(object.getString("url"));
                            imageList = new ArrayList<>();
                            imageList.add(object.getString("thumbnail_pic_s"));
                            if (object.has("thumbnail_pic_s02")) {
                                imageList.add(object.getString("thumbnail_pic_s02"));
                            }
                            if (object.has("thumbnail_pic_s03")) {
                                imageList.add(object.getString("thumbnail_pic_s03"));
                            }
                            news.setImageList(imageList);
                            newsList.add(news);
                        }
                        showList();
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
                Log.d("NewsFragment", error.toString());
            }
        });
        queue.add(request);
    }

    private void showList() {
        adapterNews = new AdapterNews(getActivity(), newsList);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapterNews);
        adapterNews.setOnItemClickListener(new AdapterNews.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), WebViewDetailActivity.class);
                intent.putExtra("url", newsList.get(position).getUrl());
                intent.putExtra("name", newsList.get(position).getName());
                startActivity(intent);
            }
        });
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
}
