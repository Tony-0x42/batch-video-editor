package com.example.cj.videoeditor.bean;

public class VideoGroup {
    public String id;
    public String name;
    public String createDate;
    public int generatedCount;
    public int maxLimit;
    public int sortWeight;

    public VideoGroup(String id, String name, String createDate, int generatedCount, int maxLimit) {
        this(id, name, createDate, generatedCount, maxLimit, 0);
    }

    public VideoGroup(String id, String name, String createDate, int generatedCount, int maxLimit, int sortWeight) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.generatedCount = generatedCount;
        this.maxLimit = maxLimit;
        this.sortWeight = sortWeight;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCreateDate() { return createDate; }
    public int getGeneratedCount() { return generatedCount; }
    public int getMaxLimit() { return maxLimit; }
    public int getMaxCount() { return maxLimit; }
    public int getSortWeight() { return sortWeight; }
    public void setSortWeight(int sortWeight) { this.sortWeight = sortWeight; }
}