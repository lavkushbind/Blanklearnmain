package com.blank_learn.newversion.O1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.airbnb.lottie.LottieAnimationView;
import com.blank_learn.dark.R;

// ANALYTICS LIBRARIES
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
// END ANALYTICS LIBRARIES

import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// DESTINATION ACTIVITIES
import com.blank_learn.newversion.O2.MainActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";

    // SHARED PREFERENCES CONSTANTS
    public static final String APP_PREFS = "app_preferences";
    public static final String KEY_FIRST_LAUNCH = "is_first_launch";

    private enum UiState { PHONE_INPUT, OTP_INPUT, LOADING, VERIFIED }

    private TextInputEditText etPhoneNumber, etOtp;
    private TextInputLayout tilPhoneNumber, tilOtp;
    private AppCompatButton btnSendOtp, btnVerifyOtp, btnGoogleSignIn;
    private LinearLayout layoutSendOtp, layoutVerifyOtp, dividerLayout;
    private ProgressBar progressBar;
    private LottieAnimationView animationView;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef; // Reference for User Profiles
    private GoogleSignInClient mGoogleSignInClient;
    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    // Analytics
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ActivityResultLauncher<IntentSenderRequest> phoneHintLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        // Initialize Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        FacebookSdk.fullyInitialize();
        fbLogger = AppEventsLogger.newLogger(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        initViews();
        setupClickListeners();
        setupGoogleSignIn();
        setupVerificationCallbacks();
        setupActivityLaunchers();

        // Auto-fetch phone number hint
        requestPhoneNumberHint();
    }

    // =========================================================================
    // CORE LOGIC: DATABASE CHECK & NAVIGATION
    // =========================================================================

    private void updateUserInDatabase(String registrationMethod) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Authentication error.", Toast.LENGTH_SHORT).show();
            updateUiState(UiState.PHONE_INPUT);
            return;
        }

        String uid = firebaseUser.getUid();

        // Check if User Profile exists in "Users" node
        usersRef.child(uid).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Database read failed", task.getException());
                updateUiState(UiState.PHONE_INPUT);
                return;
            }

            if (task.getResult().exists()) {
                // === SCENARIO 1: OLD USER (Profile exists) ===
                Log.d(TAG, "User exists. Checking subscription status to decide route...");
                // Ab check karo ki banda Paid hai ya nahi (SplashActivity logic match karne ke liye)
                checkSubscriptionAndRedirect(uid);

            } else {
                // === SCENARIO 2: NEW USER (Creating Profile) ===
                logSignUpEvent(registrationMethod);
                Log.d(TAG, "New user detected. Creating profile...");

                Map<String, Object> newUserMap = new HashMap<>();
                newUserMap.put("uid", uid);
                newUserMap.put("creationTimestamp", System.currentTimeMillis());
                newUserMap.put("registrationMethod", registrationMethod);

                if (firebaseUser.getDisplayName() != null) newUserMap.put("name", firebaseUser.getDisplayName());
                if (firebaseUser.getEmail() != null) newUserMap.put("email", firebaseUser.getEmail());
                if (firebaseUser.getPhoneNumber() != null) newUserMap.put("phone", firebaseUser.getPhoneNumber());
                if (firebaseUser.getPhotoUrl() != null) newUserMap.put("profilepic", firebaseUser.getPhotoUrl().toString());

                usersRef.child(uid).setValue(newUserMap).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        // New User hamesha FREE wale home par jayega
                        navigateToHome(MainActivity.class);
                    } else {
                        updateUiState(UiState.PHONE_INPUT);
                        Toast.makeText(this, "Failed to save user.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Helper to check subscription status from "user_subscriptions" node
    private void checkSubscriptionAndRedirect(String userId) {
        DatabaseReference subRef = FirebaseDatabase.getInstance().getReference("user_subscriptions").child(userId);

        subRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String orderStatus = null;
                if (snapshot.exists()) {
                    orderStatus = snapshot.child("orderStatus").getValue(String.class);
                }

                if (orderStatus != null && orderStatus.equals("paid")) {
                    // User Login hai aur PAID hai -> NewHomeActivity
                    Log.d(TAG, "User is PAID -> Redirecting to NewHomeActivity");
                    navigateToHome(MainActivity.class);
                } else {
                    // User Login hai par FREE hai -> NewActivity
                    Log.d(TAG, "User is FREE -> Redirecting to NewActivity");
                    navigateToHome(MainActivity.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error aane par safe option (Free Home)
                navigateToHome(MainActivity.class);
            }
        });
    }

    private void navigateToHome(Class<?> targetActivity) {
        // First Launch Flag False karna
        setFirstLaunchFlagToFalse();

        updateUiState(UiState.VERIFIED);

        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setFirstLaunchFlagToFalse() {
        SharedPreferences prefs = getSharedPreferences(APP_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }

    // =========================================================================
    // AUTHENTICATION METHODS (Google & Phone)
    // =========================================================================

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        updateUiState(UiState.LOADING);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUserInDatabase("phone");
                    } else {
                        updateUiState(UiState.OTP_INPUT);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            tilOtp.setError("Invalid OTP.");
                        } else {
                            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        updateUserInDatabase("google");
                    } else {
                        updateUiState(UiState.PHONE_INPUT);
                        Toast.makeText(this, "Google Auth Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationCode() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        tilPhoneNumber.setError(null);

        if (phoneNumber.isEmpty() || phoneNumber.length() != 10) {
            tilPhoneNumber.setError("Enter valid 10-digit number");
            return;
        }

        updateUiState(UiState.LOADING);
        String finalPhoneNumber = "+91" + phoneNumber;
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(finalPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = etOtp.getText().toString().trim();
        tilOtp.setError(null);

        if (code.isEmpty() || code.length() < 6) {
            tilOtp.setError("Enter 6-digit OTP");
            return;
        }

        updateUiState(UiState.LOADING);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithGoogle() {
        updateUiState(UiState.LOADING);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    // =========================================================================
    // INITIALIZATION & UI SETUP
    // =========================================================================

    private void initViews() {
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etOtp = findViewById(R.id.etOtp);
        tilPhoneNumber = findViewById(R.id.tilPhoneNumber);
        tilOtp = findViewById(R.id.tilOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        layoutSendOtp = findViewById(R.id.layoutSendOtp);
        layoutVerifyOtp = findViewById(R.id.layoutVerifyOtp);
        dividerLayout = findViewById(R.id.dividerLayout);
        progressBar = findViewById(R.id.progressBar);
        animationView = findViewById(R.id.animationView);
    }

    private void setupClickListeners() {
        btnSendOtp.setOnClickListener(v -> sendVerificationCode());
        btnVerifyOtp.setOnClickListener(v -> verifyCode());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupVerificationCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                updateUiState(UiState.PHONE_INPUT);
                Log.e(TAG, "Verification Failed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    tilPhoneNumber.setError("Invalid phone number request.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(PhoneAuthActivity.this, "Too many attempts. Try later.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PhoneAuthActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                updateUiState(UiState.OTP_INPUT);
                Toast.makeText(PhoneAuthActivity.this, "OTP Sent.", Toast.LENGTH_SHORT).show();
            }
        };
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
                            updateUiState(UiState.PHONE_INPUT);
                            Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        updateUiState(UiState.PHONE_INPUT);
                    }
                });

        phoneHintLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            String phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.getData());
                            if (phoneNumber != null) {
                                phoneNumber = phoneNumber.replaceAll("[\\s-()]", "");
                                if (phoneNumber.length() > 10) phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                                etPhoneNumber.setText(phoneNumber);
                            }
                        } catch (ApiException e) {
                            Log.e(TAG, "Phone Hint Failed", e);
                        }
                    }
                });
    }

    private void requestPhoneNumberHint() {
        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
        Identity.getSignInClient(this).getPhoneNumberHintIntent(request)
                .addOnSuccessListener(result -> {
                    try {
                        phoneHintLauncher.launch(new IntentSenderRequest.Builder(result.getIntentSender()).build());
                    } catch (Exception e) {
                        Log.e(TAG, "Hint intent failed", e);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get phone hint", e));
    }

    private void logSignUpEvent(String registrationMethod) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, registrationMethod);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);

        Bundle fbParams = new Bundle();
        fbParams.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, registrationMethod);
        fbLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, fbParams);
    }

    private void updateUiState(UiState state) {
        switch (state) {
            case LOADING:
                progressBar.setVisibility(View.VISIBLE);
                layoutSendOtp.setVisibility(View.GONE);
                layoutVerifyOtp.setVisibility(View.GONE);
                dividerLayout.setVisibility(View.GONE);
                btnGoogleSignIn.setVisibility(View.GONE);
                break;
            case PHONE_INPUT:
                progressBar.setVisibility(View.GONE);
                layoutSendOtp.setVisibility(View.VISIBLE);
                layoutVerifyOtp.setVisibility(View.GONE);
                dividerLayout.setVisibility(View.VISIBLE);
                btnGoogleSignIn.setVisibility(View.VISIBLE);
                break;
            case OTP_INPUT:
                progressBar.setVisibility(View.GONE);
                layoutSendOtp.setVisibility(View.GONE);
                layoutVerifyOtp.setVisibility(View.VISIBLE);
                dividerLayout.setVisibility(View.GONE);
                btnGoogleSignIn.setVisibility(View.GONE);
                etOtp.requestFocus();
                break;
            case VERIFIED:
                progressBar.setVisibility(View.GONE);
                break;
        }
    }
}
