package com.zzh.assistant.activitys;

import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzh.assistant.R;
import com.zzh.assistant.entities.User;
import com.zzh.assistant.utils.DateUtil;
import com.zzh.assistant.utils.DialogProcess;
import com.zzh.assistant.utils.SPUtil;
import com.zzh.assistant.utils.ShowErrorSort;
import com.zzh.assistant.utils.Toasts;

import java.util.Date;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;

import static com.zzh.assistant.global.MyUser.objectId;
import static com.zzh.assistant.utils.DateUtil.YMD;


public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private EditText editUser, editPwd;
    private ImageView imgUserClear, imgPwdClear, imgEyes;
    private Boolean showPassword = true;
    private Button btnLogin;
    private TextView txtForgetPwd, txtRegister;
    private DialogProcess dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        editUser = (EditText) findViewById(R.id.edit_login_username);
        editPwd = (EditText) findViewById(R.id.edit_login_pwd);
        imgUserClear = (ImageView) findViewById(R.id.img_login_clear);
        imgPwdClear = (ImageView) findViewById(R.id.img_login_clear2);
        imgEyes = (ImageView) findViewById(R.id.img_login_pwd_state);
        btnLogin = (Button) findViewById(R.id.btn_login);
        txtForgetPwd = (TextView) findViewById(R.id.txt_login_forget_pwd);
        txtRegister = (TextView) findViewById(R.id.txt_login_register);
    }

    private void initData() {
        dialog = new DialogProcess(this, R.style.TransparentDialog);
        editUser.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
//        editPwd.setTransformationMethod(new PasswordCharSequenceStyle());
        imgUserClear.setVisibility(View.GONE);
        imgPwdClear.setVisibility(View.GONE);
        txtForgetPwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        txtForgetPwd.getPaint().setAntiAlias(true);//抗锯齿
        txtRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        txtRegister.getPaint().setAntiAlias(true);
    }

    private void initEvent() {
        imgUserClear.setOnClickListener(this);
        imgPwdClear.setOnClickListener(this);
        imgEyes.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        txtForgetPwd.setOnClickListener(this);
        txtRegister.setOnClickListener(this);
        editUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(editUser.getText().toString())) {
                    imgUserClear.setVisibility(View.GONE);
                } else {
                    imgUserClear.setVisibility(View.VISIBLE);
                }
            }
        });
        editPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(editPwd.getText().toString())) {
                    imgPwdClear.setVisibility(View.GONE);
                } else {
                    imgPwdClear.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_login_clear:
                editUser.setText("");
                break;
            case R.id.img_login_clear2:
                editPwd.setText("");
                break;
            case R.id.img_login_pwd_state:
                if (showPassword) {// 显示密码
                    imgEyes.setImageDrawable(getResources().getDrawable(R.drawable.login_eyesopen));
                    editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    editPwd.setSelection(editPwd.getText().toString().length());
                    showPassword = !showPassword;
                } else {// 隐藏密码
                    imgEyes.setImageDrawable(getResources().getDrawable(R.drawable.login_eyesclosed));
                    editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    editPwd.setSelection(editPwd.getText().toString().length());
                    showPassword = !showPassword;
                }
                break;
            case R.id.btn_login:
                login(editUser.getText().toString(), editPwd.getText().toString());
                break;
            case R.id.txt_login_forget_pwd:
                break;
            case R.id.txt_login_register:
                startActivityForResult(new Intent(this, RegisterActivity.class), 1);
                break;
        }
    }


    /**
     * 更新本地用户信息
     * 注意：需要先登录，否则会报9024错误
     */
    private void fetchUserInfo() {
        BmobUser.fetchUserJsonInfo(new FetchUserInfoListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    Log.d("fetchUserInfo", s);
                } else {
                    Log.d("fetchUserInfo", e.toString());
                }
            }
        });
    }

    //登录
    private void login(String user, String pwd) {
        dialog.show();
        BmobUser.loginByAccount(user, pwd, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (user != null) {
                    fetchUserInfo();
                    if (getIntent().getBooleanExtra("flag", false)) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    Toasts.showShort(LoginActivity.this, "登录成功");
                    finish();
                } else {
                    ShowErrorSort.show(e.getErrorCode(), LoginActivity.this);
                    Log.d("loginActivity", e.toString());
                }
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String phone = data.getStringExtra("phone");
            editUser.setText(phone);
            editUser.setFocusable(true);
            editUser.setSelection(phone.length());
            editPwd.setText(data.getStringExtra("pwd"));
        }
    }
    /**
     * 密码圆点改为*
     */
   /* public class PasswordCharSequenceStyle extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source;
            }

            public char charAt(int index) {
                return '*';
            }

            public int length() {
                return mSource.length();
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end);
            }
        }
    }*/
}
