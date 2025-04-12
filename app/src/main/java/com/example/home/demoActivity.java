package com.example.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.blank_learn.dark.R;
import com.example.One_Signal.NotificationWorker;
import com.example.demo.AllocationListFragment;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.onesignal.OneSignal;


import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;


public class demoActivity extends AppCompatActivity {
    private CalendarView calendarView;
    String randomKey;
    FirebaseDatabase database;

    private LinearLayout timeSlotContainer, classContainer;
    private Button payButton;
    private DatabaseReference databaseReference;
    private String selectedDate, selectedTimeSlot, selectedClass;
    private TextView selectedClassView, selectedTimeSlotView;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        database= FirebaseDatabase.getInstance();
        calendarView = findViewById(R.id.calendar_view);
        timeSlotContainer = findViewById(R.id.time_slot_container);
        classContainer = findViewById(R.id.time_slot_container1);
        payButton = findViewById(R.id.next_button);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year
        );

        loadTimeSlots();
        loadClasses();
        payButton.setOnClickListener(v -> allocateTeacher());
    }

    private void loadTimeSlots() {
        String[] timeSlots = {"6-7 AM", "7-8 AM", "8-9 AM", "9-10 AM", "10-11 AM", "11-12 AM", "12-1 PM",
                "1-2 PM", "2-3 PM", "3-4 PM", "4-5 PM", "5-6 PM", "6-7 PM", "7-8 PM", "8-9 PM", "9-10 PM"};

        for (String slot : timeSlots) {
            TextView slotView = createTextView(slot);

            // Set margins for each TextView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 10, 10, 10);
            slotView.setLayoutParams(params);

            // Set default background
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
                    slotView.setBackground(ContextCompat.getDrawable(this, R.drawable.click_bg));
                }
            });

            timeSlotContainer.addView(slotView);
        }
    }

    private void loadClasses() {
        String[] classes = {"Class LKG", "Class UKG", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8"};

        for (String cls : classes) {
            TextView classView = createTextView(cls);

            // Set margins for each TextView
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(10, 10, 10, 10); // Left, Top, Right, Bottom margin
            classView.setLayoutParams(params);

            classView.setBackground(ContextCompat.getDrawable(this, R.drawable.signbg));

            classView.setOnClickListener(v -> {
                if (selectedClassView == classView) {
                    // If clicking the same class, reset to default background
                    classView.setBackground(ContextCompat.getDrawable(this, R.drawable.signbg));
                    selectedClassView = null;
                    selectedClass = null;
                } else {
                    if (selectedClassView != null) {
                        selectedClassView.setBackground(ContextCompat.getDrawable(this, R.drawable.signbg));
                    }
                    // Highlight the newly selected class
                    selectedClassView = classView;
                    selectedClass = cls;
                    classView.setBackground(ContextCompat.getDrawable(this, R.drawable.click_bg));
                }
            });

            classContainer.addView(classView);
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14); // Set text size to 16dp
        textView.setTextColor(Color.WHITE); // Set text color to white
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20, 20, 20, 20);
        return textView;
    }

    private void allocateTeacher() {


        if (selectedDate == null || selectedTimeSlot == null || selectedClass == null) {
            Toast.makeText(this, "Please select a date, time slot, and class", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("teachers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
                    assignTeacher(bestTeacherID);
                } else {
                    Toast.makeText(demoActivity.this, "No available teacher for this slot", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(demoActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void assignTeacher(String teacherID) {
        DatabaseReference teacherRef = databaseReference.child("teachers")
                .child(teacherID)
                .child("studentsCount")
                .child(selectedTimeSlot);

        teacherRef.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.getValue(Integer.class);
                if (count == null) count = 0;
                if (count < 3) {
                    currentData.setValue(count + 1);
                    return com.google.firebase.database.Transaction.success(currentData);
                } else {
                    return com.google.firebase.database.Transaction.abort();
                }
            }

            @Override
            public void onComplete(@androidx.annotation.Nullable DatabaseError error, boolean committed, @androidx.annotation.Nullable DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(demoActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (committed) {

                    saveAllocationToDatabase(teacherID);
                    Toast.makeText(demoActivity.this, "Teacher assigned successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(demoActivity.this, "Slot is full, please select another time.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    private void saveAllocationToDatabase(String teacherID) {
        randomKey = database.getReference().push().getKey();

        HashMap<String, Object> allocationData = new HashMap<>();
        DatabaseReference allocationRef = databaseReference.child("allocated_classes").child(randomKey);


        allocationData.put("teacherID", teacherID);
        allocationData.put("demoID", randomKey);
        allocationData.put("className", selectedClass);
        allocationData.put("timeSlot", selectedTimeSlot);
        allocationData.put("date", selectedDate);
        allocationData.put("studentID", currentUserID);
        allocationData.put("paymentStatus", "booked");

        allocationRef.setValue(allocationData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(demoActivity.this, "Allocation saved!", Toast.LENGTH_SHORT).show();

                    // Get teacher's OneSignal Player ID
                    databaseReference.child("users").child(teacherID).child("oneSignalPlayerId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String teacherPlayerId = dataSnapshot.getValue(String.class);

                                    if (teacherPlayerId != null) {
                                        // Send OneSignal Notification
                                        sendNotificationToTeacher(teacherPlayerId, selectedDate, selectedTimeSlot, selectedClass);
                                    } else {
                                        Log.w("DemoActivity", "Teacher has no OneSignal Player ID");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e("DemoActivity", "Error retrieving OneSignal ID: " + databaseError.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(demoActivity.this, "Failed to save allocation", Toast.LENGTH_SHORT).show()
                );
    }
    private void scheduleNotification(String teacherID, String studentID, String selectedDate, String selectedTimeSlot, String selectedClass) {
        databaseReference.child("users").child(teacherID).child("oneSignalPlayerId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String teacherPlayerId = dataSnapshot.getValue(String.class);

                        databaseReference.child("users").child(studentID).child("oneSignalPlayerId")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String studentPlayerId = dataSnapshot.getValue(String.class);

                                        if (teacherPlayerId != null && studentPlayerId != null) {
                                            Calendar demoTime = Calendar.getInstance();
                                            Calendar notificationTime = (Calendar) demoTime.clone();
                                            notificationTime.add(Calendar.HOUR_OF_DAY, -3); // Subtract 3 hours

                                            long delayInMillis = notificationTime.getTimeInMillis() - System.currentTimeMillis();
                                            Data inputData = new Data.Builder()
                                                    .putString("teacherPlayerId", teacherPlayerId)
                                                    .putString("studentPlayerId", studentPlayerId)
                                                    .putString("selectedDate", selectedDate)
                                                    .putString("selectedTimeSlot", selectedTimeSlot)
                                                    .putString("selectedClass", selectedClass)
                                                    .build();
                                            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                                                    .setInputData(inputData)
                                                    .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
                                                    .build();

                                            WorkManager.getInstance(demoActivity.this).enqueue(notificationWork);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("DemoActivity", "Error retrieving student OneSignal ID: " + databaseError.getMessage());
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DemoActivity", "Error retrieving teacher OneSignal ID: " + databaseError.getMessage());
                    }
                });
    }




    private void sendNotificationToTeacher(String teacherPlayerId, String selectedDate, String selectedTimeSlot, String selectedClass) {
        // OneSignal API URL
        String oneSignalApiUrl = "https://onesignal.com/api/v1/notifications";

        // OneSignal App ID
        String oneSignalAppId = "102b9dc4-8938-43bf-88b1-4df4b52d136d";

        // Notification content
        String notificationMessage = "You have a new demo class scheduled on " + selectedDate + " at " + selectedTimeSlot + " for " + selectedClass;

        // Create the JSON payload for the OneSignal API
        JSONObject notificationContent = new JSONObject();
        try {
            notificationContent.put("app_id", oneSignalAppId);
            notificationContent.put("include_player_ids", new JSONArray().put(teacherPlayerId));
            notificationContent.put("contents", new JSONObject().put("en", notificationMessage));
            notificationContent.put("headings", new JSONObject().put("en", "New Demo Class Booked"));
            notificationContent.put("data", new JSONObject().put("date", selectedDate).put("timeSlot", selectedTimeSlot).put("className", selectedClass));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("DemoActivity", "Error creating JSON payload: " + e.getMessage());
            return;
        }

        // Create a Volley request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a JSON object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, oneSignalApiUrl, notificationContent,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("DemoActivity", "Notification sent successfully: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("DemoActivity", "Error sending notification: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Add headers for OneSignal API
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic YOUR_ONESIGNAL_REST_API_KEY");
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }
}


