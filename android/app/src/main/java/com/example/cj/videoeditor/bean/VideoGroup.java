package com.example.cj.videoeditor.bean;

public class VideoGroup {
    public String id;
    public String name;
    public String createDate;
    public int generatedCount;
    public int maxLimit;

    public VideoGroup(String id, String name, String createDate, int generatedCount, int maxLimit) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.generatedCount = generatedCount;
        this.maxLimit = maxLimit;
    }
}