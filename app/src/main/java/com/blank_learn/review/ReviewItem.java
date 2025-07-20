package com.blank_learn.review;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties // Important for Firebase to ignore extra fields if any
public class ReviewItem {
        private String reviewerName;
        private String reviewerProfileImageUrl; // Changed from resId to String URL
        private float rating;
        private String reviewText;
        private String videoUrl;
        private long timestamp; // Using long for Firebase timestamp

        // --- No-argument constructor required for Firebase ---
        public ReviewItem() {
        }

        public ReviewItem(String reviewerName, String reviewerProfileImageUrl, float rating, String reviewText, String videoUrl, long timestamp) {
                this.reviewerName = reviewerName;
                this.reviewerProfileImageUrl = reviewerProfileImageUrl;
                this.rating = rating;
                this.reviewText = reviewText;
                this.videoUrl = videoUrl;
                this.timestamp = timestamp;
        }

        // --- Getters ---
        public String getReviewerName() {
                return reviewerName;
        }
        public String getReviewerProfileImageUrl() {
                return reviewerProfileImageUrl;
        }
        public float getRating() {
                return rating;
        }
        public String getReviewText() {
                return reviewText;
        }
        public String getVideoUrl() {
                return videoUrl;
        }
        public long getTimestamp() {
                return timestamp;
        }

        // --- Setters (also good practice for Firebase, though not always strictly needed for reading)
        public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
        public void setReviewerProfileImageUrl(String reviewerProfileImageUrl) { this.reviewerProfileImageUrl = reviewerProfileImageUrl; }
        public void setRating(float rating) { this.rating = rating; }
        public void setReviewText(String reviewText) { this.reviewText = reviewText; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }


        // --- Helper ---
        public boolean hasVideo() {
                return videoUrl != null && !videoUrl.isEmpty();
        }
}