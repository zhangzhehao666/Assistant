package com.zzh.assistant.activitys;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.zzh.assistant.R;
import com.zzh.assistant.utils.QRCodeUtil;
import com.zzh.assistant.utils.Toasts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class QRCodeActivity extends Activity {
    private EditText editText;
    private ImageView imageView;
    private Button button;
    private String path = Environment.getExternalStorageDirectory() + "/Assistant/qr_code/";
    private Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);
        setContentView(R.layout.activity_qrcode);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        editText = (EditText) findViewById(R.id.qrcode_edit);
        imageView = (ImageView) findViewById(R.id.qrcode_img);
        button = (Button) findViewById(R.id.qrcode_generation);
    }

    private void initData() {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void initEvent() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = path + System.currentTimeMillis() + ".jpg";
                QRCodeUtil.createQRImage(editText.getText().toString(), 500, 500,
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                        filePath);
                Toasts.showLong(QRCodeActivity.this, "保存路径：" + path);
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath));
                    imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmap);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
