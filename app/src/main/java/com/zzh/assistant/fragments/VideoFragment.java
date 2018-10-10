package com.zzh.assistant.fragments;

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
import com.zzh.assistant.adapters.AdapterVideo;
import com.zzh.assistant.entities.Video;
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

public class VideoFragment extends Fragment {
    private View rootView;
    private RecyclerView recyclerView;
    private AdapterVideo adapter;
    private List<Video> list = new ArrayList<>();
    private int page = 1, lastOffset, lastPosition;
    private RequestQueue queue;
    private DialogProcess dialog;
    private SimpleRefreshLayout refreshLayout;
    private static final int STATUS_REFRESH = 1;
    private static final int STATUS_LOAD = 2;
    private int mDataStatus = STATUS_REFRESH;
    private boolean isLoadMore = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_video, container, false);
            initView();
            initData();
            initEvent();
            getData();
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void initView() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.video_recycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshLayout = (SimpleRefreshLayout) rootView.findViewById(R.id.video_refresh);
        refreshLayout.setPullUpEnable(true);
        refreshLayout.setPullDownEnable(true);
        refreshLayout.setHeaderView(new SimpleRefreshView(getActivity()));
        refreshLayout.setFooterView(new SimpleLoadView(getActivity()));
        refreshLayout.setBottomView(new SimpleBottomView(getActivity()));
        refreshLayout.setEffectivePullDownRange(CommonUtil.Dp2Px(getActivity(), 70));
    }

    private void initData() {
        queue = Volley.newRequestQueue(getActivity());
        dialog = new DialogProcess(getActivity(), R.style.TransparentDialog);
    }

    private void initEvent() {
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });
    }

    public void getData() {
        if (!isLoadMore) {
            dialog.show();
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "4");
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
                            Video video = new Video();
                            JSONObject object = jsonArrayData.getJSONObject(i);
                            video.setImage(object.getString("bimageuri"));
                            video.setVideoUrl(object.getString("videouri"));
                            video.setTitle(object.getString("text"));
                            list.add(video);
                        }
                        showList();
                        switch (mDataStatus) {
                            case STATUS_LOAD:
                                scrollToPosition();
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
                Log.d("VideoFragment", error.toString());
            }
        });
        queue.add(request);
    }

    private void showList() {
        adapter = new AdapterVideo(getActivity(), list);
        recyclerView.setAdapter(adapter);
    }

    //记录刷新之前的位置
    private void getPositionAndOffset() {
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

    /**
     * 让RecyclerView滚动到指定位置
     */
    private void scrollToPosition() {
        if (recyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }
}
