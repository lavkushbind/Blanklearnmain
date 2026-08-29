// File: app/src/main/java/com/newversion/O2/UserPlanData.java

package com.blank_learn.newversion.O2;

import java.util.List;
import androidx.annotation.Keep; // <<< 1. YEH IMPORT ZAROORI HAI

/**
 * Yeh class user ke plan selections ko store karne ke liye hai.
 * Firebase isse seedha database mein save/read karne ke liye use karta hai.
 */

@Keep // <<< 2. CLASS PAR @Keep LAGAYEN
public class UserPlanData {

    // YEH SABHI VARIABLES 'public' HONE ZAROORI HAIN (Inhe Keep kar liya gaya hai)
    public String userId;
    public String selectedClass;
    public String selectedBoard;
    public List<String> selectedSubjects;
    public String selectedSlot;
    public Integer selectedPrice;
    public String paymentId;
    public String orderStatus;

    // Firebase ko data read karne ke liye ek khali constructor ki zaroorat hoti hai.
    public UserPlanData() {
    }

    // Yeh constructor PlanBuilderActivity mein data save karne ke kaam aata hai.
    public UserPlanData(String userId, String selectedClass, String selectedBoard,
                        List<String> selectedSubjects, String selectedSlot, Integer selectedPrice) {
        this.userId = userId;
        this.selectedClass = selectedClass;
        this.selectedBoard = selectedBoard;
        this.selectedSubjects = selectedSubjects;
        this.selectedSlot = selectedSlot;
        this.selectedPrice = selectedPrice;
        this.paymentId = "pending";
        this.orderStatus = "pending";
    }
}