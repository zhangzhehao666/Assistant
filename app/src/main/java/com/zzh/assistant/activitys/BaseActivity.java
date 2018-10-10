package com.zzh.assistant.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zzh.assistant.R;
import com.zzh.assistant.service.MusicPlayerService;
import com.zzh.assistant.utils.MusicUtil;

import cn.bmob.v3.Bmob;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "ed92638c616c9380c1070826f86dc941");
        Intent startIntent = new Intent(BaseActivity.this, MusicPlayerService.class);
        startService(startIntent);
        initTheme();
    }

    private void initTheme() {
        int themeId = MusicUtil.getTheme(BaseActivity.this);
        switch (themeId) {
            default:
            case 0:
                setTheme(R.style.BiLiPinkTheme);
                break;
            case 1:
                setTheme(R.style.ZhiHuBlueTheme);
                break;
            case 2:
                setTheme(R.style.KuAnGreenTheme);
                break;
            case 3:
                setTheme(R.style.CloudRedTheme);
                break;
            case 4:
                setTheme(R.style.TengLuoPurpleTheme);
                break;
            case 5:
                setTheme(R.style.SeaBlueTheme);
                break;
            case 6:
                setTheme(R.style.GrassGreenTheme);
                break;
            case 7:
                setTheme(R.style.CoffeeBrownTheme);
                break;
            case 8:
                setTheme(R.style.LemonOrangeTheme);
                break;
            case 9:
                setTheme(R.style.StartSkyGrayTheme);
                break;
            case 10:
                setTheme(R.style.NightModeTheme);
                break;
        }
    }
}
