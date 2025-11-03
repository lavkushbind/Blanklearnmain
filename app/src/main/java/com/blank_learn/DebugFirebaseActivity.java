package com.blank_learn;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DebugFirebaseActivity extends AppCompatActivity {

    private static final String TAG = "FirebaseDebug"; // Logcat में फिल्टर करने के लिए टैग

    private EditText editTextData;
    private Button buttonUpload;
    private TextView textViewStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_firebase);

        // UI एलिमेंट्स को इनिशियलाइज़ करें
        editTextData = findViewById(R.id.editTextData);
        buttonUpload = findViewById(R.id.buttonUpload);
        textViewStatus = findViewById(R.id.textViewStatus);

        Log.d(TAG, "Activity Created. Initializing Firebase...");
        updateStatus("Activity Initialized.");

        // Firebase इंस्टेंस को इनिशियलाइज़ करें
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Log.d(TAG, "Firebase instances obtained.");

        // बटन क्लिक लिसनर सेट करें
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDataToFirebase();
            }
        });
    }

    private void uploadDataToFirebase() {
        Log.d(TAG, "Upload button clicked. Starting upload process...");
        updateStatus("Upload process started...");

        // 1. ऑथेंटिकेशन चेक करें
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Authentication Check FAILED: User is not logged in.");
            updateStatus("Error: User not logged in!");
            Toast.makeText(this, "You must be logged in to upload data.", Toast.LENGTH_SHORT).show();
            return; // आगे नहीं बढ़ना
        }

        Log.i(TAG, "Authentication Check PASSED: User is logged in. UID: " + currentUser.getUid());

        // 2. EditText से टेक्स्ट प्राप्त करें
        String dataToUpload = editTextData.getText().toString().trim();
        if (dataToUpload.isEmpty()) {
            Log.w(TAG, "Validation FAILED: EditText is empty.");
            updateStatus("Error: Input text is empty.");
            Toast.makeText(this, "Please enter some text.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Data to upload: '" + dataToUpload + "'");

        // 3. Firebase Realtime Database में डेटा लिखें
        // हम एक 'debug_uploads' नोड में डेटा सेव करेंगे, जिसमें यूज़र का UID होगा
        DatabaseReference debugRef = mDatabase.child("debug_uploads").child(currentUser.getUid());

        Log.d(TAG, "Database reference created at path: " + debugRef.toString());
        updateStatus("Uploading to database...");

        debugRef.child("last_message").setValue(dataToUpload)
                .addOnSuccessListener(aVoid -> {
                    // जब डेटा सफलतापूर्वक लिख दिया जाता है
                    Log.i(TAG, "SUCCESS: Data successfully written to Firebase.");
                    updateStatus("Success! Data uploaded.");
                    Toast.makeText(DebugFirebaseActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // जब डेटा लिखने में कोई एरर आती है
                    Log.e(TAG, "FAILURE: Failed to write data to Firebase.", e); // 'e' एरर की पूरी जानकारी देगा
                    updateStatus("Failure! Check Logcat for details.");
                    Toast.makeText(DebugFirebaseActivity.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // UI पर स्टेटस दिखाने के लिए एक हेल्पर मेथड
    private void updateStatus(String status) {
        if (textViewStatus != null) {
            textViewStatus.setText("Status: " + status);
        }
    }
}