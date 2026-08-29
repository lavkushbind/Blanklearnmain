package com.blank_learn.newversion.O2; // Check your package name

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class HelpSupportActivity extends AppCompatActivity {

    private EditText etSubject, etMessage;
    private View btnSubmit, btnBack;
    private FrameLayout progressOverlay;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("HelpRequests");

        // Init Views
        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnBack = findViewById(R.id.btnBack);
        progressOverlay = findViewById(R.id.progressOverlay);

        // Click Listeners
        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String subject = etSubject.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(subject)) {
            etSubject.setError("Required");
            return;
        }
        if (TextUtils.isEmpty(message)) {
            etMessage.setError("Required");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show Loading
        progressOverlay.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Prepare Data
        String requestId = dbRef.push().getKey();
        Map<String, Object> helpData = new HashMap<>();
        helpData.put("id", requestId);
        helpData.put("userId", user.getUid());
        helpData.put("userEmail", user.getEmail() != null ? user.getEmail() : "No Email");
        helpData.put("userPhone", user.getPhoneNumber() != null ? user.getPhoneNumber() : "No Phone");
        helpData.put("subject", subject);
        helpData.put("message", message);
        helpData.put("status", "Pending");
        helpData.put("timestamp", System.currentTimeMillis());

        // Send to Firebase
        if (requestId != null) {
            dbRef.child(requestId).setValue(helpData)
                    .addOnCompleteListener(task -> {
                        progressOverlay.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(HelpSupportActivity.this, "Request Sent Successfully!", Toast.LENGTH_LONG).show();
                            finish(); // Close Activity
                        } else {
                            Toast.makeText(HelpSupportActivity.this, "Failed to send request.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}