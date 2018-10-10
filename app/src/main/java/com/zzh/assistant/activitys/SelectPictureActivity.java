package com.zzh.assistant.activitys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.zzh.assistant.R;
import com.zzh.assistant.adapters.AdapterFolder;
import com.zzh.assistant.adapters.AdapterImageGrid;
import com.zzh.assistant.entities.EntityFolder;
import com.zzh.assistant.entities.EntityImage;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectPictureActivity extends FragmentActivity {
    /**
     * 最大图片选择次数，int类型，默认9
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 图片选择模式，默认多选
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * 是否显示相机，默认显示
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * 默认选择集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";
    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100;

    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;

    // 文件夹数据
    private ArrayList<EntityFolder> mResultFolder = new ArrayList<EntityFolder>();

    // 图片Grid
    private GridView mGridView;

    private AdapterImageGrid mImageAdapter;
    private AdapterFolder mFolderAdapter;

    private ListPopupWindow mFolderPopupWindow;

    // 时间线
    private TextView mTimeLineText;
    // 类别
    private TextView mCategoryText;
    // 预览按钮
    private Button mPreviewBtn;
    // 底部View
    private View mPopupAnchorView;

    private boolean hasFolderGened = false;
    private boolean mIsShowCamera = false;

    private int mGridWidth, mGridHeight;

    private File mTmpFile;

    private ArrayList<String> resultList = new ArrayList<String>();
    private TextView tvSubmit;
    private int mDefaultCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);

        Intent intent = getIntent();
        mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, 9);
        final int mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        mIsShowCamera = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        if (mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
        }

        //返回按钮
        findViewById(R.id.iv_activity_choose_picture_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // 完成按钮
        tvSubmit = (TextView) findViewById(R.id.tv_activity_choose_picture_submit);
        tvSubmit.setVisibility(View.GONE);
        if (resultList == null || resultList.size() <= 0) {
            tvSubmit.setText("完成");
            tvSubmit.setEnabled(false);
        } else {
            tvSubmit.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
            tvSubmit.setEnabled(true);
        }
        tvSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultList != null && resultList.size() > 0) {
                    // 返回已选择的图片数据
                    Intent data = new Intent();
                    data.putStringArrayListExtra(EXTRA_RESULT, resultList);
                    setResult(RESULT_OK, data);

                    finish();
                }
            }
        });


        mImageAdapter = new AdapterImageGrid(this, mIsShowCamera);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mode == MODE_MULTI);

        mPopupAnchorView = findViewById(R.id.footer);

        mTimeLineText = (TextView) findViewById(R.id.timeline_area);
        // 初始化，先隐藏当前timeline
        mTimeLineText.setVisibility(View.GONE);

        mCategoryText = (TextView) findViewById(R.id.category_btn);
        // 初始化，加载所有图片
        mCategoryText.setText("所有照片");
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mFolderPopupWindow == null) {
                    createPopupFolderList(mGridWidth, mGridHeight);
                }

                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();
                    int index = mFolderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        mPreviewBtn = (Button) findViewById(R.id.preview);
        // 初始化，按钮状态初始化
        if (resultList == null || resultList.size() <= 0) {
            mPreviewBtn.setText("预览");
            mPreviewBtn.setEnabled(false);
        }
        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 预览
            }
        });

        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {

                final Picasso picasso = Picasso.with(SelectPictureActivity.this);
                if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_TOUCH_SCROLL) {
                    picasso.resumeTag(SelectPictureActivity.this);
                } else {
                    picasso.pauseTag(SelectPictureActivity.this);
                }

                if (state == SCROLL_STATE_IDLE) {
                    // 停止滑动，日期指示器消失
                    mTimeLineText.setVisibility(View.GONE);
                } else if (state == SCROLL_STATE_FLING) {
                    mTimeLineText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mTimeLineText.getVisibility() == View.VISIBLE) {
                    int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1 : firstVisibleItem + 1;
                    EntityImage image = (EntityImage) view.getAdapter().getItem(index);
                    if (image != null) {
                        mTimeLineText.setText(CommonUtil.formatPhotoDate(image.getPathOriginal()));
                    }
                }
            }
        });
        mGridView.setAdapter(mImageAdapter);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int width = mGridView.getWidth();
                final int height = mGridView.getHeight();

                mGridWidth = width;
                mGridHeight = height;

                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = width / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                mImageAdapter.setItemSize(columnWidth);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mImageAdapter.isShowCamera()) {
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if (i == 0) {
                        showCameraAction();
                    } else {
                        // 正常操作
                        EntityImage image = (EntityImage) adapterView.getAdapter().getItem(i);
                        selectImageFromGrid(image, mode);
                    }
                } else {
                    // 正常操作
                    EntityImage image = (EntityImage) adapterView.getAdapter().getItem(i);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        mFolderAdapter = new AdapterFolder(SelectPictureActivity.this);

        this.getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    onCameraShot(mTmpFile);
                }
            }
        } else {
            if (mTmpFile != null && mTmpFile.exists()) {
                mTmpFile.delete();
            }
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList(int width, int height) {
        mFolderPopupWindow = new ListPopupWindow(SelectPictureActivity.this);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height * 5 / 8);
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mFolderAdapter.setSelectIndex(i);

                final int index = i;
                final AdapterView v = adapterView;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            SelectPictureActivity.this.getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText("所有照片");
                            if (mIsShowCamera) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            EntityFolder folder = (EntityFolder) v.getAdapter().getItem(index);
                            if (null != folder) {
                                mImageAdapter.setData(folder.images);
                                mCategoryText.setText(folder.name);
                                // 设定默认选择
                                if (resultList != null && resultList.size() > 0) {
                                    //mImageAdapter.setDefaultSelected(resultList);
                                }
                            }
                            mImageAdapter.setShowCamera(false);
                        }

                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);

            }
        });
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = FileUtil.createTmpFile(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", mTmpFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            } else {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            }
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "没有系统相机", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(EntityImage image, int mode) {
        if (image != null) {
            // 多选模式
            if (mode == MODE_MULTI) {
                if (resultList.contains(image.getPathOriginal())) {
                    resultList.remove(image.getPathOriginal());
                    if (resultList.size() != 0) {
                        mPreviewBtn.setEnabled(true);
                        mPreviewBtn.setText("预览" + "(" + resultList.size() + ")");
                    } else {
                        mPreviewBtn.setEnabled(false);
                        mPreviewBtn.setText("预览");
                    }
                    onImageUnselected(image.getPathOriginal());
                } else {
                    // 判断选择数量问题
                    if (mDefaultCount == resultList.size()) {
                        //Toast.makeText(getActivity(), R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    resultList.add(image.getPathOriginal());
                    mPreviewBtn.setEnabled(true);
                    mPreviewBtn.setText("预览" + "(" + resultList.size() + ")");
                    onImageSelected(image.getPathOriginal());
                }
                mImageAdapter.select(image);
            } else if (mode == MODE_SINGLE) {
                onSingleImageSelected(image.getPathOriginal());
            }
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(SelectPictureActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        null, null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(SelectPictureActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<EntityImage> images = new ArrayList<EntityImage>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        EntityImage image = new EntityImage();
                        image.setPathOriginal(path);
                        image.setName(name);
                        image.setTime(dateTime);
                        images.add(image);
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            EntityFolder folder = new EntityFolder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!mResultFolder.contains(folder)) {
                                List<EntityImage> imageList = new ArrayList<EntityImage>();
                                imageList.add(image);
                                folder.images = imageList;
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                                EntityFolder f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.images.add(image);
                            }
                        }

                    } while (data.moveToNext());

                    mImageAdapter.setData(images);

                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        //mImageAdapter.setDefaultSelected(resultList);
                    }

                    mFolderAdapter.setData(mResultFolder);
                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        // 有图片之后，改变按钮状态
        if (resultList.size() > 0) {
            tvSubmit.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
            if (!tvSubmit.isEnabled()) {
                tvSubmit.setEnabled(true);
            }
        }
    }

    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
            tvSubmit.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
        } else {
            tvSubmit.setText("完成(" + resultList.size() + "/" + mDefaultCount + ")");
        }
        // 当为选择图片时候的状态
        if (resultList.size() == 0) {
            tvSubmit.setText("完成");
            tvSubmit.setEnabled(false);
        }
    }

    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            Intent data = new Intent();
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
