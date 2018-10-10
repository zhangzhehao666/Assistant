package com.zzh.assistant.entities;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

public class User extends BmobUser {

    private String pwd;
    private String headerImage;
    private String label;
    private String point;
    private String lastSignTime;
    private BmobFile img;

    public BmobFile getImg() {
        return img;
    }

    public void setImg(BmobFile img) {
        this.img = img;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getHeaderImage() {
        return headerImage;
    }

    public void setHeaderImage(String headerImage) {
        this.headerImage = headerImage;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getLastSignTime() {
        return lastSignTime;
    }

    public void setLastSignTime(String lastSignTime) {
        this.lastSignTime = lastSignTime;
    }
}
