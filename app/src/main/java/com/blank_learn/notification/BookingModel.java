package com.blank_learn.notification;

public class BookingModel {
    private String className;
    private String date;
    private String timeSlot;
    private String teacherID;

    public BookingModel() {} // Required empty constructor

    public BookingModel(String teacherID, String className, String timeSlot, String date) {
        this.teacherID = teacherID;
        this.className = className;
        this.timeSlot = timeSlot;
        this.date = date;
    }

    public String getTeacherID() { return teacherID; }
    public String getClassName() { return className; }  // Ensure it's named correctly
    public String getTimeSlot() { return timeSlot; }
    public String getDate() { return date; }
}
