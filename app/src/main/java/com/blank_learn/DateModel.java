package com.blank_learn; // Apna package name check kar lein

public class DateModel {
    private String dayName;
    private String dateNumber;
    private String fullDate;
    private boolean isSelected;

    public DateModel(String dayName, String dateNumber, String fullDate, boolean isSelected) {
        this.dayName = dayName;
        this.dateNumber = dateNumber;
        this.fullDate = fullDate;
        this.isSelected = isSelected;
    }

    // Getters
    public String getDayName() { return dayName; }
    public String getDateNumber() { return dateNumber; }
    public String getFullDate() { return fullDate; }
    public boolean isSelected() { return isSelected; }

    // Setter for isSelected
    public void setSelected(boolean selected) { isSelected = selected; }
}