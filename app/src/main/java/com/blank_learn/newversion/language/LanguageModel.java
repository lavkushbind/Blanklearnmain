package com.blank_learn.newversion.language;

/**
 * Data model for Language Selection Cards.
 * Stores the full name, the API/Locale code, and the flag resource ID.
 */
public class LanguageModel {

    private String name;
    private String code;     // E.g., "en", "es", "fr"
    private int flagResId;   // E.g., R.drawable.ic_flag_uk

    // Constructor
    public LanguageModel(String name, String code, int flagResId) {
        this.name = name;
        this.code = code;
        this.flagResId = flagResId;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getFlagResId() {
        return flagResId;
    }
}