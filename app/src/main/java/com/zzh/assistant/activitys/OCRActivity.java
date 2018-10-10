package com.zzh.assistant.activitys;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzh.assistant.R;
import com.zzh.assistant.ocr.AuthService;
import com.zzh.assistant.ocr.Base64Util;
import com.zzh.assistant.ocr.FileUtil;
import com.zzh.assistant.ocr.HttpUtil;
import com.zzh.assistant.utils.DialogProcess;
import com.zzh.assistant.utils.PermissionUtil;
import com.zzh.assistant.utils.Toasts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class OCRActivity extends Activity implements View.OnClickListener {
    //相机请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    //剪裁请求码
    private static final int CROP_REQUEST_CODE = 3;
    //调用照相机返回图片文件
    private File tempFile;
    private ImageView ocrImg, back;
    private TextView txtResult;
    private Button take;
    private String path = Environment.getExternalStorageDirectory() + "/Assistant/ocr";
    private String s = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        ocrImg = (ImageView) findViewById(R.id.ocr_img);
        txtResult = (TextView) findViewById(R.id.ocr_result);
        take = (Button) findViewById(R.id.ocr_take);
        back = (ImageView) findViewById(R.id.ocr_back);
    }

    private void initData() {
        txtResult.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initEvent() {
        take.setOnClickListener(this);
        back.setOnClickListener(this);
        txtResult.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager copy = (ClipboardManager) OCRActivity.this
                        .getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, txtResult.getText().toString());
                copy.setPrimaryClip(clipData);
                Toasts.showShort(OCRActivity.this, "已复制到粘贴板");
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ocr_take:
                if (PermissionUtil.checkCameraPermission(OCRActivity.this)) {
                    txtResult.setText("");
                    getPicFromCamera();
                }
                break;
            case R.id.ocr_back:
                finish();
                break;
        }
    }

    private void getPicFromCamera() {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        //用于保存调用相机拍照后所生成的文件
        tempFile = new File(file.getPath(), "ocr.jpg");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(OCRActivity.this, getPackageName() + ".provider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        }
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPicFromCamera();
                } else {

                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            // 调用相机后返回
            case CAMERA_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
//                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        Uri contentUri = FileProvider.getUriForFile(OCRActivity.this, getPackageName() + ".provider", tempFile);
//                        cropPhoto(contentUri);//裁剪图片
//
//                    } else {
//                        cropPhoto(Uri.fromFile(tempFile));//裁剪图片
//                    }
                    Bitmap bmp = BitmapFactory.decodeFile(tempFile.getPath());
                    ocrImg.setImageBitmap(bmp);
                    txtResult.scrollTo(0, 0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtResult.setText("正在解析中，请稍等。。。");
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getOCR(tempFile.getPath());
                        }
                    }).start();
                }
                break;
            //调用剪裁后返回
            case CROP_REQUEST_CODE:
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Bitmap image = bundle.getParcelable("data");
                    ocrImg.setImageBitmap(image);
                    final String path = saveImage("ocr_crop", image);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getOCR(path);
                        }
                    }).start();
                }
                break;
        }
    }

    /**
     * 裁剪图片
     */
    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        //解决华为手机裁剪为圆形
        if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 800);
            intent.putExtra("aspectY", 500);
        }
        intent.putExtra("outputX", 360);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }

    /**
     * 保存图片到本地
     *
     * @param name
     * @param bmp
     * @return
     */
    public String saveImage(String name, Bitmap bmp) {
        File appDir = new File(path);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = name + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getOCR(String filePath) {
        s = "";
        // 通用识别url
        String otherHost = "https://aip.baidubce.com/rest/2.0/ocr/v1/general";
        try {
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String params = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(imgStr, "UTF-8");
            /**
             * 线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
             */
            String accessToken = AuthService.getAuth();
            String result = HttpUtil.post(otherHost, accessToken, params);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray array = jsonObject.getJSONArray("words_result");
            final List<String> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject jo = array.getJSONObject(i);
                list.add(jo.getString("words"));
            }
            for (int i = 0; i < list.size(); i++) {
                s += list.get(i) + "\n";
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtResult.setText(s);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
