package com.example.cj.videoeditor.bean;

public class UserInfo {
    public String phone;
    public String nickname;
    public String avatarUrl;
    public boolean vip;
    public String vipExpire;
    public int totalCompute;
    public int usedCompute;

    public UserInfo(String phone, String nickname, String avatarUrl, boolean vip,
                    String vipExpire, int totalCompute, int usedCompute) {
        this.phone = phone;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.vip = vip;
        this.vipExpire = vipExpire;
        this.totalCompute = totalCompute;
        this.usedCompute = usedCompute;
    }
}