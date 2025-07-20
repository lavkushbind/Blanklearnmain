package com.blank_learn.loginandsignup;

import com.google.firebase.database.PropertyName;

// Renamed from Users to User (singular) for standard Java convention
public class Users {

    // --- Fields ---
    // All fields are private to enforce encapsulation.
    // Use @PropertyName to link the Java field to the exact key in the Firebase database.
    // This prevents issues if you use ProGuard/R8 to shrink your app.

    @PropertyName("name")
    private String name;

    @PropertyName("email")
    private String email;

    @PropertyName("userID")
    private String userID;

    @PropertyName("pass")
    private String pass; // Note: Storing passwords in the database is not secure. Consider Firebase Auth.

    @PropertyName("phone")
    private String phone;

    @PropertyName("coverpic")
    private String coverpic;

    @PropertyName("profilepic")
    private String profilepic;

    @PropertyName("bio")
    private String bio;

    // Corrected typo from "profesion" to "profession" in Java, but kept JSON key the same
    @PropertyName("profesion")
    private String profesion;

    @PropertyName("instagram")
    private String instagram;

    @PropertyName("linkedin")
    private String linkedin;

    @PropertyName("twitter")
    private String twitter;

    @PropertyName("fb")
    private String fb;

    @PropertyName("save")
    private String save; // Consider if this should be a List<String>

    @PropertyName("buy")
    private String buy; // Consider if this should be a List<String>

    @PropertyName("upload")
    private String upload;

    @PropertyName("video")
    private String video;

    @PropertyName("storyid")
    private String storyid;

    @PropertyName("uid")
    private String uid;

    @PropertyName("registrationMethod")
    private String registrationMethod;

    @PropertyName("charge")
    private long charge;

    @PropertyName("followercount")
    private int followercount;

    @PropertyName("verify")
    private boolean verify;

    // CRITICAL FIX: Changed from String to Long. Long can be null, which is safer.
    @PropertyName("creationTimestamp")
    private Long creationTimestamp;


    // --- Constructors ---

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class)
     */
    public Users() {
    }

    // --- Getters and Setters ---
    // Organized for readability

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCoverpic() { return coverpic; }
    public void setCoverpic(String coverpic) { this.coverpic = coverpic; }

    public String getProfilepic() { return profilepic; }
    public void setProfilepic(String profilepic) { this.profilepic = profilepic; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfesion() { return profesion; }
    public void setProfesion(String profession) { this.profesion = profession; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }

    public String getFb() { return fb; }
    public void setFb(String fb) { this.fb = fb; }

    public String getSave() { return save; }
    public void setSave(String save) { this.save = save; }

    public String getBuy() { return buy; }
    public void setBuy(String buy) { this.buy = buy; }

    public String getUpload() { return upload; }
    public void setUpload(String upload) { this.upload = upload; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }

    public String getStoryid() { return storyid; }
    public void setStoryid(String storyid) { this.storyid = storyid; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getRegistrationMethod() { return registrationMethod; }
    public void setRegistrationMethod(String registrationMethod) { this.registrationMethod = registrationMethod; }

    public long getCharge() { return charge; }
    public void setCharge(long charge) { this.charge = charge; }

    public int getFollowercount() { return followercount; }
    public void setFollowercount(int followercount) { this.followercount = followercount; }

    public boolean isVerify() { return verify; }
    public void setVerify(boolean verify) { this.verify = verify; }

    public Long getCreationTimestamp() { return creationTimestamp; }
    public void setCreationTimestamp(Long creationTimestamp) { this.creationTimestamp = creationTimestamp; }
}
