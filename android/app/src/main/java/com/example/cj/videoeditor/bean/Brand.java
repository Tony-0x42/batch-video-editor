package com.example.cj.videoeditor.bean;

import java.util.List;

public class Brand {
    public String id;
    public String name;
    public String logoUrl;
    public String intro;
    public String detail;
    public List<String> mediaUrls;

    public Brand(String id, String name, String logoUrl, String intro,
                 String detail, List<String> mediaUrls) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.intro = intro;
        this.detail = detail;
        this.mediaUrls = mediaUrls;
    }
}