package com.example.cj.videoeditor.bean;

public class ProfileMenu {
    public String title;
    public int iconRes;
    public String extra;

    public ProfileMenu(String title, int iconRes, String extra) {
        this.title = title;
        this.iconRes = iconRes;
        this.extra = extra;
    }

    public ProfileMenu(String title, int iconRes) {
        this(title, iconRes, "");
    }
}