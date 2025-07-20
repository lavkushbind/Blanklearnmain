package com.blank_learn.loginandsignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

// Import your R file
import com.blank_learn.dark.R;

// Replace with your actual MainActivity or target activity after login
// !! IMPORTANT: Replace this with your actual MainActivity class !!
import com.blank_learn.home.MainActivity;


import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException; // Import specific exception
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class NumberSignupActivity extends AppCompatActivity {

    // Define a TAG for logging
    private static final String TAG = "NumberSignupActivity";

    // UI Elements
    private EditText editTextPhone, editTextOtp;
    private Button buttonSendOtp, buttonVerifyOtp;
    private ProgressBar progressBar;
    private LinearLayout layoutSendOtp, layoutVerifyOtp;
    private TextView textViewOtpInfo;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_signup);
        Log.d(TAG, "onCreate: Activity started.");

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: Firebase Auth instance obtained.");

        // Initialize Views
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOtp = findViewById(R.id.editTextOtp);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        progressBar = findViewById(R.id.progressBar);
        layoutSendOtp = findViewById(R.id.layoutSendOtp);
        layoutVerifyOtp = findViewById(R.id.layoutVerifyOtp);
        textViewOtpInfo = findViewById(R.id.textViewOtpInfo);
        Log.d(TAG, "onCreate: UI Views initialized.");

        // Initialize Phone Auth callbacks
        initFirebaseCallbacks();
        Log.d(TAG, "onCreate: Firebase callbacks initialized.");

        // Set Button Click Listeners
        buttonSendOtp.setOnClickListener(v -> {
            Log.i(TAG, "Send OTP Button Clicked!");
            // --- Rate Limiting: Disable button immediately ---
            setSendOtpButtonEnabled(false);
            sendVerificationCode();
            // --- Button will be re-enabled in callbacks on failure ---
        });

        buttonVerifyOtp.setOnClickListener(v -> {
            Log.i(TAG, "Verify OTP Button Clicked!");
            verifyCode();
        });

        Log.d(TAG, "onCreate: Button listeners set.");

        // Initial UI State
        progressBar.setVisibility(View.GONE);
        layoutSendOtp.setVisibility(View.VISIBLE);
        layoutVerifyOtp.setVisibility(View.GONE);
        setSendOtpButtonEnabled(true); // Ensure button is enabled initially
        Log.d(TAG, "onCreate: Initial UI state set. Send OTP button enabled.");
    }

    private void initFirebaseCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.i(TAG, "onVerificationCompleted: Auto-verification successful. Credential: " + credential);
                hideProgressBar();
                // Consider disabling Send OTP button here too if auto-verification happens
                // setSendOtpButtonEnabled(false);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "onVerificationFailed: Verification failed.", e); // Log the full exception
                hideProgressBar();
                // Show phone input layout again on failure
                layoutSendOtp.setVisibility(View.VISIBLE);
                layoutVerifyOtp.setVisibility(View.GONE);

                // --- Rate Limiting: Re-enable Send OTP button on failure ---
                Log.d(TAG, "onVerificationFailed: Re-enabling Send OTP button.");
                setSendOtpButtonEnabled(true);

                // Provide specific feedback to the user
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    editTextPhone.setError("Invalid phone number format (include '+', country code, and number)");
                    Toast.makeText(NumberSignupActivity.this, "Invalid phone number format.", Toast.LENGTH_LONG).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // SMS quota exceeded - THIS IS THE KEY ERROR HANDLER FOR QUOTA
                    Toast.makeText(NumberSignupActivity.this, "SMS quota exceeded. Please wait a while and try again.", Toast.LENGTH_LONG).show();
                    Log.w(TAG, "onVerificationFailed: FirebaseTooManyRequestsException occurred.");
                    // Consider adding a longer delay or specific instructions here
                } else {
                    // Generic error
                    Toast.makeText(NumberSignupActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                // Clear potentially invalid verification ID if verification failed early
                mVerificationId = null;
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.i(TAG, "onCodeSent: Code sent successfully. Verification ID: " + verificationId);
                hideProgressBar();

                // Save verification ID and resending token
                mVerificationId = verificationId;
                mResendToken = token;

                // Update UI to show OTP input field
                layoutSendOtp.setVisibility(View.GONE);
                layoutVerifyOtp.setVisibility(View.VISIBLE);
                textViewOtpInfo.setText("Enter the 6-digit OTP sent to " + editTextPhone.getText().toString());
                editTextOtp.requestFocus();
                Toast.makeText(NumberSignupActivity.this, "OTP Sent Successfully.", Toast.LENGTH_SHORT).show();

                // --- Rate Limiting: Keep Send OTP button DISABLED here ---
                // The user's next action is to enter the OTP, not resend immediately.
                Log.d(TAG, "onCodeSent: Keeping Send OTP button disabled.");
            }
        };
        Log.d(TAG, "initFirebaseCallbacks: mCallbacks object created.");
    }

    private void sendVerificationCode() {
        Log.d(TAG, "sendVerificationCode: Method entered.");
        String phoneNumber = editTextPhone.getText().toString().trim();
        Log.d(TAG, "sendVerificationCode: Phone number entered: '" + phoneNumber + "'");

        // --- Basic Phone Number Validation ---
        if (phoneNumber.isEmpty()) {
            Log.w(TAG, "sendVerificationCode: Phone number is empty.");
            editTextPhone.setError("Phone number cannot be empty.");
            editTextPhone.requestFocus();
            hideProgressBar(); // Hide progress if it was shown briefly
            setSendOtpButtonEnabled(true); // Re-enable button on validation failure
            return;
        }
        if (!phoneNumber.startsWith("+")) {
            Log.w(TAG, "sendVerificationCode: Phone number does not start with '+'.");
            editTextPhone.setError("Include country code starting with '+' (e.g., +16505551234)");
            editTextPhone.requestFocus();
            hideProgressBar();
            setSendOtpButtonEnabled(true); // Re-enable button on validation failure
            return;
        }
        // Add more robust validation if needed (length, digits only after '+')
        // Example: if (phoneNumber.length() < 10) { ... }

        Log.d(TAG, "sendVerificationCode: Phone number validation passed.");
        showProgressBar();
        // Hide input layouts while processing (Send OTP layout already hidden or will be)
        layoutVerifyOtp.setVisibility(View.GONE); // Ensure verify layout is hidden

        // !! IMPORTANT: For development, add Test Phone Numbers in Firebase Console !!
        // !! to avoid hitting SMS Quotas: Authentication -> Settings -> Phone numbers !!
        // !! Example: +1 650 555 1234 / Code: 654321                     !!

        try {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            // Optional: Uncomment if implementing resend functionality
                            // .setForceResendingToken(mResendToken)
                            .build();
            Log.d(TAG, "sendVerificationCode: PhoneAuthOptions built.");
            PhoneAuthProvider.verifyPhoneNumber(options);
            Log.i(TAG, "sendVerificationCode: Verification request initiated for number: " + phoneNumber);
            // Progress bar visibility is handled by the callbacks (onCodeSent/onVerificationFailed)

        } catch (Exception e) {
            // Catch potential errors during setup (less common but possible)
            Log.e(TAG, "sendVerificationCode: Error building options or starting verification", e);
            hideProgressBar();
            layoutSendOtp.setVisibility(View.VISIBLE); // Show phone input again
            setSendOtpButtonEnabled(true); // Re-enable button on setup error
            Toast.makeText(this, "Error starting verification process: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void verifyCode() {
        Log.d(TAG, "verifyCode: Method entered.");
        String code = editTextOtp.getText().toString().trim();
        Log.d(TAG, "verifyCode: OTP entered: '" + code + "'");

        // --- Basic OTP Validation ---
        if (code.isEmpty()) {
            Log.w(TAG, "verifyCode: OTP is empty.");
            editTextOtp.setError("OTP cannot be empty.");
            editTextOtp.requestFocus();
            return; // Don't show progress bar for basic validation errors
        }
        if (code.length() != 6) {
            Log.w(TAG, "verifyCode: OTP length is not 6.");
            editTextOtp.setError("Enter the full 6-digit OTP.");
            editTextOtp.requestFocus();
            return;
        }

        Log.d(TAG, "verifyCode: OTP validation passed.");
        showProgressBar();

        // --- Check if Verification ID exists ---
        if (mVerificationId != null && !mVerificationId.isEmpty()) {
            Log.d(TAG, "verifyCode: Verification ID found: " + mVerificationId);
            try {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                Log.d(TAG, "verifyCode: PhoneAuthCredential created.");
                signInWithPhoneAuthCredential(credential);
            } catch (IllegalArgumentException e) {
                // Should not happen with basic length check, but good practice
                Log.e(TAG, "verifyCode: Error creating credential (likely invalid format)", e);
                hideProgressBar();
                editTextOtp.setError("Invalid OTP format.");
                Toast.makeText(this, "Invalid OTP format.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // --- Handle missing verification ID ---
            Log.e(TAG, "verifyCode: Verification ID is missing or empty! Cannot verify code.");
            hideProgressBar();
            Toast.makeText(this, "Verification process error. Please request OTP again.", Toast.LENGTH_LONG).show();
            // Revert UI back to phone input state as verification cannot proceed
            layoutSendOtp.setVisibility(View.VISIBLE);
            layoutVerifyOtp.setVisibility(View.GONE);
            mVerificationId = null; // Clear potentially invalid ID

            // --- Rate Limiting: Re-enable Send OTP button as user needs to restart ---
            Log.d(TAG, "verifyCode: Re-enabling Send OTP button due to missing verification ID.");
            setSendOtpButtonEnabled(true);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: Attempting sign-in.");
        // Keep Send OTP button disabled during sign-in attempt
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    hideProgressBar(); // Hide progress bar regardless of outcome
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.i(TAG, "signInWithPhoneAuthCredential: Success!");
                        FirebaseUser user = task.getResult().getUser();
                        Toast.makeText(NumberSignupActivity.this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "signInWithPhoneAuthCredential: User UID: " + (user != null ? user.getUid() : "null"));
                        // Navigate to your main activity or dashboard
                        navigateToMainActivity();

                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithPhoneAuthCredential: Failure", task.getException());
                        // Keep OTP layout visible for retry
                        layoutVerifyOtp.setVisibility(View.VISIBLE);
                        // Keep Send OTP button disabled, user should retry OTP or need to resend

                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Log.w(TAG, "signInWithPhoneAuthCredential: Invalid OTP entered.");
                            editTextOtp.setError("Invalid OTP. Please check and try again.");
                            Toast.makeText(NumberSignupActivity.this, "Invalid OTP entered.", Toast.LENGTH_LONG).show();
                        } else {
                            // Other errors (network, server issue etc.)
                            Log.e(TAG,"signInWithPhoneAuthCredential: Error - ", task.getException());
                            Toast.makeText(NumberSignupActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                        editTextOtp.setText(""); // Clear the invalid OTP
                        editTextOtp.requestFocus(); // Focus back on OTP field
                    }
                });
    }

    private void navigateToMainActivity() {
        Log.d(TAG, "navigateToMainActivity: Navigating...");
        // !! Replace MainActivity.class with your actual main activity !!
        Intent intent = new Intent(NumberSignupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish(); // Finish this activity
        Log.d(TAG, "navigateToMainActivity: Navigation intent sent and activity finished.");
    }

    // --- Helper method for Send OTP Button State ---
    private void setSendOtpButtonEnabled(boolean enabled) {
        if (buttonSendOtp != null) {
            buttonSendOtp.setEnabled(enabled);
            // Optional: Change visual appearance when disabled (e.g., greyed out)
            buttonSendOtp.setAlpha(enabled ? 1.0f : 0.5f);
            Log.d(TAG, "setSendOtpButtonEnabled: Button state set to " + enabled);
        } else {
            Log.w(TAG, "setSendOtpButtonEnabled: buttonSendOtp is null!");
        }
    }


    // --- Helper methods for progress bar visibility ---
    private void showProgressBar() {
        if (progressBar != null) {
            Log.d(TAG, "showProgressBar: Making progress bar visible.");
            progressBar.setVisibility(View.VISIBLE);
        } else {
            Log.w(TAG, "showProgressBar: progressBar is null!");
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            Log.d(TAG, "hideProgressBar: Making progress bar gone.");
            progressBar.setVisibility(View.GONE);
        } else {
            Log.w(TAG, "hideProgressBar: progressBar is null!");
        }
    }

    // Optional: Add onStart checks or other lifecycle methods if needed
    // @Override
    // protected void onStart() { ... }
}