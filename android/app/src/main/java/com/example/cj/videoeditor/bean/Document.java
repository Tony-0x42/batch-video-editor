package com.example.cj.videoeditor.bean;

public class Document {
    public String id;
    public String title;
    public String category;
    public String updateTime;
    public String content;

    public Document(String id, String title, String category, String updateTime, String content) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.updateTime = updateTime;
        this.content = content;
    }
}