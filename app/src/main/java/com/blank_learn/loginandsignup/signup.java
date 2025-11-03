package com.blank_learn.loginandsignup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieDrawable;
import com.blank_learn.dark.databinding.ActivitySignupBinding;
import com.blank_learn.home.MainActivity;
import com.blank_learn.home.demoActivity;
import com.blank_learn.home.demoActivity2;
import com.blank_learn.modelfast;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

// Step 1: Import the necessary Meta SDK classes
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

public class signup extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Check if user is already logged in
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(signup.this, MainActivity.class));
            finish();
            return; // Stop further execution of onCreate
        }

        // Set up listeners using View Binding (cleaner than findViewById)
        setupListeners();
    }

    private void setupListeners() {
        binding.golog.setOnClickListener(v -> {
            startActivity(new Intent(signup.this, login.class));
            finish();
        });

        binding.signupbtn.setOnClickListener(v -> {
            performSignup();
        });
    }

    private void performSignup() {
        String email = binding.emailbtn.getText().toString().trim();
        String pass = binding.pasbtn.getText().toString().trim();
        String name = binding.namebtn.getText().toString().trim();
        String phone = binding.phonebtn.getText().toString().trim();

        // --- Input Validation ---
        if (name.isEmpty()) {
            binding.namebtn.setError("Name is required");
            binding.namebtn.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            binding.phonebtn.setError("Phone number is required");
            binding.phonebtn.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            binding.emailbtn.setError("Email is required");
            binding.emailbtn.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            binding.pasbtn.setError("Password is required");
            binding.pasbtn.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            binding.pasbtn.setError("Password must be at least 6 characters");
            binding.pasbtn.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = task.getResult().getUser();
                if (firebaseUser == null) return;

                String id = firebaseUser.getUid();
                modelfast userModel = new modelfast(name, phone, email, pass);
                database.getReference().child("Users").child(id).setValue(userModel);

                // --- LOG EVENTS FOR SUCCESSFUL REGISTRATION ---

                // 1. Log to Firebase Analytics (your existing code, which is correct)
                Bundle firebaseBundle = new Bundle();
                firebaseBundle.putString(FirebaseAnalytics.Param.METHOD, "email");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, firebaseBundle);

                // 2. Log to Meta (Facebook) Analytics
                logMetaRegistrationEvent();

                // Navigate to the main activity
                Intent intent = new Intent(signup.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(signup.this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void logMetaRegistrationEvent() {
        // Step 2: Get a logger instance
        AppEventsLogger logger = AppEventsLogger.newLogger(this);

        // Step 3: Create a bundle for parameters (optional but recommended)
        Bundle params = new Bundle();
        // IMPORTANT: DO NOT include personally identifiable information like name, email, or phone.
        // "REGISTRATION_METHOD" is a standard, safe parameter.
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, "email");

        // Step 4: Log the standard "Completed Registration" event
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.profilimg.setRepeatCount(LottieDrawable.INFINITE);
        binding.profilimg.playAnimation();
    }

    @Override
    protected void onPause() {
        binding.profilimg.pauseAnimation();
        super.onPause();
    }
}
