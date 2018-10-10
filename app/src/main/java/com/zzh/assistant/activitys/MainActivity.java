package com.zzh.assistant.activitys;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nineoldandroids.view.ViewHelper;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.User;
import com.zzh.assistant.fragments.FunnyFragment;
import com.zzh.assistant.fragments.MusicFragment;
import com.zzh.assistant.fragments.NewsFragment;
import com.zzh.assistant.fragments.VideoFragment;
import com.zzh.assistant.global.MyUser;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.CommonDialog;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.DateUtil;
import com.zzh.assistant.utils.MusicUtil;
import com.zzh.assistant.utils.PermissionUtil;
import com.zzh.assistant.utils.Toasts;
import com.zzh.assistant.views.CircleImageView;

import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.Field;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

import static com.zzh.assistant.global.MyUser.objectId;
import static com.zzh.assistant.utils.BitmapUtil.stringToBitmap;
import static com.zzh.assistant.utils.DateUtil.HH;
import static com.zzh.assistant.utils.DateUtil.YMD;


public class MainActivity extends BaseActivity implements View.OnClickListener, DrawerLayout.DrawerListener {
    private static DrawerLayout drawerLayout;
    //    private ImageView imgMenu;
    private CircleImageView imgHeader;
    private NavigationView navigation;
    private TextView txtUserName, showPoint, txtPoint, txtLabel;
    private static FragmentTabHost tabHost;
    private LayoutInflater layoutInflater;
    private Class[] fragments = {FunnyFragment.class, NewsFragment.class, VideoFragment.class, MusicFragment.class};
    private int[] tabImages = {R.drawable.funny_choose, R.drawable.news_choose, R.drawable.video_choose, R.drawable.music_choose};
    private String[] tabTexts = {"笑话", "新闻", "视频", "音乐"};
    private ImageView imageView;
    private AnimationSet set;
    private Button btnSign;
    private boolean isOpen = false;
    private int point;
    BmobRealTimeData data = new BmobRealTimeData();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            objectId = user.getObjectId();
            getUserInfo();
//            Picasso.with(this).load(BitmapUtil.stringToBitmap(user.getHeaderImage())).transform(new CustomTransformer()).into(imgHeader);
        } else {
            imgHeader.setImageResource(R.drawable.default_img);
            txtUserName.setText("点击头像登录");
        }
    }

    private void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_main);
//        imgMenu = (ImageView) findViewById(R.id.img_main_menu);
        navigation = (NavigationView) findViewById(R.id.navigation_main_menu);
        View headerView = navigation.inflateHeaderView(R.layout.navigation_header);
        imgHeader = (CircleImageView) headerView.findViewById(R.id.img_navigation_header);
        txtUserName = (TextView) headerView.findViewById(R.id.txt_navigation_header);
        imageView = (ImageView) headerView.findViewById(R.id.navigation_img);
        showPoint = (TextView) headerView.findViewById(R.id.navigation_point);
        btnSign = (Button) headerView.findViewById(R.id.navigation_btn_sign);
        txtPoint = (TextView) headerView.findViewById(R.id.navigation_level_txt);
        txtLabel = (TextView) headerView.findViewById(R.id.navigation_label_txt);
        tabHost = (FragmentTabHost) findViewById(R.id.fragmentab_main);
    }

    private void initData() {
//        MusicUtil.delete(this);

        Intent startIntent = new Intent(MainActivity.this, MusicPlayerService.class);
        startService(startIntent);
        //自适应宽
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        ViewGroup.LayoutParams para = navigation.getLayoutParams();
        para.width = width / 3 * 2;
        para.height = height;
        navigation.setLayoutParams(para);

        Glide.with(this).load(R.drawable.bg_header).into(imageView);

        layoutInflater = LayoutInflater.from(this);
        tabHost.setup(this, getSupportFragmentManager(), R.id.frame_main);
        for (int i = 0; i < fragments.length; i++) {
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(tabTexts[i]).setIndicator(getTabItemView(i));
            tabHost.addTab(tabSpec, fragments[i], null);
            tabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
            if (i == 0) {
                tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.color.common_background);
            } else {
                tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.color.common_background);
            }
        }
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                for (int i = 0; i < fragments.length; i++) {
                    tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.color.common_background);
                    if (tabId.equals(tabTexts[0])) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    } else {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    }
                }
                tabHost.getCurrentTabView().setBackgroundResource(R.color.common_background);
            }
        });


        disableNavigationViewScrollbars(navigation);
        setDrawerLeftEdgeSize(this, drawerLayout, 0.2f);

        int left = showPoint.getLeft();
        int top = showPoint.getTop();
        // 创建平移和渐变的动画集合
        // 定义一个平移动画对象
        TranslateAnimation translate = new TranslateAnimation(left, left, top, top - 50);
        translate.setDuration(2000);
        //translate.setRepeatCount(1);

        // 渐变动画
        AlphaAnimation alpha = new AlphaAnimation(2, 0);
        alpha.setDuration(2000);
        alpha.setFillAfter(true);

        // 创建动画集合，将平移动画和渐变动画添加到集合中，一起start
        set = new AnimationSet(false);
        set.addAnimation(translate);
        set.addAnimation(alpha);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(broadcastReceiver, filter);

        //实时数据同步（暂无效果）
        /*data.start(new ValueEventListener() {
            @Override
            public void onConnectCompleted(Exception e) {
                if (data.isConnected()) {
                    // 监听表更新
                    data.subTableDelete("_User");
                    // 监听行更新
                    data.subRowUpdate("_User", objectId);
                    // 监听行删除
                    data.subRowDelete("_User", objectId);
                }
            }

            @Override
            public void onDataChange(JSONObject j) {
                Log.d("bmob", "(" + j.optString("action") + ")" + "数据：" + data);
                if (BmobRealTimeData.ACTION_UPDATETABLE.equals(j.optString("action"))) {
                    JSONObject data = j.optJSONObject("data");
                }
            }
        });*/
    }

    private void initEvent() {
        drawerLayout.addDrawerListener(this);
        drawerLayout.setOnClickListener(this);
        btnSign.setOnClickListener(this);
        imgHeader.setOnClickListener(this);
//        imgMenu.setOnClickListener(this);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_information:
                        startActivity(new Intent(MainActivity.this, UserDetailActivity.class));
                        break;
                    case R.id.menu_logout:
                        if (MyUser.objectId.equals("")){
                            Toasts.showShort(MainActivity.this,"请先登录！");
                        }else {
                            new CommonDialog(MainActivity.this, R.style.dialog,
                                    "确定退出当前帐号吗？", new CommonDialog.OnCloseListener() {
                                @Override
                                public void onClick(Dialog dialog, boolean confirm) {
                                    if (confirm) {
                                        BmobUser.logOut();
                                        dialog.dismiss();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        intent.putExtra("flag", true);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }).setTitle("提示").setNegativeButton("取消").setPositiveButton("确定").show();
                        }
                        break;
                    case R.id.menu_theme:
                        startActivity(new Intent(MainActivity.this, ThemeActivity.class));
                        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom);
                        break;
                    case R.id.menu_tools:
                        startActivity(new Intent(MainActivity.this, ToolsActivity.class));
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.img_main_menu:
//                drawerLayout.openDrawer(Gravity.LEFT);
//                break;
            case R.id.navigation_btn_sign:
                sign();
                break;
            case R.id.img_navigation_header:
                User u = BmobUser.getCurrentUser(User.class);
                if (u == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtra("flag", false);
                    startActivity(intent);
                }
                break;
        }
    }

    //解决大屏幕滑动
    private void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            Field leftDraggerField =
                    drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (displaySize.x *
                    displayWidthPercentage)));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.view_tab_item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_view_tab_item);
        imageView.setImageResource(tabImages[index]);
        TextView textView = (TextView) view.findViewById(R.id.tv_view_tab_item);
        textView.setText(tabTexts[index]);
        return view;
    }

    public static void setTab(int tab) {
        tabHost.setCurrentTab(tab);
        drawerLayout.openDrawer(GravityCompat.START);
    }


    //去除滚动条
    private void disableNavigationViewScrollbars(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        View mContent = drawerLayout.getChildAt(0);
        View mMenu = drawerView;
        float scale = 1 - slideOffset;
        float rightScale = 0.8f + scale * 0.2f;
        if (drawerView.getTag().equals("start")) {
            float leftScale = 1 - 0.3f * scale;
            //设置左边菜单滑动后的占据屏幕大小
            ViewHelper.setScaleX(mMenu, leftScale);
            ViewHelper.setScaleY(mMenu, leftScale);
            //设置菜单透明度
            ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
            //设置内容界面水平和垂直方向偏转量
            //在滑动时内容界面的宽度为 屏幕宽度减去菜单界面所占宽度
            ViewHelper.setTranslationX(mContent,
                    mMenu.getMeasuredWidth() * (1 - scale));
            mContent.invalidate();
            //设置右边菜单滑动后的占据屏幕大小
            ViewHelper.setScaleX(mContent, rightScale);
            ViewHelper.setScaleY(mContent, rightScale);
        }
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        isOpen = true;
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        isOpen = false;
    }


    @Override
    public void onDrawerStateChanged(int newState) {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void sign() {
        if (btnSign.getText().toString().equals("马上签到")) {
            User user = new User();
            user.setLastSignTime(CommonUtil.getNetTime(YMD));
            user.setPoint(String.valueOf(point + 5));
            user.update(objectId, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        showPoint.setText("积分 +5");
                        showPoint.startAnimation(set);
                        showPoint.setVisibility(View.GONE);
                        btnSign.setText("已签到");
                        getUserInfo();
                    } else {
                        if (!MyUser.objectId.equals("")) {
                            Toasts.showShort(MainActivity.this, "签到失败！");
                        } else {
                            Toasts.showShort(MainActivity.this, "请先登录！");
                        }
                        Log.d("sign", "失败：" + e.getMessage() + "," + e.getErrorCode());
                    }
                }
            });
        } else {
            Toasts.showShort(this, "今日已签到！");
        }
    }

    private void getUserInfo() {
        BmobQuery<User> user = new BmobQuery<User>();
        user.getObject(objectId, new QueryListener<User>() {
            @Override
            public void done(User u, BmobException e) {
                if (e == null) {
                    txtUserName.setText(u.getUsername());
                    imgHeader.setImageBitmap(stringToBitmap(u.getHeaderImage()));
                    txtPoint.setText(u.getPoint());
                    point = Integer.parseInt(u.getPoint());
                    txtLabel.setText(u.getLabel());
                    String time = "";
                    if (u.getLastSignTime().equals("0")) {
                        btnSign.setText("马上签到");
                    } else {
                        time = DateUtil.format(u.getLastSignTime(), YMD);
                        if (time.equals(CommonUtil.getNetTime(YMD))) {
                            btnSign.setText("已签到");
                        } else {
                            btnSign.setText("马上签到");
                        }
                    }
                } else {
                    Log.d("getUserInfo", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isOpen) {
                drawerLayout.closeDrawers();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
                if (CommonUtil.getNetTime(HH).equals("00")) {
                    btnSign.setText("马上签到");
                }
            }
        }
    };
}
