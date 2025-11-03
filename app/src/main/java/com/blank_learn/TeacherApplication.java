package com.blank_learn;


 public class TeacherApplication {

    // Fields must match the form inputs
    private String publicName;
    private String legalName;
    private String email;
    private String phone;
    private String location;
    private String howHeard;
    private String topics;
    private String ageRanges;
    private String experienceType;
    private String yearsExperience;
    private String qualifications;
    private String uid; // To store the unique ID of the application

    // IMPORTANT: Firebase needs a public no-argument constructor for deserialization
    public TeacherApplication() {
    }

    public TeacherApplication(String publicName, String legalName, String email, String phone, String location, String howHeard, String topics, String ageRanges, String experienceType, String yearsExperience, String qualifications) {
        this.publicName = publicName;
        this.legalName = legalName;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.howHeard = howHeard;
        this.topics = topics;
        this.ageRanges = ageRanges;
        this.experienceType = experienceType;
        this.yearsExperience = yearsExperience;
        this.qualifications = qualifications;
    }

    // --- Getters for all fields (Firebase uses these) ---
    public String getPublicName() { return publicName; }
    public String getLegalName() { return legalName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getHowHeard() { return howHeard; }
    public String getTopics() { return topics; }
    public String getAgeRanges() { return ageRanges; }
    public String getExperienceType() { return experienceType; }
    public String getYearsExperience() { return yearsExperience; }
    public String getQualifications() { return qualifications; }
    public String getUid() { return uid; }

    public void setUid(String uid) {
        this.uid = uid;
    }
}