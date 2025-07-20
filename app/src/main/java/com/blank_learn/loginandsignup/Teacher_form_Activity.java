package com.blank_learn.loginandsignup;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.blank_learn.payment.PaymentActivity_teacher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Teacher_form_Activity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private GridLayout classGrid, timeSlotGrid;
    private Button uploadButton;
    private Button UploadButton_video;
    private DatabaseReference databaseReference;
    private ProgressBar progressBarCheck;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDatabaseRef;
    private FirebaseUser currentUser;

    private StorageReference storageReference;

    private final String[] classes = {"LKG", "UKG", "1", "2", "3", "4", "5", "6", "7", "8"};
    private final String[] timeSlots = {
            "6-7 AM", "7-8 AM", "8-9 AM",
            "9-10 AM", "10-11 AM", "11-12 PM",
            "12-1 PM", "1-2 PM", "2-3 PM",
            "3-4 PM", "4-5 PM", "5-6 PM",
            "6-7 PM", "7-8 PM", "8-9 PM",
            "9-10 PM"
    };

    private List<CheckBox> classCheckBoxes = new ArrayList<>();
    private List<CheckBox> timeSlotCheckBoxes = new ArrayList<>();
    private Uri videoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_form);
        databaseReference = FirebaseDatabase.getInstance().getReference("teachers");
        storageReference = FirebaseStorage.getInstance().getReference("teacher_videos");
        classGrid = findViewById(R.id.classGrid);
        timeSlotGrid = findViewById(R.id.timeSlotGrid);
        uploadButton = findViewById(R.id.uploadButton);
        UploadButton_video = findViewById(R.id.button10);
        createCheckBoxes(classes, classGrid, classCheckBoxes);
        createCheckBoxes(timeSlots, timeSlotGrid, timeSlotCheckBoxes);
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        firebaseAuth = FirebaseAuth.getInstance();
        UploadButton_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openVideoPicker();
//                checkVerificationAndProceed();


                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser == null) {

                    return;
                }

                final String userId = currentUser.getUid();

                DatabaseReference userVerifyRef = userDatabaseRef.child(userId).child("verify");


                userVerifyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Boolean isVerified = dataSnapshot.getValue(Boolean.class);

                        if (dataSnapshot.exists() && Boolean.TRUE.equals(isVerified)) {
//                            uploadTeacherData();
                            openVideoPicker();

                        } else {

                            Intent intent = new Intent(Teacher_form_Activity.this, PaymentActivity_teacher.class);
                            startActivity(intent);
//                            Toast.makeText(v.getContext(), "Account not verified. Please verify your account to upload data.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
//            }
//        });


            }


        });
//        uploadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//                if (currentUser == null) {
//                    return;
//                }
//                final String userId = currentUser.getUid();
//                DatabaseReference userVerifyRef = databaseReference.child(userId).child("verify");
//                userVerifyRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        Boolean isVerified = dataSnapshot.getValue(Boolean.class);
//
//                        if (dataSnapshot.exists() && Boolean.TRUE.equals(isVerified)) {
//                            uploadTeacherData();
//
//                        } else {
//
//                            Intent intent = new Intent(Teacher_form_Activity.this, PaymentActivity_teacher.class);
//                            startActivity(intent);
//                        }
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//                });
//            }
//        });


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    return;
                }

                final String userId = currentUser.getUid();
                DatabaseReference userVerifyRef = userDatabaseRef.child(userId).child("verify");

                userVerifyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Boolean isVerified = dataSnapshot.getValue(Boolean.class);


                        if (Boolean.TRUE.equals(isVerified)) {
                            uploadTeacherData();
                        } else {
                            Intent intent = new Intent(Teacher_form_Activity.this, PaymentActivity_teacher.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FirebaseError", "Error checking verification", databaseError.toException());
                    }
                });
            }
        });

    }


    private void openVideoPicker() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData(); // Get the selected video URI
            Toast.makeText(this, "Video selected!", Toast.LENGTH_SHORT).show();
        }
    }

    // Create checkboxes dynamically
    private void createCheckBoxes(String[] items, GridLayout gridLayout, List<CheckBox> checkBoxList) {
        for (String item : items) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(item);
            checkBox.setTextSize(16);
            checkBoxList.add(checkBox);
            gridLayout.addView(checkBox);
        }
    }

    private void uploadTeacherData() {
        List<String> selectedClasses = new ArrayList<>();
        List<String> selectedTimeSlots = new ArrayList<>();
        Map<String, Integer> studentCounts = new HashMap<>();

        // Get selected classes
        for (CheckBox checkBox : classCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedClasses.add(checkBox.getText().toString());
            }
        }

        // Get selected time slots
        for (CheckBox checkBox : timeSlotCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedTimeSlots.add(checkBox.getText().toString());
                studentCounts.put(checkBox.getText().toString(), 0);
            }
        }

        // Validate selections
        if (selectedClasses.isEmpty()) {
            Toast.makeText(this, "Please select at least one class!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTimeSlots.size() > 8) {
            Toast.makeText(this, "You can select only up to 8 time slots!", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedTimeSlots.isEmpty()) {
            Toast.makeText(this, "Please select at least one time slot!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique teacher ID
        String teacherId = FirebaseAuth.getInstance().getUid();

        // Upload video if selected
        if (videoUri != null) {
            uploadVideo(teacherId, selectedClasses, selectedTimeSlots, studentCounts);
        } else {
            // If no video is selected, upload only teacher data
            uploadTeacherDataToDatabase(teacherId, selectedClasses, selectedTimeSlots, studentCounts, null);
        }
    }

    // Upload video to Firebase Storage
    private void uploadVideo(String teacherId, List<String> selectedClasses, List<String> selectedTimeSlots, Map<String, Integer> studentCounts) {
        StorageReference videoRef = storageReference.child(teacherId).child(videoUri.getLastPathSegment());

        videoRef.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get video download URL
                        videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Upload teacher data with video URL
                                uploadTeacherDataToDatabase(teacherId, selectedClasses, selectedTimeSlots, studentCounts, downloadUri.toString());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Teacher_form_Activity.this, "Failed to upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Upload teacher data to Firebase Realtime Database
    private void uploadTeacherDataToDatabase(String teacherId, List<String> selectedClasses, List<String> selectedTimeSlots, Map<String, Integer> studentCounts, String videoUrl) {
        Map<String, Object> teacherData = new HashMap<>();
        teacherData.put("name", "John Doe"); // Replace with actual teacher name input
        teacherData.put("classes", selectedClasses);
        teacherData.put("timeSlots", selectedTimeSlots);
        teacherData.put("studentsCount", studentCounts);

        if (videoUrl != null) {
            teacherData.put("videoUrl", videoUrl); // Add video URL if available
        }

        databaseReference.child(teacherId).setValue(teacherData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Teacher data uploaded successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}