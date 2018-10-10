package com.zzh.assistant.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {
    public String name;//名称
    public String dir;//目录
    public String path;//路径
    public String urlStr;//连接
    public long size;//大小

    public static final long B = 1;
    public static final long KB = B * 1024;
    public static final long MB = KB * 1024;
    public static final long GB = MB * 1024;

    public FileUtil() {

    }

    public FileUtil(String name, String dir) {
        this.name = name;
        this.dir = dir;
        this.path = dir + name;
    }

    public FileUtil(String path) {
        this.path = path;
    }

    public FileUtil(String name, String dir, String urlStr) {
        this.name = name;
        this.dir = dir;
        this.urlStr = urlStr;
    }

    /**
     * 创建文件
     *
     * @return
     */
    public File createFile() {
        File file = new File(dir, name);
        createDir();
        if (!file.exists()) {
            try {
                file.createNewFile();
                return file;
            } catch (IOException e) {
                Log.e("UtilFile error log:", e.getStackTrace().toString());
            }
        }
        return file;
    }

    /**
     * 创建文件目录
     */
    public void createDir() {
        File file = new File(dir);
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                Log.e("UtilFile error log:", e.getStackTrace().toString());
            }
        }
    }

    /**
     * 判断文件是否存在
     *
     * @return
     */
    public boolean isExist() {
        File file = new File(dir, name);
        return file.exists();
    }

    /**
     * 从url写入到本地文件
     */
    public void writeFromUrl() {
        final File file = createFile();
        if (file != null) {
            new Thread() {
                @Override
                public void run() {
                    HttpURLConnection urlConn = null;
                    InputStream is = null;
                    OutputStream os = null;
                    try {
                        URL url = new URL(urlStr);
                        urlConn = (HttpURLConnection) url.openConnection();
                        is = urlConn.getInputStream();
                        os = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        while (is.read(buffer) != -1) {
                            os.write(buffer);
                        }
                        os.flush();
                    } catch (MalformedURLException e) {
                        Log.e("UtilFile error log:", e.getStackTrace().toString());
                    } catch (FileNotFoundException e) {
                        Log.e("UtilFile error log:", e.getStackTrace().toString());
                    } catch (IOException e) {
                        Log.e("UtilFile error log:", e.getStackTrace().toString());
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
        } else {
            Log.e("UtilFile error log:", "file is null");
        }
    }

    /**
     * 格式化文件大小 带有单位
     *
     * @return
     */
    public static String formatFileSize(long size) {
        StringBuilder sb = new StringBuilder();
        String u = null;
        double tmpSize = 0;
        if (size < KB) {
            sb.append(size).append("B");
            return sb.toString();
        } else if (size < MB) {
            tmpSize = getSize(size, KB);
            u = "KB";
        } else if (size < GB) {
            tmpSize = getSize(size, MB);
            u = "MB";
        } else {
            tmpSize = getSize(size, GB);
            u = "GB";
        }
        return sb.append(twoDot(tmpSize)).append(u).toString();
    }

    private static double getSize(long size, long u) {
        return (double) size / (double) u;
    }

    /**
     * 保留两位小数
     *
     * @param d
     * @return
     */
    private static String twoDot(double d) {
        return String.format("%.2f", d);
    }

    /**
     * SD卡挂载且可用
     *
     * @return
     */
    public static boolean isSDCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static File createTmpFile(Context context) {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            // 已挂载
            File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "multi_image_" + timeStamp + "";
            File tmpFile = new File(pic, fileName + ".jpg");
            return tmpFile;
        } else {
            File cacheDir = context.getCacheDir();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "multi_image_" + timeStamp + "";
            File tmpFile = new File(cacheDir, fileName + ".jpg");
            return tmpFile;
        }
    }
}
