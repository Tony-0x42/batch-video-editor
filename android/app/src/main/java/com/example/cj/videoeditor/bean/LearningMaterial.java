package com.example.cj.videoeditor.bean;

public class LearningMaterial {
    public String id;
    public String title;
    public String type;
    public String coverUrl;
    public String contentUrl;
    public String publishTime;
    public int viewCount;

    public LearningMaterial(String id, String title, String type, String coverUrl,
                            String contentUrl, String publishTime, int viewCount) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.coverUrl = coverUrl;
        this.contentUrl = contentUrl;
        this.publishTime = publishTime;
        this.viewCount = viewCount;
    }
}