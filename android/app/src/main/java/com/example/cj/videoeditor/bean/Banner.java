package com.example.cj.videoeditor.bean;

public class Banner {
    public String id;
    public String imageUrl;
    public String link;
    public int sort;

    public Banner(String id, String imageUrl, String link, int sort) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.link = link;
        this.sort = sort;
    }
}