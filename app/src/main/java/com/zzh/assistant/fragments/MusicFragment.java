package com.zzh.assistant.fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.zzh.assistant.R;
import com.zzh.assistant.activitys.LoginActivity;
import com.zzh.assistant.activitys.MainActivity;
import com.zzh.assistant.activitys.MyLoveActivity;
import com.zzh.assistant.activitys.PlayActivity;
import com.zzh.assistant.adapters.AdapterMusicFragment;
import com.zzh.assistant.adapters.AdapterSort;
import com.zzh.assistant.database.DBManager;
import com.zzh.assistant.entities.Music;
import com.zzh.assistant.global.MyUser;
import com.zzh.assistant.http.CustomRequest;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.CommonDialog;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.Constant;
import com.zzh.assistant.utils.DialogProcess;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.utils.Toasts;
import com.zzh.assistant.views.MusicPopMenuWindow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.zzh.assistant.http.ServerUrl.QQMusic;

public class MusicFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private Toolbar toolbar;
    private RelativeLayout reSearch, rl;
    private RecyclerView recyclerView, recyclerViewSort;
    private List<String> list;
    private AdapterSort adapterNewsSort;
    private String[] title = {"流行榜", "新歌", "热歌", "网络歌曲", "K歌金曲", "内地", "港台", "韩国", "欧美", "日本"};
    private RequestQueue queue;
    private int topid = 4;
    private List<Music> musicList = new ArrayList<>();
    private List<Music> showMusicList = new ArrayList<>();
    private AdapterMusicFragment adapterMusicFragment;
    private PlayBarFragment playBarFragment;
    private RelativeLayout myLove;
    private TextView txtLoveNum;
    private DBManager dbManager;
    private DialogProcess dialog;
    private float[] mCurrentPosition = new float[2];
    private FrameLayout frameLayout;
    private PathMeasure mPathMeasure;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null == rootView) {
            rootView = inflater.inflate(R.layout.fragment_music, container, false);
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


    @Override
    public void onResume() {
        super.onResume();
        getMusicList();
    }

    private void initView() {
        toolbar = (Toolbar) rootView.findViewById(R.id.music_toolbar);
        reSearch = (RelativeLayout) rootView.findViewById(R.id.music_re2);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.music_recycler);
        recyclerViewSort = (RecyclerView) rootView.findViewById(R.id.music_recycler_grid);
        myLove = (RelativeLayout) rootView.findViewById(R.id.music_re3);
        txtLoveNum = (TextView) rootView.findViewById(R.id.music_love_num);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.fragment_playbar);
        rl = (RelativeLayout) rootView.findViewById(R.id.music_re4);
    }

    private void initData() {
        dialog = new DialogProcess(getActivity(), R.style.TransparentDialog);
        Intent startIntent = new Intent(getActivity(), MusicPlayerService.class);
        getActivity().startService(startIntent);
        dbManager = new DBManager(getActivity());
        queue = Volley.newRequestQueue(getActivity());
        list = new ArrayList<>();
        for (int i = 0; i < title.length; i++) {
            list.add(title[i]);
        }
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (playBarFragment == null) {
            playBarFragment = PlayBarFragment.newInstance();
            ft.add(R.id.fragment_playbar, playBarFragment).commit();
        } else {
            ft.show(playBarFragment).commit();
        }
    }

    private void initEvent() {
        reSearch.setOnClickListener(this);
        myLove.setOnClickListener(this);
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
                showSort(list.get(position));
                getData();
            }
        });
    }

    private void showSort(String title) {
        switch (title) {
            case "流行榜":
                topid = 4;
                break;
            case "新歌":
                topid = 27;
                break;
            case "热歌":
                topid = 26;
                break;
            case "网络歌曲":
                topid = 28;
                break;
            case "日本":
                topid = 17;
                break;
            case "K歌金曲":
                topid = 36;
                break;
            case "内地":
                topid = 5;
                break;
            case "港台":
                topid = 6;
                break;
            case "韩国":
                topid = 16;
                break;
            case "欧美":
                topid = 3;
                break;
        }
    }

    private void getMusicList() {
        BmobQuery<Music> query = new BmobQuery<Music>();
        query.addWhereEqualTo("userId", MyUser.objectId);
        query.setLimit(500);
        query.findObjects(new FindListener<Music>() {
            @Override
            public void done(List<Music> object, BmobException e) {
                if (e == null) {
                    txtLoveNum.setText(object.size() + "");
                } else {
                    Log.d("getMusicList", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    private void getData() {
        dialog.show();
        Map<String, String> params = new HashMap<String, String>();
        params.put("showapi_appid", "59751");
        params.put("showapi_sign", "4c19b0f7d772496aba7a347db67a26db");
        params.put("topid", String.valueOf(topid));
        CustomRequest request = new CustomRequest(Request.Method.POST, QQMusic + "4",
                params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject s) {
                dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(s.toString());
                    if (jsonObject.getInt("showapi_res_code") == 0) {
                        jsonObject = jsonObject.getJSONObject("showapi_res_body")
                                .getJSONObject("pagebean");
                        JSONArray jsonArrayData = jsonObject.getJSONArray("songlist");
                        musicList.clear();
                        showMusicList.clear();
                        for (int i = 0; i < jsonArrayData.length(); i++) {
                            Music m = new Music();
                            JSONObject object = jsonArrayData.getJSONObject(i);
                            m.setSingerName(object.getString("singername"));
                            m.setSongName(object.getString("songname"));
                            m.setUrl(object.getString("url"));
                            m.setDownUrl(object.getString("downUrl"));
                            m.setAlbumpic_small(object.getString("albumpic_small"));
                            m.setAlbumpic_big(object.getString("albumpic_big"));
                            musicList.add(m);
                        }
                        showListModel();
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
                        intent.putExtra("fileName", dbManager.getMusicName(dbManager.getMaxId()));
                        getActivity().sendBroadcast(intent);
//                        Toasts.showShort(getActivity(), "添加列表成功！");
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

    private void addAnimator(ImageView iv) {
        final ImageView goods = new ImageView(getActivity());
        //goods.setImageResource(R.drawable.collect_true);
        goods.setImageDrawable(iv.getDrawable());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        rl.addView(goods, params);

//        二、计算动画开始/结束点的坐标的准备工作
        //得到父布局的起始点坐标（用于辅助计算动画开始/结束时的点的坐标）
        int[] parentLocation = new int[2];
        rl.getLocationInWindow(parentLocation);

        //得到商品图片的坐标（用于计算动画开始的坐标）
        int startLoc[] = new int[2];
        iv.getLocationInWindow(startLoc);

        //得到购物车图片的坐标(用于计算动画结束后的坐标)
        int endLoc[] = new int[2];
        frameLayout.getLocationInWindow(endLoc);


//        三、正式开始计算动画开始/结束的坐标
        //开始掉落的商品的起始点：商品起始点-父布局起始点+该商品图片的一半
        float startX = startLoc[0] - parentLocation[0] + iv.getWidth() / 2;
        float startY = startLoc[1] - parentLocation[1] + iv.getHeight() / 2;

        //商品掉落后的终点坐标：购物车起始点-父布局起始点+购物车图片的1/5
        float toX = endLoc[0] - parentLocation[0] + frameLayout.getWidth() / 5;
        float toY = endLoc[1] - parentLocation[1];

//        四、计算中间动画的插值坐标（贝塞尔曲线）（其实就是用贝塞尔曲线来完成起终点的过程）
        //开始绘制贝塞尔曲线
        Path path = new Path();
        //移动到起始点（贝塞尔曲线的起点）
        path.moveTo(startX, startY);
        //使用二次萨贝尔曲线：注意第一个起始坐标越大，贝塞尔曲线的横向距离就会越大，一般按照下面的式子取即可
        path.quadTo((startX + toX) / 2, startY, toX, toY);
        //mPathMeasure用来计算贝塞尔曲线的曲线长度和贝塞尔曲线中间插值的坐标，
        // 如果是true，path会形成一个闭环
        mPathMeasure = new PathMeasure(path, false);

        //属性动画实现（从0到贝塞尔曲线的长度之间进行插值计算，获取中间过程的距离值）
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
        valueAnimator.setDuration(1000);
        // 匀速线性插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 当插值计算进行时，获取中间的每个值，
                // 这里这个值是中间过程中的曲线长度（下面根据这个值来得出中间点的坐标值）
                float value = (Float) animation.getAnimatedValue();
                // 获取当前点坐标封装到mCurrentPosition
                // boolean getPosTan(float distance, float[] pos, float[] tan) ：
                // 传入一个距离distance(0<=distance<=getLength())，然后会计算当前距
                // 离的坐标点和切线，pos会自动填充上坐标，这个方法很重要。
                mPathMeasure.getPosTan(value, mCurrentPosition, null);//mCurrentPosition此时就是中间距离点的坐标值
                // 移动的商品图片（动画图片）的坐标设置为该中间点的坐标
                goods.setTranslationX(mCurrentPosition[0]);
                goods.setTranslationY(mCurrentPosition[1]);
            }
        });
        //      五、 开始执行动画
        valueAnimator.start();

//      六、动画结束后的处理
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            //当动画结束后：
            @Override
            public void onAnimationEnd(Animator animation) {
                rl.removeView(goods);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void showListModel() {
        adapterMusicFragment = new AdapterMusicFragment(getActivity(), musicList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapterMusicFragment);
        adapterMusicFragment.setOnItemClickListener(new AdapterMusicFragment.OnItemClickListener() {
            @Override
            public void onClick(int position, ImageView iv) {
                addAnimator(iv);
                listModel(position);
            }

            @Override
            public void onClickMenu(int position) {
                showPopFormBottom(musicList.get(position));
            }
        });
    }

    public void showPopFormBottom(final Music m) {
        MusicPopMenuWindow menuPopupWindow = new MusicPopMenuWindow(getActivity(), m, true);
//      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getActivity().getWindow().setAttributes(params);
        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha = 1f;
                getActivity().getWindow().setAttributes(params);
            }
        });
        menuPopupWindow.setOnUpdateListener(new MusicPopMenuWindow.OnUpdateListener() {
            @Override
            public void onDeleteUpdate() {

            }

            @Override
            public void onAddLoveUpdate() {
                addMyLove(m, getActivity());
            }
        });
    }

    public void addMyLove(Music m, final Context context) {
        if (MyUser.objectId.equals("")
                || MyUser.objectId == null) {
            MainActivity.setTab(0);
            Toasts.showShort(getActivity(), "请先登录！");
        } else {
            Music music = new Music();
            music.setUserId(MyUser.objectId);
            music.setSingerName(m.getSingerName());
            music.setSongName(m.getSongName());
            music.setUrl(m.getUrl());
            music.setDownUrl(m.getDownUrl());
            music.setAlbumpic_small(m.getAlbumpic_small());
            music.setAlbumpic_big(m.getAlbumpic_big());
            music.setFirstLetter(CommonUtil.StringToPinyinSpecial(m.getSongName()).toUpperCase().charAt(0) + "");
            music.save(new SaveListener<String>() {
                @Override
                public void done(String s, BmobException e) {
                    if (e == null) {
                        getMusicList();
                        Toasts.showShort(context, "添加成功！");
                    } else {
                        Toasts.showLong(context, "添加失败！");
                        Log.d("添加列表", e.toString());
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.music_re2:
                intent = new Intent(getActivity(), PlayActivity.class);
                break;
            case R.id.music_re3:
                intent = new Intent(getActivity(), MyLoveActivity.class);
                break;
        }
        startActivity(intent);
    }
}
