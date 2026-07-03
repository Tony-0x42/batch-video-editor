package com.example.cj.videoeditor.bean;

import java.util.List;

public class WatermarkResult {
    public String videoUrl;
    public List<String> imageUrls;
    public String text;

    public WatermarkResult(String videoUrl, List<String> imageUrls, String text) {
        this.videoUrl = videoUrl;
        this.imageUrls = imageUrls;
        this.text = text;
    }
}