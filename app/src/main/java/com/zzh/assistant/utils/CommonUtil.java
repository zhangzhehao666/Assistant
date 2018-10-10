package com.zzh.assistant.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.github.promeg.pinyinhelper.Pinyin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zzh.assistant.utils.DateUtil.YMD;


public class CommonUtil {
    //设置miui黑色顶栏图标
    public static boolean MIUISetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    public static Context sContext = null;

    /**
     * 判断是否为手机号
     *
     * @param phone 手机号
     * @return true
     */
    public static boolean isPhone(String phone) {
        Pattern p;
        Matcher m;
        boolean b;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$");
        m = p.matcher(phone);
        b = m.matches();
        return b;
    }

    /**
     * 判断网络是否连接
     *
     * @return
     */
    public static boolean isNet() {
        ConnectivityManager connectivity = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi() {
        ConnectivityManager cm = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 打开网络设置界面
     */
    public static void openNetSet(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) sContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 对象转为Base64位字符串
     *
     * @param object Object
     * @return base64
     */
    public static String objectToBase64(Object object) {
        String userBase64;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            userBase64 = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
            return userBase64;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
                if (oos != null) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Base64位字符串转为对象
     *
     * @param base64Str 64位字符串
     * @return Object
     */
    public static Object base64ToObject(String base64Str) {
        Object object;
        byte[] buffer = Base64.decode(base64Str, Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            object = ois.readObject();
            return object;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bais.close();
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Map 转 JsonStr
     *
     * @param map Map
     * @return JSON字符串
     */
    public static String mapToJsonStr(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "null";
        }
        String jsonStr = "{";
        Set<?> keySet = map.keySet();
        for (Object key : keySet) {
            jsonStr += "\"" + key + "\":\"" + map.get(key) + "\",";
        }
        jsonStr = jsonStr.substring(1, jsonStr.length() - 2);
        jsonStr += "}";
        return jsonStr;
    }

    /**
     * JsonStr 转 Map
     *
     * @param jsonStr JSON字符串
     * @return Map
     */
    public static Map jsonStrToMap(String jsonStr) {
        String sb = jsonStr.substring(1, jsonStr.length() - 1);
        String[] name = sb.split("\\\",\\\"");
        String[] nn;
        Map<String, String> map = new HashMap<>();
        for (String aName : name) {
            nn = aName.split("\\\":\\\"");
            map.put(nn[0], nn[1]);
        }
        return map;
    }

    /**
     * 获取非空Str
     *
     * @param tagStr     目标Str
     * @param defaultStr 默认Str
     * @return ResultStr
     */
    public static String setNoNullStr(String tagStr, String defaultStr) {
        if (!TextUtils.isEmpty(tagStr)) {
            return tagStr;
        }
        return defaultStr;
    }

    /**
     * 去除空字符
     *
     * @param str old
     * @return new
     */
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     * 获取包信息
     *
     * @return PackageInfo
     */
    public static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
    }

    //把屏幕分辨率dp转成px
    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    //获取网络视频的第一帧
    public static Bitmap getVideoThumbnail(String url) {
        Bitmap bitmap = null;
        //MediaMetadataRetriever 是android中定义好的一个类，提供了统一
        //的接口，用于从输入的媒体文件中取得帧和元数据；
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据文件路径获取缩略图
            retriever.setDataSource(url, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    //汉字转拼音
    public static String stringToPinyin(String s) {
        String pinyin = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            pinyin += Pinyin.toPinyin(c).toLowerCase();
        }
        return pinyin;
    }


    /**
     * 让RecyclerView滚动到指定位置
     */
    public static void scrollToPosition(RecyclerView recyclerView, int lastOffset, int lastPosition) {
        if (recyclerView.getLayoutManager() != null && lastPosition >= 0) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastPosition, lastOffset);
        }
    }

    //获取宽高
    public static int getWidth(Context context) {
        int width;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        width = dm.widthPixels;
        return width;
    }

    public static int getHeight(Context context) {
        int height;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        height = dm.heightPixels;
        return height;
    }

    public static String getNetTime(String type) {
        String time = "";
        final Calendar calendar = Calendar.getInstance();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://www.baidu.com");
                    URLConnection uc = url.openConnection();
                    uc.connect();
                    long ld = uc.getDate();
                    calendar.setTimeInMillis(ld);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        time = DateUtil.format(calendar.getTime(), type);
        return time;
    }

    public static String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static String formatPhotoDate(long time) {
        return timeFormat(time, "yyyy-MM-dd");
    }

    public static String formatPhotoDate(String path) {
        File file = new File(path);
        if (file.exists()) {
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "1970-01-01";
    }

    public static String StringToPinyinSpecial(String input) {
        if (input == null) {
            return null;
        }
        String result = null;
        for (int i = 0; i < input.length(); i++) {
            //是否在汉字范围内
            if (input.charAt(i) >= 0x4e00 && input.charAt(i) <= 0x9fa5) {
                result += Pinyin.toPinyin(input.charAt(i));
            } else {
                result += input.charAt(i);
            }
        }
        if (result.length() > 4) {
            result = result.substring(4, result.length());
        }
        //如果首字母不在[a,z]和[A,Z]内则首字母改为‘#’
        if (!(result.toUpperCase().charAt(0) >= 'A' && result.toUpperCase().charAt(0) <= 'Z')) {
            StringBuilder builder = new StringBuilder(result);
            builder.replace(0, 1, "#");
            result = builder.toString();
        }
        return result;
    }

    /**
     * 动态获取当前主题中的自定义颜色属性值
     *
     * @param attr         e.g R.attr.colorAccent
     * @param defaultColor 默认颜色值
     */
    public static int getAttrColorValue(int attr, int defaultColor, Context context) {

        int[] attrsArray = {attr};
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        int value = typedArray.getColor(0, defaultColor);
        typedArray.recycle();
        return value;
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
//        String strContent = strcontent + "\r\n";
        File f = new File(filePath);
        if (!f.exists()) {
            f.mkdirs();
        }
        try {
            File file = new File(strFilePath);
            if (file.exists()) {
                file.delete();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strcontent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String replaceTitle(String name) {
        if (name.contains("-")) {
            name = name.trim().substring(0, name.indexOf("-"));
        } else if (name.contains("－")) {
            name = name.trim().substring(0, name.indexOf("－"));
        } else {
            Log.d("replaceTitle:无法拆分", name);
        }
        String title = name.replaceAll("[^\u4E00-\u9FA5]", "");
        return title;
    }
}
