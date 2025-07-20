package com.blank_learn; // The package for your Activity

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.blank_learn.dark.R;
import com.blank_learn.home.MainActivity;
import com.blank_learn.loginandsignup.Users;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {
    public static final String SIGNUP_DEBUG_TAG = "SIGNUP_ACTIVITY_DEBUG";
    private static final String TAG = "AuthActivity";

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReference;
    private EditText etPhoneNumber, etOtp;
    private Button btnSendOtp, btnVerifyOtp, btnGoogleSignIn;
    private LinearLayout layoutSendOtp, layoutVerifyOtp;
    private ProgressBar progressBar;
    private LottieAnimationView profilimg;
    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ActivityResultLauncher<IntentSenderRequest> phoneHintLauncher;


    // All methods up to `updateUserInDatabase` are the same...
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        initViews();
        setupClickListeners();
        setupGoogleSignIn();
        setupVerificationCallbacks();
        setupActivityLaunchers();

        requestPhoneNumberHint();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            navigateToHome();
        }
    }
    private void initViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        profilimg = findViewById(R.id.profilimg);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        layoutSendOtp = findViewById(R.id.layoutSendOtp);
        layoutVerifyOtp = findViewById(R.id.layoutVerifyOtp);
        progressBar = findViewById(R.id.progressBar);
    }
    private void setupClickListeners() {
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnSendOtp.setOnClickListener(v -> sendVerificationCode());
        btnVerifyOtp.setOnClickListener(v -> verifyCode());
    }
    private void setupActivityLaunchers() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            showLoading(false);
                            Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        Log.d(TAG, "Google sign in cancelled by user.");
                    }
                });
        phoneHintLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            String phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.getData());
                            if (phoneNumber != null && phoneNumber.startsWith("+91")) {
                                phoneNumber = phoneNumber.substring(3);
                            }
                            etPhoneNumber.setText(phoneNumber);
                            sendVerificationCode();
                        } catch (ApiException e) {
                            Log.e(TAG, "Phone hint failed to be retrieved.", e);
                        }
                    }
                });
    }
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signInWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        logAllRegistrationEvents("google");
                        updateUserInDatabase("google");
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void requestPhoneNumberHint() {
        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
        Identity.getSignInClient(this)
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener(result -> {
                    try {
                        IntentSender intentSender = result.getIntentSender();
                        phoneHintLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't start phone number hint intent", e);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get phone number hint intent", e));
    }
    private void sendVerificationCode() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            etPhoneNumber.setError("Enter a valid 10-digit phone number");
            etPhoneNumber.requestFocus();
            return;
        }
        showLoading(true);
        layoutSendOtp.setVisibility(View.GONE);
        Toast.makeText(this, "Requesting OTP for +91 " + phoneNumber, Toast.LENGTH_SHORT).show();
        String finalPhoneNumber = "+91" + phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(finalPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void setupVerificationCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted: Verification was automatic!");
                Toast.makeText(PhoneAuthActivity.this, "Verified Automatically!", Toast.LENGTH_SHORT).show();
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                showLoading(false);
                layoutSendOtp.setVisibility(View.VISIBLE);
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    etPhoneNumber.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(PhoneAuthActivity.this, "Quota exceeded. Try again later.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PhoneAuthActivity.this, "Verification failed.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent: Manual OTP flow started.");
                mVerificationId = verificationId;
                showLoading(false);
                layoutSendOtp.setVisibility(View.GONE);
                btnGoogleSignIn.setVisibility(View.GONE);
                layoutVerifyOtp.setVisibility(View.VISIBLE);
                etOtp.requestFocus();
                Toast.makeText(PhoneAuthActivity.this, "OTP Sent. Please enter it manually.", Toast.LENGTH_LONG).show();
            }
        };
    }
    private void verifyCode() {
        String code = etOtp.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            etOtp.setError("Enter valid OTP");
            return;
        }
        showLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showLoading(true);
        layoutVerifyOtp.setVisibility(View.GONE);
        Toast.makeText(this, "Signing you in...", Toast.LENGTH_SHORT).show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        logAllRegistrationEvents("phone");
                        updateUserInDatabase("phone");
                    } else {
                        showLoading(false);
                        layoutVerifyOtp.setVisibility(View.VISIBLE);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            etOtp.setError("The OTP is incorrect.");
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * MAJOR REWRITE: This method now uses HashMaps to write data.
     * This prevents saving empty fields to the database for new users.
     */
    private void updateUserInDatabase(String registrationMethod) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference userNode = databaseReference.child(uid);

        userNode.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Database read failed", task.getException());
                showLoading(false);
                Toast.makeText(this, "Database error. Could not save user.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (task.getResult().exists()) {
                // --- CASE 1: USER ALREADY EXISTS ---
                // We only update the core details that might change upon re-login.
                // We use `updateChildren` so we don't wipe out fields the user set themselves (like bio).
                Log.d(TAG, "User " + uid + " exists. Updating info.");
                Map<String, Object> updates = new HashMap<>();

                if (firebaseUser.getDisplayName() != null) {
                    updates.put("name", firebaseUser.getDisplayName());
                }
                if (firebaseUser.getEmail() != null) {
                    updates.put("email", firebaseUser.getEmail());
                }
                if (firebaseUser.getPhoneNumber() != null) {
                    updates.put("phone", firebaseUser.getPhoneNumber());
                }
                if (firebaseUser.getPhotoUrl() != null) {
                    updates.put("profilepic", firebaseUser.getPhotoUrl().toString());
                }

                // Only perform an update if there's something to update
                if (!updates.isEmpty()) {
                    userNode.updateChildren(updates).addOnCompleteListener(dbTask -> handleDbWriteCompletion(dbTask));
                } else {
                    // Nothing to update, just proceed
                    handleDbWriteCompletion(null);
                }

            }
            else {
                // --- CASE 2: THIS IS A NEW USER ---
                // We create a new user object with only the available data and essential defaults.
                // We use `setValue` because we are creating the entire node for the first time.
                Log.d(TAG, "New user: " + uid + ". Creating new entry.");
                Map<String, Object> newUserMap = new HashMap<>();

                // Essential fields that should always exist
                newUserMap.put("uid", uid);
                newUserMap.put("userID", uid);
                newUserMap.put("creationTimestamp", System.currentTimeMillis());
                newUserMap.put("registrationMethod", registrationMethod);
                newUserMap.put("followercount", 0);
                newUserMap.put("charge", 0L);
                newUserMap.put("verify", false);

                // Optional fields - only add them if they are not null/empty
                if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
                    newUserMap.put("name", firebaseUser.getDisplayName());
                }
                if (firebaseUser.getEmail() != null) {
                    newUserMap.put("email", firebaseUser.getEmail());
                }
                if (firebaseUser.getPhoneNumber() != null) {
                    newUserMap.put("phone", firebaseUser.getPhoneNumber());
                }
                if (firebaseUser.getPhotoUrl() != null) {
                    newUserMap.put("profilepic", firebaseUser.getPhotoUrl().toString());
                }

                userNode.setValue(newUserMap).addOnCompleteListener(dbTask -> handleDbWriteCompletion(dbTask));
            }
        });
    }

    /**
     * NEW HELPER METHOD to avoid repeating code. Handles the result of a database write.
     */
    private void handleDbWriteCompletion(Task<Void> dbTask) {
        // The task can be null if there were no updates to perform for an existing user.
        if (dbTask == null || dbTask.isSuccessful()) {
            Toast.makeText(this, "Sign-In Successful!", Toast.LENGTH_SHORT).show();
            navigateToHome();
        } else {
            showLoading(false);
            Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to write to database", dbTask.getException());
        }
    }


    // The rest of the helper methods remain the same...
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGoogleSignIn.setEnabled(!isLoading);
        btnSendOtp.setEnabled(!isLoading);
        btnVerifyOtp.setEnabled(!isLoading);
        etPhoneNumber.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
    }
    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void logAllRegistrationEvents(String registrationMethod) {
        Log.d(SIGNUP_DEBUG_TAG, "SUCCESS: >>> ABOUT TO SEND META SIGNUP EVENT NOW! <<<");

        Bundle firebaseBundle = new Bundle();
        firebaseBundle.putString(FirebaseAnalytics.Param.METHOD, registrationMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, firebaseBundle);
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle metaParams = new Bundle();
        metaParams.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, registrationMethod);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, metaParams);
        Log.d(TAG, "Logged SIGN_UP event for method: " + registrationMethod);
    }
    @Override
    protected void onResume() {
        super.onResume();
        profilimg.setRepeatCount(LottieDrawable.INFINITE);
        profilimg.playAnimation();
    }
    @Override
    protected void onPause() {
        profilimg.pauseAnimation();
        super.onPause();
    }
}


