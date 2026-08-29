package com.blank_learn.newversion.language;

/**
 * Data model for Level Selection Cards.
 * Stores the title, detailed description, and icon resource ID.
 */
public class LevelModel {

    private String title;
    private String description;
    private int iconResId;

    // Constructor
    public LevelModel(String title, String description, int iconResId) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
    }

    // --- Getters ---

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    // --- Optional Setters (Agar zarurat ho toh) ---

    // public void setTitle(String title) { this.title = title; }
}