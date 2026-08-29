package com.blank_learn.newversion.Commune;

public class Post {
    private String postId;
    private String userName;
    private String userAvatarUrl; // URL to image
    private String content;
    private String postImageUrl; // Optional image in post
    private long timestamp;
    private int likeCount;
    private int commentCount;
    private boolean isTeacher; // To identify Teacher Moderators
    private boolean isPinned;

    // Empty constructor required for Firebase
    public Post() {}

    public Post(String userName, String content, boolean isTeacher, boolean isPinned) {
        this.userName = userName;
        this.content = content;
        this.isTeacher = isTeacher;
        this.isPinned = isPinned;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public boolean isTeacher() { return isTeacher; }
    public boolean isPinned() { return isPinned; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public String getUserAvatarUrl() { return userAvatarUrl; }
}