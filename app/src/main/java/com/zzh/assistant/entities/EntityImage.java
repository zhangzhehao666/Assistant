package com.zzh.assistant.entities;

import android.graphics.Bitmap;

public class EntityImage {
    /**
     * 原图路径
     */
    private String pathOriginal;
    /**
     * 缩略图路径
     */
    private String pathThumbnail;
    /**
     * 图片名称
     */
    private String name;
    /**
     * 创建时间
     */
    private long time;
    /**
     * 图片类型 0：本地图片 1：网络图片
     */
    private int type;
    /**
     * 图片bitmap
     */
    private Bitmap bitmap;

    public EntityImage(){
        pathOriginal = "";
        pathThumbnail = "";
        name = "";
        time = 0;
        type = 0;
        bitmap = null;
    }

    public String getPathOriginal() {
        return pathOriginal;
    }

    public void setPathOriginal(String pathOriginal) {
        this.pathOriginal = pathOriginal;
    }

    public String getPathThumbnail() {
        return pathThumbnail;
    }

    public void setPathThumbnail(String pathThumbnail) {
        this.pathThumbnail = pathThumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public boolean equals(Object image) {
        try {
            EntityImage other = (EntityImage) image;
            return this.pathOriginal.equalsIgnoreCase(other.pathOriginal);
        }catch (ClassCastException e){
            e.printStackTrace();
        }
        return super.equals(image);
    }

    @Override
    public String toString() {
        return "[图片名称："+name+"]";
    }
}
