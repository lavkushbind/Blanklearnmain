package com.blank_learn.newversion.O2;

import java.util.List;

public class Plan {
    private String planName;
    private int price; // **कीमत int होनी चाहिए**
    private String duration;
    private List<String> features;

    public Plan(String planName, int price, String duration, List<String> features) {
        this.planName = planName;
        this.price = price;
        this.duration = duration;
        this.features = features;
    }

    // Getters
    public String getPlanName() { return planName; }
    public int getPrice() { return price; } // **यह int लौटाएगा**
    public String getDuration() { return duration; }
    public List<String> getFeatures() { return features; }
}