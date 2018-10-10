package com.zzh.assistant.entities;

import android.os.Parcel;
import android.os.Parcelable;

import cn.bmob.v3.BmobObject;

public class Music extends BmobObject implements Comparable, Parcelable {
    private int id;
    private String singerName, songName, url, downUrl, albumpic_big, albumpic_small;
    private String userId;
    private String firstLetter;
    private String songmid;

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public String getSongmid() {
        return songmid;
    }

    public void setSongmid(String songmid) {
        this.songmid = songmid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSingerName() {
        return singerName;
    }

    public void setSingerName(String singerName) {
        this.singerName = singerName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getAlbumpic_big() {
        return albumpic_big;
    }

    public void setAlbumpic_big(String albumpic_big) {
        this.albumpic_big = albumpic_big;
    }

    public String getAlbumpic_small() {
        return albumpic_small;
    }

    public void setAlbumpic_small(String albumpic_small) {
        this.albumpic_small = albumpic_small;
    }

    public String getFirstLetter() {
        return firstLetter;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.firstLetter);
    }

    @Override
    public int compareTo(Object o) {
        Music info = (Music) o;
        if (info.getFirstLetter().equals("#"))
            return -1;
        if (firstLetter.equals("#"))
            return 1;
        return firstLetter.compareTo(info.getFirstLetter());
    }
    public Music() {
    }
    protected Music(Parcel in) {
        this.firstLetter = in.readString();
    }
}
