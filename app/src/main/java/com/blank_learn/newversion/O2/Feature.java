package com.blank_learn.newversion.O2;

public class Feature {
    // Yeh feature ka text store karega, jaise "Live Classes", "24/7 Doubt Support"
    private String featureText;

    // Firebase ke liye empty constructor zaroori hai
    public Feature() {
    }

    // Ek constructor jisse hum aasani se object bana sakein
    public Feature(String featureText) {
        this.featureText = featureText;
    }

    // Getters aur Setters
    public String getFeatureText() {
        return featureText;
    }

    public void setFeatureText(String featureText) {
        this.featureText = featureText;
    }
}