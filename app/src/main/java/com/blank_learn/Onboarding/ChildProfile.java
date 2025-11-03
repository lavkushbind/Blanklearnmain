package com.blank_learn.Onboarding;
import java.util.ArrayList;
import java.util.List;

/**
 * यह क्लास एक बच्चे की प्रोफाइल से जुड़ी सारी जानकारी को होल्ड करती है।
 * यह एक डेटा मॉडल या POJO (Plain Old Java Object) है।
 */
public class ChildProfile {

    // 1. वैरिएबल्स (Fields) - जानकारी रखने की जगह
    // इन्हें 'private' बनाया गया है ताकि कोई दूसरी क्लास इन्हें सीधे न बदल सके।
    private String name;
    private String grade;
    private String board;
    private List<String> subjects; // एक से ज़्यादा सब्जेक्ट हो सकते हैं, इसलिए List का इस्तेमाल किया गया है।
    private String preferredTiming;

    // 2. कंस्ट्रक्टर (Constructor) - जब भी इस क्लास का नया ऑब्जेक्ट बनता है, यह अपने आप चलता है।
    public ChildProfile() {
        // subjects लिस्ट को यहीं पर इनिशियलाइज़ करना एक अच्छी प्रैक्टिस है,
        // ताकि बाद में NullPointerException की एरर न आए।
        this.subjects = new ArrayList<>();
    }

    // 3. गेटर्स और सेटर्स (Getters and Setters)
    // ये पब्लिक मेथड्स हैं जो प्राइवेट वैरिएबल्स की वैल्यू को पढ़ने (get) और बदलने (set) की इजाज़त देते हैं।

    // --- Name के लिए ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- Grade के लिए ---
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    // --- Board के लिए ---
    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    // --- Subjects के लिए ---
    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    // --- PreferredTiming के लिए ---
    public String getPreferredTiming() {
        return preferredTiming;
    }

    public void setPreferredTiming(String preferredTiming) {
        this.preferredTiming = preferredTiming;
    }
}