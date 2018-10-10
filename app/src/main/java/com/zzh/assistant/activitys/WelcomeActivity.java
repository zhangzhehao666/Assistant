package com.zzh.assistant.activitys;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zzh.assistant.R;
import com.zzh.assistant.entities.User;
import com.zzh.assistant.utils.HttpUtil;
import com.zzh.assistant.utils.PermissionUtil;
import com.zzh.assistant.utils.ShowErrorSort;

import org.json.JSONObject;

import java.io.IOException;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.ValueEventListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zzh.assistant.global.MyUser.objectId;

public class WelcomeActivity extends AppCompatActivity {
    private ImageView imageView1, imageView2;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Bmob.initialize(this, "ed92638c616c9380c1070826f86dc941");
        imageView1 = (ImageView) findViewById(R.id.welcome_img1);
        imageView2 = (ImageView) findViewById(R.id.welcome_img2);
//        loadBingPic();
        Glide.with(WelcomeActivity.this).asGif().load(R.drawable.bg_header).into(imageView1);
        Glide.with(WelcomeActivity.this).asGif().load(R.drawable.welcome).into(imageView2);
        if (PermissionUtil.checkCameraPermission(WelcomeActivity.this)) {
            openActivity();
        }
    }

    private void loadBingPic() {
        HttpUtil.sendOkHttpRequest(HttpUtil.requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String bingPic = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WelcomeActivity.this).load(bingPic).into(imageView2);
                            Glide.with(WelcomeActivity.this).asGif().load(R.drawable.bg_header).into(imageView1);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void openActivity() {
        Handler handler = new Handler(Looper.myLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (intent == null) {
                    intent = new Intent(WelcomeActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 3000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openActivity();
                } else {

                }
                return;
        }
    }
}
