package com.blank_learn.Onboarding;

public class Board {
    private String name;
    private int logoResId;
    public Board(String name, int logoResId) {
        this.name = name;
        this.logoResId = logoResId;
    }

    public String getName() {
        return name;
    }

    public int getLogoResId() {
        return logoResId;
    }
}