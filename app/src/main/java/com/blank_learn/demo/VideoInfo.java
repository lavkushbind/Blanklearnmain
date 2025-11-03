package com.blank_learn.demo;


public class VideoInfo {
    private String name;
    private String videoUrl;

    // IMPORTANT: Default constructor is required for calls to DataSnapshot.getValue(VideoInfo.class)
    public VideoInfo() {
    }

    public VideoInfo(String name, String videoUrl) {
        this.name = name;
        this.videoUrl = videoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
