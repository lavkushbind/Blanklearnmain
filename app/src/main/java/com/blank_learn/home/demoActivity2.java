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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

 import com.blank_learn.dark.R;
import com.blank_learn.demo.PaymentActivityDemo;
import com.blank_learn.demo.ReviewAdapter;
import com.blank_learn.demo.ReviewModel;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class demoActivity2 extends AppCompatActivity {

    public static final String DEBUG_TAG = "DEMO_BOOKING_V4";
    private static final int PAYMENT_REQUEST_CODE = 123;

    // UI Elements
    private CalendarView calendarView;
    private LinearLayout timeSlotContainer, classContainer;
    private CardView mainLayout;
    private Button payButton;
    private ProgressBar progressBar;
    private TextView selectedClassView, selectedTimeSlotView, alreadyBookedMessage;
    private RecyclerView reviewsRecyclerView;

    // Firebase & Analytics
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private AppEventsLogger metaLogger;

    // Adapters & Data
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList;

    // State Variables
    private String currentUserID;
    private String selectedDate, selectedTimeSlot, selectedClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        initializeVariables();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to book a demo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserID = mAuth.getCurrentUser().getUid();

        setupUI();
        checkIfUserHasBooking();
        loadReviews();
    }

    private void initializeVariables() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        metaLogger = AppEventsLogger.newLogger(this);

        calendarView = findViewById(R.id.calendar_view);
        timeSlotContainer = findViewById(R.id.time_slot_container);
        classContainer = findViewById(R.id.time_slot_container1);
        payButton = findViewById(R.id.next_button);
        mainLayout = findViewById(R.id.main_layout_demo);
        progressBar = findViewById(R.id.progress_bar_demo);
        alreadyBookedMessage = findViewById(R.id.already_booked_message);
        reviewsRecyclerView = findViewById(R.id.reviews_recycler_view);

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
    }

    private void setupUI() {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        alreadyBookedMessage.setVisibility(View.GONE);

        setDefaultDate();

        payButton.setOnClickListener(v -> initiateBookingProcess());

        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime());
        });
    }

    private void setDefaultDate() {
        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private void checkIfUserHasBooking() {
        databaseReference.child("allocated_classes").orderByChild("studentID").equalTo(currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        if (snapshot.exists()) {
                            mainLayout.setVisibility(View.GONE);
                            alreadyBookedMessage.setVisibility(View.VISIBLE);
                            alreadyBookedMessage.setText("You have already booked your 1-on-1 demo class.");
                        } else {
                            mainLayout.setVisibility(View.VISIBLE);
                            loadTimeSlots();
                            loadClasses();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleError("Error checking booking status: " + error.getMessage());
                    }
                });
    }

    private void loadReviews() {
        databaseReference.child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviewList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ReviewModel review = snapshot.getValue(ReviewModel.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(demoActivity2.this, "Failed to load reviews.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- New Booking Flow ---

    private void initiateBookingProcess() {
        if (selectedDate == null || selectedTimeSlot == null || selectedClass == null) {
            Toast.makeText(this, "Please select a date, time slot, and class.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(DEBUG_TAG, "Booking process initiated. Checking availability...");
        progressBar.setVisibility(View.VISIBLE);

        checkTeacherAvailability(isAvailable -> {
            progressBar.setVisibility(View.GONE);
            if (isAvailable) {
                Log.d(DEBUG_TAG, "Pre-check OK: Teacher available. Proceeding to payment.");
                launchPaymentActivity();
            } else {
                Log.e(DEBUG_TAG, "Pre-check FAIL: No teacher available for this slot.");
                Toast.makeText(this, "Sorry, no teacher available for this slot. Please try another.", Toast.LENGTH_LONG).show();
            }
        });
    }

    interface AvailabilityCallback {
        void onResult(boolean isAvailable);
    }

    private void checkTeacherAvailability(final AvailabilityCallback callback) {
        databaseReference.child("teachers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isAnyTeacherAvailable = false;
                for (DataSnapshot teacherSnap : snapshot.getChildren()) {
                    if (isTeacherSuitable(teacherSnap)) {
                        isAnyTeacherAvailable = true;
                        break;
                    }
                }
                callback.onResult(isAnyTeacherAvailable);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(DEBUG_TAG, "Pre-check DB Error", error.toException());
                callback.onResult(false);
            }
        });
    }

    private void launchPaymentActivity() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userEmail = "user@example.com";
                String userPhone = "";
                if (snapshot.exists()) {
                    userEmail = snapshot.child("email").getValue(String.class) != null ? snapshot.child("email").getValue(String.class) : "user@example.com";
                    userPhone = snapshot.child("phone").getValue(String.class) != null ? snapshot.child("phone").getValue(String.class) : "";
                }
                Intent intent = new Intent(demoActivity2.this, PaymentActivityDemo.class);
                intent.putExtra("amount", "1");
                intent.putExtra("user_email", userEmail);
                intent.putExtra("user_phone", userPhone);
                progressBar.setVisibility(View.GONE);
                startActivityForResult(intent, PAYMENT_REQUEST_CODE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleError("Could not fetch user details: " + error.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(DEBUG_TAG, "Payment Successful! Finalizing booking.");
                Toast.makeText(this, "Payment confirmed. Booking your session...", Toast.LENGTH_SHORT).show();
                finalizeBooking();
            } else {
                Log.e(DEBUG_TAG, "Payment was not completed.");
                Toast.makeText(this, "Payment failed or was cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void finalizeBooking() {
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.child("teachers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String bestTeacherID = null;
                int minStudents = Integer.MAX_VALUE;

                for (DataSnapshot teacherSnap : snapshot.getChildren()) {
                    if (isTeacherSuitable(teacherSnap)) {
                        int currentStudentCount = teacherSnap.child("studentsCount").child(selectedTimeSlot).getValue(Integer.class);
                        if (currentStudentCount < minStudents) {
                            minStudents = currentStudentCount;
                            bestTeacherID = teacherSnap.getKey();
                        }
                    }
                }
                if (bestTeacherID != null) {
                    assignTeacher(bestTeacherID);
                } else {
                    handleError("Sorry, this slot was just booked by someone else.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleError("Booking failed: " + error.getMessage());
            }
        });
    }

    private boolean isTeacherSuitable(DataSnapshot teacherSnap) {
        if (selectedTimeSlot == null || selectedClass == null) return false;

        DataSnapshot classesSnapshot = teacherSnap.child("classes");
        DataSnapshot timeSlotsSnapshot = teacherSnap.child("timeSlots");
        DataSnapshot studentsCountSnapshot = teacherSnap.child("studentsCount").child(selectedTimeSlot);

        if (!classesSnapshot.exists() || !timeSlotsSnapshot.exists() || !studentsCountSnapshot.exists()) return false;

        boolean teachesClass = false;
        String classToCompare = selectedClass.replace("Class ", "");
        for (DataSnapshot classSnap : classesSnapshot.getChildren()) {
            String classValue = classSnap.getValue(String.class);
            if (classValue != null && classValue.equals(classToCompare)) {
                teachesClass = true;
                break;
            }
        }
        boolean availableAtSlot = false;
        for (DataSnapshot slotSnap : timeSlotsSnapshot.getChildren()) {
            String slotValue = slotSnap.getValue(String.class);
            if (slotValue != null && slotValue.equals(selectedTimeSlot)) {
                availableAtSlot = true;
                break;
            }
        }
        int studentCount = studentsCountSnapshot.getValue(Integer.class) != null ? studentsCountSnapshot.getValue(Integer.class) : 0;
        return teachesClass && availableAtSlot && studentCount < 10;
    }

    private void assignTeacher(String teacherID) {
        DatabaseReference teacherStudentCountRef = databaseReference.child("teachers").child(teacherID).child("studentsCount").child(selectedTimeSlot);
        teacherStudentCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.getValue(Integer.class);
                if (count == null) count = 0;
                if (count < 10) {
                    currentData.setValue(count + 1);
                    return Transaction.success(currentData);
                } else {
                    return Transaction.abort();
                }
            }
            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    handleError("Database error: " + error.getMessage());
                    return;
                }
                if (committed) {
                    saveAllocationToDatabase(teacherID);
                } else {
                    handleError("Slot just got full. Please select another time.");
                }
            }
        });
    }

    private void saveAllocationToDatabase(String teacherID) {
        String randomKey = databaseReference.child("allocated_classes").push().getKey();
        if (randomKey == null) {
            handleError("Could not create booking record.");
            return;
        }
        databaseReference.child("Users").child(currentUserID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String studentName = snapshot.exists() ? snapshot.getValue(String.class) : "New Student";
                HashMap<String, Object> allocationData = new HashMap<>();
                allocationData.put("teacherID", teacherID);
                allocationData.put("demoID", randomKey);
                allocationData.put("className", selectedClass);
                allocationData.put("timeSlot", selectedTimeSlot);
                allocationData.put("date", selectedDate);
                allocationData.put("studentID", currentUserID);
                allocationData.put("studentName", studentName);
                allocationData.put("paymentStatus", "booked");
                allocationData.put("bookingTimestamp", System.currentTimeMillis());

                databaseReference.child("allocated_classes").child(randomKey).setValue(allocationData)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(demoActivity2.this, "Your session is booked!", Toast.LENGTH_LONG).show();
                            logAnalyticsEvents(randomKey);
                            navigateToMainScreen();
                        })
                        .addOnFailureListener(e -> handleError("Failed to save booking: " + e.getMessage()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleError("Error fetching user name: " + error.getMessage());
            }
        });
    }

    private void handleError(String message) {
        Log.e(DEBUG_TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
    }

    private void navigateToMainScreen() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void logAnalyticsEvents(String demoId) {
        try {
            // Firebase Analytics: Custom "Schedule" event
            Bundle firebaseBundle = new Bundle();
            firebaseBundle.putString("demo_id", demoId);
            firebaseBundle.putString("class_name", selectedClass);
            firebaseBundle.putString("time_slot", selectedTimeSlot);
            mFirebaseAnalytics.logEvent("demo_session_booked", firebaseBundle);
            Log.d(DEBUG_TAG, "Successfully logged 'demo_session_booked' to Firebase.");

            // Meta (Facebook) SDK: Standard "Schedule" event
            Bundle metaParams = new Bundle();
            metaParams.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, demoId);
            metaParams.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "1-on-1-demo");
            metaLogger.logEvent(AppEventsConstants.EVENT_NAME_SCHEDULE, metaParams);
            Log.d(DEBUG_TAG, "Successfully logged 'Schedule' event to Meta SDK.");

        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Error logging analytics events", e);
        }
    }


    private void loadTimeSlots() {
        timeSlotContainer.removeAllViews();
        String[] timeSlots = {"6-7 AM", "7-8 AM", "8-9 AM", "9-10 AM", "10-11 AM", "11-12 AM", "12-1 PM",
                "1-2 PM", "2-3 PM", "3-4 PM", "4-5 PM", "5-6 PM", "6-7 PM", "7-8 PM", "8-9 PM", "9-10 PM"};

        for (String slot : timeSlots) {
            TextView slotView = createTextView(slot);
            slotView.setOnClickListener(v -> {
                if (selectedTimeSlotView != null) {
                    selectedTimeSlotView.setBackgroundResource(R.drawable.signbg);
                }
                selectedTimeSlotView = slotView;
                selectedTimeSlot = slot;
                slotView.setBackgroundResource(R.drawable.click_bg2);
            });
            timeSlotContainer.addView(slotView);
        }
    }



//
//    // --- UI Helper Methods ---
//    private void loadTimeSlots() {
//        timeSlotContainer.removeAllViews();
//        String[] timeSlots = {"6-7 AM", "7-8 AM", "8-9 AM", "9-10 AM", "10-11 AM", "11-12 AM", "12-1 PM",
//                "1-2 PM", "2-3 PM", "3-4 PM", "4-5 PM", "5-6 PM", "6-7 PM", "7-8 PM", "8-9 PM", "9-10 PM"};
//        for (String slot : timeSlots) {
//            TextView slotView = createTextView(slot);
//            slotView.setOnClickListener(v -> {
//                if (selectedTimeSlotView != null) {
//                    selectedTimeSlotView.setBackgroundResource(R.drawable.signbg);
//                }
//                selectedTimeSlotView = slotView;
//                selectedTimeSlot = slot;
//                slotView.setBackgroundResource(R.drawable.click_bg2);
//            });
//            timeSlotContainer.addView(slotView);
//        }
//    }

    private void loadClasses() {
        classContainer.removeAllViews();
        String[] classes = {
                "Preschool", "Kindergarten",
                "Grade 1", "Grade 2", "Grade 3", "Grade 4", "Grade 5",
                "Grade 6", "Grade 7", "Grade 8"
        };
        for (String cls : classes) {
            TextView classView = createTextView(cls);
            classView.setOnClickListener(v -> {
                if (selectedClassView != null) {
                    selectedClassView.setBackgroundResource(R.drawable.signbg);
                }
                selectedClassView = classView;
                selectedClass = cls;
                classView.setBackgroundResource(R.drawable.click_bg2);
            });
            classContainer.addView(classView);
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 8, 16, 8);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.signbg);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(32, 24, 32, 24);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        return textView;
    }
}