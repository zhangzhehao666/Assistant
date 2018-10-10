package com.zzh.assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodUtil {
    public static String path2Base64intauto(String url, int wid, int hei) {//自定义压缩比例
        Bitmap bitmap = decodeBitmap(url, wid, hei);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();
        return Base64.encodeToString(datas, Base64.DEFAULT);
    }

    public static Bitmap decodeBitmap(String path, int displayWidth, int displayHeight) {


        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inJustDecodeBounds = true;
        // op.inJustDecodeBounds = true;表示我们只读取Bitmap的宽高等信息，不读取像素。
        Bitmap bmp = BitmapFactory.decodeFile(path, op); // 获取尺寸信息
        // op.outWidth表示的是图像真实的宽度
        // op.inSamplySize 表示的是缩小的比例
        // op.inSamplySize = 4,表示缩小1/4的宽和高，1/16的像素，android认为设置为2是最快的。
        // 获取比例大小
        int wRatio = (int) Math.ceil(op.outWidth / (float) displayWidth);
        int hRatio = (int) Math.ceil(op.outHeight / (float) displayHeight);
        // 如果超出指定大小，则缩小相应的比例
        if (wRatio > 1 && hRatio > 1) {
            if (wRatio > hRatio) {
                // 如果太宽，我们就缩小宽度到需要的大小，注意，高度就会变得更加的小。
                op.inSampleSize = wRatio;
            } else {
                op.inSampleSize = hRatio;
            }
        }
        op.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, op);


        // 从原Bitmap创建一个给定宽高的Bitmap
        if (bmp != null) {
            Float hwb = Float.valueOf(bmp.getHeight()) / Float.valueOf(bmp.getWidth());
            if (wRatio > hRatio) {
                return Bitmap.createScaledBitmap(bmp, (int) (displayWidth / hwb), displayWidth, true);
            } else {
                return Bitmap.createScaledBitmap(bmp, displayWidth, (int) (displayWidth * hwb), true);
            }
        } else {
            return null;
        }


    }

    public static String htmlfit(String wvdata) {
        String customHtml = replace("&lt;", "<", wvdata);
        customHtml = replace("&gt;", ">", customHtml);
        customHtml = replace("&quot;", "\"", customHtml);
        Document doc_Dis = Jsoup.parse(customHtml);
        Elements ele_Img = doc_Dis.getElementsByTag("img");
        if (ele_Img.size() != 0) {
            for (Element e_Img : ele_Img) {
                e_Img.attr("style", "max-width:100%;height:auto");
            }
        }
        String newHtmlContent = doc_Dis.toString();
        return newHtmlContent;
    }

    public static String htmlfit2(String wvdata) {
        String customHtml = replace("&lt;", "<", wvdata);
        customHtml = replace("&gt;", ">", customHtml);
        customHtml = replace("&quot;", "\"", customHtml);
//        Document doc_Dis = Jsoup.parse(customHtml);
//        Elements ele_Img = doc_Dis.getElementsByTag("img");
//        if (ele_Img.size() != 0) {
//            for (Element e_Img : ele_Img) {
//                e_Img.attr("style", "max-width:100%;height:auto");
//            }
//        }
//        String newHtmlContent = doc_Dis.toString();
        return customHtml;
    }

    public static String replace(String from, String to, String source) {
        if (source == null || from == null || to == null)
            return null;
        StringBuffer bf = new StringBuffer("");
        int index = -1;
        while ((index = source.indexOf(from)) != -1) {
            bf.append(source.substring(0, index) + to);
            source = source.substring(index + from.length());
            index = source.indexOf(from);
        }
        bf.append(source);
        return bf.toString();
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {
        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 计算listview的高度
     *
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 0);
        listView.setLayoutParams(params);
    }

    /**
     * 计算View高度
     *
     * @param view
     */
    public static void measureView(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int height;
        int tempHeigth = p.height;
        if (tempHeigth > 0) {
            height = View.MeasureSpec.makeMeasureSpec(tempHeigth, View.MeasureSpec.EXACTLY);
        } else {
            height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        view.measure(width, height);
    }

    /**
     * 判断是否为手机号
     *
     * @param str
     * @return
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][0-9][0-9]{9}$");
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 跳转页面
     *
     * @param context
     */
    public static void startActivity(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }

    /**
     * 对象转为Base64位字符串
     *
     * @param object
     * @return
     */
    public static String object2Base64Str(Object object) {
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
                if (baos != null) {
                    baos.close();
                }
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
     * Base64位字符串2对象
     *
     * @param base64Str
     * @return
     */
    public static Object base64Str2Object(String base64Str) {
        Object object;
        byte[] buffer = Base64.decode(base64Str, Base64.DEFAULT);
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
            object = ois.readObject();
            return object;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bais != null) {
                    bais.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getJsonObject(Object object) {
        if (object == null) {
            return "";
        } else if (object instanceof String) {
            return object.toString();
        }
        return "";
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
