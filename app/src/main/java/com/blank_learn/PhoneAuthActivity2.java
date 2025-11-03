package com.blank_learn;

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
import android.widget.TextView;
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
import com.blank_learn.home.demoActivity2;
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
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneAuthActivity2 extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReference;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger logger;

    private EditText etPhoneNumber, etOtp;
    private Button btnSendOtp, btnVerifyOtp, btnGoogleSignIn;
    private LinearLayout layoutSendOtp, layoutVerifyOtp;
    private ProgressBar progressBar;
    private LottieAnimationView profilimg;
    private CountryCodePicker ccp;
    private TextView orSeparator;

    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private ActivityResultLauncher<IntentSenderRequest> phoneHintLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp2);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        logger = AppEventsLogger.newLogger(this);

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
        ccp = findViewById(R.id.ccp);
        orSeparator = findViewById(R.id.orSeparator);
        ccp.registerCarrierNumberEditText(etPhoneNumber);
    }

    private void setupClickListeners() {
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        btnSendOtp.setOnClickListener(v -> sendVerificationCode());
        btnVerifyOtp.setOnClickListener(v -> verifyCode());
    }

    private void sendVerificationCode() {
        if (!ccp.isValidFullNumber()) {
            etPhoneNumber.setError("Please enter a valid phone number");
            etPhoneNumber.requestFocus();
            return;
        }
        showLoading(true);
        String finalPhoneNumber = ccp.getFullNumberWithPlus();
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
                Log.d(TAG, "onVerificationCompleted: Auto-verification successful.");
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                showLoading(false);
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    etPhoneNumber.setError("Invalid phone number. Please check and try again.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(PhoneAuthActivity2.this, "Quota exceeded. Please try again later.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PhoneAuthActivity2.this, "Verification failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent: OTP sent to device.");
                mVerificationId = verificationId;
                showLoading(false);
                layoutSendOtp.setVisibility(View.GONE);
                layoutVerifyOtp.setVisibility(View.VISIBLE);
                btnGoogleSignIn.setVisibility(View.GONE);
                orSeparator.setVisibility(View.GONE);
                etOtp.requestFocus();
                Toast.makeText(PhoneAuthActivity2.this, "OTP has been sent.", Toast.LENGTH_LONG).show();
            }
        };
    }

    private void verifyCode() {
        String code = etOtp.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            etOtp.setError("Enter the 6-digit OTP");
            return;
        }
        showLoading(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Sign in successful!", Toast.LENGTH_SHORT).show();
                        updateUserInDatabase("phone");
                    } else {
                        showLoading(false);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            etOtp.setError("The code you entered is incorrect.");
                        } else {
                            Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
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
                        updateUserInDatabase("google");
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Google Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
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
                            showLoading(false);
                            Log.w(TAG, "Google sign in failed", e);
                        }
                    } else {
                        showLoading(false);
                    }
                });

        phoneHintLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            String phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.getData());
                            ccp.setFullNumber(phoneNumber);
                        } catch (ApiException e) {
                            Log.e(TAG, "Phone hint failed to be retrieved.", e);
                        }
                    }
                });
    }

    private void requestPhoneNumberHint() {
        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
        Identity.getSignInClient(this)
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener(result -> {
                    try {
                        phoneHintLauncher.launch(new IntentSenderRequest.Builder(result.getIntentSender()).build());
                    } catch (Exception e) {
                        Log.e(TAG, "Couldn't start phone number hint intent", e);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get phone number hint intent", e));
    }

    private void updateUserInDatabase(String registrationMethod) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            showLoading(false);
            return;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference userNode = databaseReference.child(uid);

        userNode.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Database read failed", task.getException());
                showLoading(false);
                return;
            }

            if (task.getResult().exists()) {
                navigateToDemo();
            } else {
                logRegistrationEvent(registrationMethod);

                Map<String, Object> newUserMap = new HashMap<>();
                newUserMap.put("uid", uid);
                newUserMap.put("userID", uid);
                newUserMap.put("creationTimestamp", System.currentTimeMillis());
                newUserMap.put("registrationMethod", registrationMethod);
                newUserMap.put("followercount", 0);
                newUserMap.put("charge", 0L);
                newUserMap.put("verify", false);
                // **** MODIFICATION HERE ****
                // Get the selected country name from the CountryCodePicker and add it to the map
                newUserMap.put("country", ccp.getSelectedCountryName());

                if (firebaseUser.getDisplayName() != null) newUserMap.put("name", firebaseUser.getDisplayName());
                if (firebaseUser.getEmail() != null) newUserMap.put("email", firebaseUser.getEmail());
                if (firebaseUser.getPhoneNumber() != null) newUserMap.put("phone", firebaseUser.getPhoneNumber());
                if (firebaseUser.getPhotoUrl() != null) newUserMap.put("profilepic", firebaseUser.getPhotoUrl().toString());

                userNode.setValue(newUserMap).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        navigateToDemo();
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void logRegistrationEvent(String method) {
        Log.d(TAG, "Logging SIGN UP event for method: " + method);

        Bundle firebaseBundle = new Bundle();
        firebaseBundle.putString(FirebaseAnalytics.Param.METHOD, method);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, firebaseBundle);

        Bundle metaParams = new Bundle();
        metaParams.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, method);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, metaParams);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnGoogleSignIn.setEnabled(!isLoading);
        btnSendOtp.setEnabled(!isLoading);
        btnVerifyOtp.setEnabled(!isLoading);
        etPhoneNumber.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
        ccp.setEnabled(!isLoading);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, ApplyToTeachActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToDemo() {
        Intent intent = new Intent(this, demoActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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