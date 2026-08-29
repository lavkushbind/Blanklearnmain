package com.blank_learn.newversion.O2;

import com.google.firebase.database.PropertyName;

public class EducatorVideoModel {
    private String videoUrl;

    public EducatorVideoModel() { }

    public EducatorVideoModel(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // Agar Firebase mein key ka naam "videoUrl" hai
    @PropertyName("videoUrl")
    public String getVideoUrl() {
        return videoUrl;
    }

    @PropertyName("videoUrl")
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // Agar Firebase mein key ka naam "url" hai (Backup)
    @PropertyName("url")
    public void setUrl(String url) {
        this.videoUrl = url;
    }

    // Agar Firebase mein key ka naam "video" hai (Backup)
    @PropertyName("video")
    public void setVideo(String video) {
        this.videoUrl = video;
    }
}