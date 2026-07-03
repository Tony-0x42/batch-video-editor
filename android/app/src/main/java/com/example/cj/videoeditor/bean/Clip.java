package com.example.cj.videoeditor.bean;

public class Clip {
    public String id;
    public int index;
    public String videoUrl;
    public boolean mirrored;

    public Clip(String id, int index, String videoUrl, boolean mirrored) {
        this.id = id;
        this.index = index;
        this.videoUrl = videoUrl;
        this.mirrored = mirrored;
    }
}