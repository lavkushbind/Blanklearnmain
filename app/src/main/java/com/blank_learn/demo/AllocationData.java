package com.blank_learn.demo;
public class AllocationData {
    private String className;
    private String date;
    private  String classLink;
    private String demoID;
    private String paymentStatus;
    private String studentID;
    private String teacherID;
    private String timeSlot;

    // Default constructor (required for Firebase)
    public AllocationData() {}

    // Parameterized constructor
    public AllocationData(String className, String date, String demoID, String paymentStatus, String studentID, String teacherID, String timeSlot) {
        this.className = className;
        this.date = date;
        this.demoID = demoID;
        this.paymentStatus = paymentStatus;
        this.studentID = studentID;
        this.teacherID = teacherID;
        this.timeSlot = timeSlot;
    }

    public String getClassLink() {
        return classLink;
    }

    public void setClassLink(String classLink) {
        this.classLink = classLink;
    }

    // Getters and setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDemoID() {
        return demoID;
    }

    public void setDemoID(String demoID) {
        this.demoID = demoID;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }
}