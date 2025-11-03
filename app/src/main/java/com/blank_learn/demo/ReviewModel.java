package com.blank_learn.demo;

public class ReviewModel {
    private String imageUrl;
    private String name;
    private String statusTag; // e.g., "Class 7 Student"
    private String reviewText;
    private String attribution; // e.g., "— Father of Aayan Singh"
    private float rating;

    // Firebase ke liye empty constructor zaroori hai
    public ReviewModel() {
    }

    public ReviewModel(String imageUrl, String name, String statusTag, String reviewText, String attribution, float rating) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.statusTag = statusTag;
        this.reviewText = reviewText;
        this.attribution = attribution;
        this.rating = rating;
    }

    // Getters and Setters
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatusTag() { return statusTag; }
    public void setStatusTag(String statusTag) { this.statusTag = statusTag; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public String getAttribution() { return attribution; }
    public void setAttribution(String attribution) { this.attribution = attribution; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}