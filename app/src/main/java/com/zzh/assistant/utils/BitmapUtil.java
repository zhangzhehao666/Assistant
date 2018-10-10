package com.zzh.assistant.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BitmapUtil {
    public static String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);
    }

    public static Bitmap stringToBitmap(String string) {
        //将字符串转换成Bitmap类型
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * bitmap转为base64字符串
     *
     * @param bitmap
     * @return
     */
    public static String bmpToBase64Str(Bitmap bitmap) {
        String bmpStr = "";
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int quality = 100;
            while (baos.toByteArray().length / 1024f > 100) {
                quality = quality - 5;// 每次都减少5
                baos.reset();// 重置baos即清空baos
                if (quality <= 0) {
                    break;
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            byte[] datas = baos.toByteArray();
            bmpStr = Base64.encodeToString(datas, Base64.DEFAULT);
        }
        return bmpStr;
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;
        while (baos.toByteArray().length / 1024 > 100) {
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }
}
