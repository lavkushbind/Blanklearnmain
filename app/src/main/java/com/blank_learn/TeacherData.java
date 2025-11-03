package com.blank_learn;

public class TeacherData {
    public String name;
    public String experience;
    public String institutes;
    public String device;
    public String familiarTools;
    public String resumeUrl;
    public String videoLink;
    public String status;
    public String teacherId;

    public TeacherData() {
    }

    public TeacherData(String teacherId, String name, String experience, String institutes, String device, String familiarTools, String resumeUrl, String videoLink) {
        this.teacherId = teacherId;
        this.name = name;
        this.experience = experience;
        this.institutes = institutes;
        this.device = device;
        this.familiarTools = familiarTools;
        this.resumeUrl = resumeUrl;
        this.videoLink = videoLink;
        this.status = "Pending"; // Default status for new applications
    }
}