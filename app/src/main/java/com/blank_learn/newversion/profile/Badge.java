package com.blank_learn.newversion.profile;

public class Badge {
    private String name;
    private int imageResId; // Resource ID (e.g., R.drawable.star)
    private boolean isUnlocked;

    public Badge(String name, int imageResId, boolean isUnlocked) {
        this.name = name;
        this.imageResId = imageResId;
        this.isUnlocked = isUnlocked;
    }

    public String getName() { return name; }
    public int getImageResId() { return imageResId; }
    public boolean isUnlocked() { return isUnlocked; }
}