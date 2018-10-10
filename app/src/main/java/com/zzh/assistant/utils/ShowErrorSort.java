package com.zzh.assistant.utils;

import android.content.Context;

public class ShowErrorSort {
    public static void show(int errorCode, Context context) {
        String msg = "";
        switch (errorCode) {
            case 101:
                msg = "手机号或密码不正确";
                break;
            case 9018:
                msg = "手机号和密码不能为空";
                break;
            case 202:
                msg = "用户名已存在";
                break;
            case 9010:
                msg = "网络超时";
                break;
            case 9016:
                msg = "请检查您的网络";
                break;
            case 210:
                msg = "旧密码不正确";
                break;
            case 9015:
                msg = "连接超时，请检查网络";
                break;
        }
        if (msg.equals("")) {
            Toasts.showShort(context, errorCode + "");
        } else {
            Toasts.showShort(context, msg);
        }
    }
}
