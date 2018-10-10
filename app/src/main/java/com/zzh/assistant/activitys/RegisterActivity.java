package com.zzh.assistant.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zzh.assistant.R;
import com.zzh.assistant.entities.User;
import com.zzh.assistant.utils.CommonUtil;
import com.zzh.assistant.utils.ShowErrorSort;
import com.zzh.assistant.utils.Toasts;


import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

import static com.zzh.assistant.utils.BitmapUtil.bitmapToString;


public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private EditText editPhone, editPwd, editName;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        editPhone = (EditText) findViewById(R.id.edit_register_phone);
        editPwd = (EditText) findViewById(R.id.edit_register_pwd);
        btnRegister = (Button) findViewById(R.id.btn_register);
        editName = (EditText) findViewById(R.id.edit_register_name);
    }

    private void initData() {

    }

    private void initEvent() {
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                if (CommonUtil.isPhone(editPhone.getText().toString())) {
                    register();
                } else {
                    Toasts.showShort(this, "手机号格式不正确");
                }
                break;
        }
    }

    private void register() {
        final User user = new User();
        user.setUsername(editName.getText().toString());
        user.setMobilePhoneNumber(editPhone.getText().toString());
        user.setPassword(editPwd.getText().toString());
        user.setPwd(editPwd.getText().toString());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_img);
        user.setHeaderImage(bitmapToString(bitmap));
        user.setLabel("无");
        user.setPoint("0");
        user.setLastSignTime("0");
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User s, BmobException e) {
                if (e == null) {
                    Toasts.showShort(RegisterActivity.this, "注册成功");
                    Intent intent = new Intent();
                    intent.putExtra("phone", editPhone.getText().toString());
                    intent.putExtra("pwd", editPwd.getText().toString());
                    setResult(RESULT_OK, intent);
                    BmobUser.logOut();
                    finish();
                } else {
                    ShowErrorSort.show(e.getErrorCode(), RegisterActivity.this);
                }
            }
        });
    }
}
