package com.zzh.assistant.activitys;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zzh.assistant.R;
import com.zzh.assistant.entities.User;
import com.zzh.assistant.global.MyUser;
import com.zzh.assistant.utils.MethodUtil;
import com.zzh.assistant.utils.PermissionUtil;
import com.zzh.assistant.utils.Toasts;
import com.zzh.assistant.views.CircleImageView;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

import static com.zzh.assistant.global.MyUser.objectId;
import static com.zzh.assistant.utils.BitmapUtil.bitmapToString;
import static com.zzh.assistant.utils.BitmapUtil.stringToBitmap;

public class UserDetailActivity extends BaseActivity implements View.OnClickListener {
    private RelativeLayout reUsername, reHeaderImg, reUpdatePwd;
    private CircleImageView imgHeader;
    private TextView txtUserName;
    private String imageUri;
    private int select_image = 1001;
    private static final int CROP_REQUEST = 2;
    private String url;
    private Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userdetail);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.user_detail_toolbar);
        reUsername = (RelativeLayout) findViewById(R.id.user_detail_reUsername);
        reHeaderImg = (RelativeLayout) findViewById(R.id.user_detail_reHeaderImg);
        reUpdatePwd = (RelativeLayout) findViewById(R.id.user_detail_reUpdatePwd);
        imgHeader = (CircleImageView) findViewById(R.id.user_detail_header_img);
        txtUserName = (TextView) findViewById(R.id.user_detail_username);
    }

    private void initData() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/Assistant/image");
        if (!file.exists()) {
            file.mkdirs();
        }
        url = file.getPath() + "/image.png";
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            txtUserName.setText(user.getUsername());
            imgHeader.setImageBitmap(stringToBitmap(user.getHeaderImage()));
            getUerInfo();
        }
    }

    private void initEvent() {
        reUsername.setOnClickListener(this);
        reHeaderImg.setOnClickListener(this);
        reUpdatePwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_detail_reUsername:

                break;
            case R.id.user_detail_reHeaderImg:
                if (PermissionUtil.checkCameraPermission(UserDetailActivity.this)) {
                    Intent intent = new Intent(UserDetailActivity.this, SelectPictureActivity.class);
                    intent.putExtra(SelectPictureActivity.EXTRA_SHOW_CAMERA, true);
                    intent.putExtra(SelectPictureActivity.EXTRA_SELECT_COUNT, 1);
                    intent.putExtra(SelectPictureActivity.EXTRA_SELECT_MODE, SelectPictureActivity.MODE_SINGLE);
                    startActivityForResult(intent, select_image);
                }
                break;
            case R.id.user_detail_reUpdatePwd:

                break;
        }
    }

    private void getUerInfo() {
        BmobQuery<User> user = new BmobQuery<User>();
        user.getObject(objectId, new QueryListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    txtUserName.setText(user.getUsername());
                    imgHeader.setImageBitmap(stringToBitmap(user.getHeaderImage()));
                } else {
                    Log.d("getUerInfo", e.toString());
                }
            }
        });
    }


    //上传头像
    private void UpdateImgHeader(Bitmap bit) {
        User user = new User();
        user.setHeaderImage(bitmapToString(bit));
        user.update(objectId, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toasts.showShort(UserDetailActivity.this, "修改成功！");
                    User u = BmobUser.getCurrentUser(User.class);
                    Log.d("qqq", String.valueOf(u.getPoint()));
                } else {
                    Toasts.showShort(UserDetailActivity.this, "修改失败！");
                    Log.d("onActivityResult:失败", e.toString());
                }
            }
        });
        /*final BmobFile bmobFile = new BmobFile(new File(url));
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    User user = new User();
                    user.setImg(bmobFile);
                    user.update(objectId, new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Log.d("qqqq1", "111");
                            } else {
                                Log.d("qqqq2", e.toString());
                            }
                        }
                    });
                } else {
                    Log.d("qqqq3", e.toString());
                }
            }
        });*/
    }

    /**
     * 裁剪拍摄的照片
     *
     * @param photoPath
     */
    public void cutPhoto(String photoPath) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        File file = new File(photoPath);
        Uri photoUri = Uri.fromFile(file);
        //兼容7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        //解决华为手机裁剪为圆形
        if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG);
        intent.putExtra("outputX", 60);
        intent.putExtra("outputY", 60);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("noFaceDetection", false);
        intent.putExtra("output", Uri.fromFile(new File(url)));
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROP_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == select_image && resultCode == RESULT_OK) {
            List<String> listImagePath = data.getStringArrayListExtra(SelectPictureActivity.EXTRA_RESULT);
            StringBuilder sb = new StringBuilder();
            for (String imagePath : listImagePath) {
                sb.append(imagePath);
                sb.append("\n");
            }
            imageUri = listImagePath.get(0);
            cutPhoto(imageUri);
        } else if (resultCode == RESULT_OK && requestCode == CROP_REQUEST) {
            imgHeader.setImageBitmap(MethodUtil.getLoacalBitmap(url));
            UpdateImgHeader(MethodUtil.getLoacalBitmap(url));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(UserDetailActivity.this, SelectPictureActivity.class);
                    intent.putExtra(SelectPictureActivity.EXTRA_SHOW_CAMERA, true);
                    intent.putExtra(SelectPictureActivity.EXTRA_SELECT_COUNT, 1);
                    intent.putExtra(SelectPictureActivity.EXTRA_SELECT_MODE, SelectPictureActivity.MODE_SINGLE);
                    startActivityForResult(intent, select_image);
                } else {

                }
                return;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }
}
