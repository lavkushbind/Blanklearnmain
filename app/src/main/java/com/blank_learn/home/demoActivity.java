package com.blank_learn.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.blank_learn.dark.R;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class demoActivity extends AppCompatActivity {

     public static final String DEBUG_TAG = "DEMO_ACTIVITY_DEBUG";

     private CalendarView calendarView;
    private LinearLayout timeSlotContainer, classContainer;
    private androidx.cardview.widget.CardView mainLayout;
    private Button payButton;
    private ProgressBar progressBar;
    private TextView selectedClassView, selectedTimeSlotView, alreadyBookedMessage;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String selectedDate, selectedTimeSlot, selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        // --- Initialization ---
        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        currentUserID = mAuth.getCurrentUser().getUid();

        // --- View Binding ---
        calendarView = findViewById(R.id.calendar_view);
        timeSlotContainer = findViewById(R.id.time_slot_container);
        classContainer = findViewById(R.id.time_slot_container1);
        payButton = findViewById(R.id.next_button);
        mainLayout = findViewById(R.id.main_layout_demo);
        progressBar = findViewById(R.id.progress_bar_demo);
        alreadyBookedMessage = findViewById(R.id.already_booked_message);

        // --- Initial UI State ---
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        alreadyBookedMessage.setVisibility(View.GONE);

        setDefaultDate();
        checkIfUserHasBooking();

        // --- Listener Setup ---
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year
        );

        payButton.setOnClickListener(v -> {
            Log.d(DEBUG_TAG, "--- Booking process started. Button clicked. ---");
            allocateTeacher();
        });
    }

    private void setDefaultDate() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
        Log.d(DEBUG_TAG, "Default date set to: " + selectedDate);
    }

    private void checkIfUserHasBooking() {
        Query userBookingQuery = databaseReference.child("allocated_classes")
                .orderByChild("studentID")
                .equalTo(currentUserID);

        userBookingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                if (snapshot.exists()) {
                    Log.d(DEBUG_TAG, "User has a previous booking. Showing message.");
                    mainLayout.setVisibility(View.GONE);
                    alreadyBookedMessage.setVisibility(View.VISIBLE);
                    alreadyBookedMessage.setText("You have already booked your free demo class. Please check your schedule.");
                } else {
                    Log.d(DEBUG_TAG, "User has not booked. Showing booking UI.");
                    mainLayout.setVisibility(View.VISIBLE);
                    alreadyBookedMessage.setVisibility(View.GONE);
                    loadTimeSlots();
                    loadClasses();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(demoActivity.this, "Error checking booking status.", Toast.LENGTH_SHORT).show();
                Log.e(DEBUG_TAG, "Database error checking booking status", error.toException());
            }
        });
    }

    private void allocateTeacher() {
        if (selectedDate == null || selectedTimeSlot == null || selectedClass == null) {
            Log.e(DEBUG_TAG, "FAILURE: A selection is null. Halting process.");
            Log.e(DEBUG_TAG, "selectedDate: " + selectedDate);
            Log.e(DEBUG_TAG, "selectedTimeSlot: " + selectedTimeSlot);
            Log.e(DEBUG_TAG, "selectedClass: " + selectedClass);
            Toast.makeText(this, "Please select a date, time slot, and class", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(DEBUG_TAG, "All selections are present. Querying Firebase for available teachers...");
        databaseReference.child("teachers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(DEBUG_TAG, "Firebase onDataChange successful. Searching through teachers...");
                String bestTeacherID = null;
                int minStudents = Integer.MAX_VALUE;

                for (DataSnapshot teacherSnap : snapshot.getChildren()) {
                    String teacherID = teacherSnap.getKey();
                    DataSnapshot classesSnapshot = teacherSnap.child("classes");
                    DataSnapshot timeSlotsSnapshot = teacherSnap.child("timeSlots");
                    DataSnapshot studentsCountSnapshot = teacherSnap.child("studentsCount").child(selectedTimeSlot);
                    if (classesSnapshot.exists() && timeSlotsSnapshot.exists() && studentsCountSnapshot.exists()) {
                        boolean teachesClass = false;
                        for (DataSnapshot classSnap : classesSnapshot.getChildren()) {
                            if (classSnap.getValue(String.class).equals(selectedClass.replace("Class ", ""))) {
                                teachesClass = true;
                                break;
                            }
                        }
                        boolean availableAtSlot = false;
                        for (DataSnapshot slotSnap : timeSlotsSnapshot.getChildren()) {
                            if (slotSnap.getValue(String.class).equals(selectedTimeSlot)) {
                                availableAtSlot = true;
                                break;
                            }
                        }
                        int studentCount = studentsCountSnapshot.getValue(Integer.class) != null ? studentsCountSnapshot.getValue(Integer.class) : 0;
                        if (teachesClass && availableAtSlot && studentCount < 3) {
                            if (studentCount < minStudents) {
                                minStudents = studentCount;
                                bestTeacherID = teacherID;
                            }
                        }
                    }
                }

                if (bestTeacherID != null) {
                    Log.d(DEBUG_TAG, "SUCCESS: Found a suitable teacher. ID: " + bestTeacherID);
                    assignTeacher(bestTeacherID);
                } else {
                    Log.e(DEBUG_TAG, "FAILURE: No available teacher found for the selected slot/class.");
                    Toast.makeText(demoActivity.this, "No available teacher for this slot", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DEBUG_TAG, "FIREBASE ERROR: Could not fetch teachers.", error.toException());
                Toast.makeText(demoActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignTeacher(String teacherID) {
        Log.d(DEBUG_TAG, "assignTeacher called. Running transaction to update student count...");
        DatabaseReference teacherRef = databaseReference.child("teachers").child(teacherID).child("studentsCount").child(selectedTimeSlot);
        teacherRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.getValue(Integer.class);
                if (count == null) count = 0;
                if (count < 3) {
                    currentData.setValue(count + 1);
                    return Transaction.success(currentData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(DEBUG_TAG, "TRANSACTION ERROR: " + error.getMessage());
                    Toast.makeText(demoActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (committed) {
                    Log.d(DEBUG_TAG, "TRANSACTION SUCCESS: Slot was available. Proceeding to save allocation.");
                    saveAllocationToDatabase(teacherID);
                } else {
                    Log.e(DEBUG_TAG, "TRANSACTION FAILED: Slot is full or was taken. Aborting.");
                    Toast.makeText(demoActivity.this, "Slot is full, please select another time.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveAllocationToDatabase(String teacherID) {
        Log.d(DEBUG_TAG, "saveAllocationToDatabase called. Creating final record...");
        String randomKey = database.getReference().push().getKey();
        if (randomKey == null) {
            Toast.makeText(this, "Could not create allocation record.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference allocationRef = databaseReference.child("allocated_classes").child(randomKey);
        DatabaseReference studentNameRef = databaseReference.child("Users").child(currentUserID).child("name");

        studentNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String studentName = "A student";
                if (snapshot.exists() && snapshot.getValue(String.class) != null) {
                    studentName = snapshot.getValue(String.class);
                }

                HashMap<String, Object> allocationData = new HashMap<>();
                allocationData.put("teacherID", teacherID);
                allocationData.put("demoID", randomKey);
                allocationData.put("className", selectedClass);
                allocationData.put("timeSlot", selectedTimeSlot);
                allocationData.put("date", selectedDate);
                allocationData.put("studentID", currentUserID);
                allocationData.put("studentName", studentName);
                allocationData.put("paymentStatus", "booked");

                allocationRef.setValue(allocationData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(DEBUG_TAG, "DATABASE SAVE SUCCESS: Allocation saved to Firebase.");
                            Toast.makeText(demoActivity.this, "Allocation saved! Taking you to your schedule...", Toast.LENGTH_LONG).show();

                            // Log events to analytics
                            Bundle firebaseBundle = new Bundle();
                            firebaseBundle.putString("class_name", selectedClass);
                            firebaseBundle.putString("time_slot", selectedTimeSlot);
                            mFirebaseAnalytics.logEvent("demo_booked", firebaseBundle);

                            Log.d(DEBUG_TAG, "SUCCESS: >>> ABOUT TO SEND META SCHEDULE EVENT NOW! <<<");
                            logMetaScheduleEvent(randomKey, selectedClass, selectedTimeSlot);

                            // Navigate back to MainActivity after success
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish(); // Close this activity.
                        })
                        .addOnFailureListener(e -> {
                            Log.e(DEBUG_TAG, "DATABASE SAVE FAILED: Could not save allocation.", e);
                            Toast.makeText(demoActivity.this, "Failed to save allocation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DEBUG_TAG, "Error fetching user details.", error.toException());
                Toast.makeText(demoActivity.this, "Error fetching user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTimeSlots() {
        String[] timeSlots = {"6-7 AM", "7-8 AM", "8-9 AM", "9-10 AM", "10-11 AM", "11-12 AM", "12-1 PM",
                "1-2 PM", "2-3 PM", "3-4 PM", "4-5 PM", "5-6 PM", "6-7 PM", "7-8 PM", "8-9 PM", "9-10 PM"};
        for (String slot : timeSlots) {
            TextView slotView = createTextView(slot);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            slotView.setLayoutParams(params);
            slotView.setBackground(getResources().getDrawable(R.drawable.signbg));
            slotView.setOnClickListener(v -> {
                if (selectedTimeSlotView == slotView) {
                    slotView.setBackground(getResources().getDrawable(R.drawable.signbg));
                    selectedTimeSlotView = null;
                    selectedTimeSlot = null;
                } else {
                    if (selectedTimeSlotView != null) {
                        selectedTimeSlotView.setBackground(getResources().getDrawable(R.drawable.signbg));
                    }
                    selectedTimeSlotView = slotView;
                    selectedTimeSlot = slot;
                    slotView.setBackground(ContextCompat.getDrawable(this, R.drawable.click_bg2));
                }
            });
            timeSlotContainer.addView(slotView);
        }
    }

    private void loadClasses() {
        String[] classes = {"Class LKG", "Class UKG", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8"};
        for (String cls : classes) {
            TextView classView = createTextView(cls);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(10, 10, 10, 10);
            classView.setLayoutParams(params);
            classView.setBackground(ContextCompat.getDrawable(this, R.drawable.signbg));
            classView.setOnClickListener(v -> {
                if (selectedClassView == classView) {
                    classView.setBackground(ContextCompat.getDrawable(this, R.drawable.signbg));
                    selectedClassView = null;
                    selectedClass = null;
                } else {
                    if (selectedClassView != null) {
                        selectedClassView.setBackground(ContextCompat.getDrawable(this, R.drawable.signbg));
                    }
                    selectedClassView = classView;
                    selectedClass = cls;
                    classView.setBackground(ContextCompat.getDrawable(this, R.drawable.click_bg2));
                }
            });
            classContainer.addView(classView);
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20, 20, 20, 20);
        return textView;
    }

    private void logMetaScheduleEvent(String demoId, String className, String timeSlot) {
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, demoId);
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "demo_class");
        params.putString("class_name", className);
        params.putString("time_slot", timeSlot);
        logger.logEvent(AppEventsConstants.EVENT_NAME_SCHEDULE, params);
        Log.d("MetaEvent", "Logged 'Schedule' event for demo ID: " + demoId);
    }
}
