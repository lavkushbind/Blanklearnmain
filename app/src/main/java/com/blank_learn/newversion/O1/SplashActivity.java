package com.blank_learn.newversion.O1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.blank_learn.dark.R;

// Firebase Auth & Database Imports
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// Analytics Imports
import com.google.firebase.analytics.FirebaseAnalytics;
import com.facebook.appevents.AppEventsLogger;

// Activities Imports
import com.blank_learn.newversion.O2.MainActivity; // Main Entry point for App

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "SplashActivity";

    // Analytics Variables
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 1. Initialize Firebase Auth & Analytics
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fbLogger = AppEventsLogger.newLogger(this);

        // 2. Animations Setup (Logo & Tagline)
        ImageView logo = findViewById(R.id.logo_image);
        TextView tagline = findViewById(R.id.tagline_text);

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1500);

        if (logo != null) logo.startAnimation(fadeIn);
        if (tagline != null) tagline.startAnimation(fadeIn);

        // 3. 1.5 Second Delay -> Check User Status
        new Handler().postDelayed(this::checkUserStatus, 1500);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // === CONDITION 1: USER NOT LOGGED IN ===
        // Logic: Send to Onboarding Slides
        if (currentUser == null) {
            Log.d(TAG, "User Not Logged In. Going to Onboarding.");
            logAnalyticsEvent("splash_nav_onboarding", "guest");
            navigateTo(Onboarding_Slides.class);
        }
        // === CONDITION 2: USER LOGGED IN ===
        // Logic: Check DB for Analytics, then send to MainActivity
        else {
            Log.d(TAG, "User Logged In (UID: " + currentUser.getUid() + "). Checking Subscription...");
            checkSubscriptionAndRedirect(currentUser.getUid());
        }
    }

    private void checkSubscriptionAndRedirect(String userId) {
        DatabaseReference userSubscriptionRef = FirebaseDatabase.getInstance()
                .getReference("user_subscriptions")
                .child(userId);

        userSubscriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String orderStatus = null;
                if (snapshot.exists()) {
                    orderStatus = snapshot.child("orderStatus").getValue(String.class);
                }

                if (orderStatus != null && orderStatus.equals("paid")) {
                    // PAID USER
                    Log.d(TAG, "Status: PAID");
                    setUserProperty("paid", userId);
                    logAnalyticsEvent("splash_user_paid", userId);
                } else {
                    // FREE USER
                    Log.d(TAG, "Status: FREE");
                    setUserProperty("free", userId);
                    logAnalyticsEvent("splash_user_free", userId);
                }

                // IMPORTANT: Both Paid and Free users go to MainActivity.
                // MainActivity will handle showing the correct Fragment (NewHome or FreeHome).
                navigateTo(MainActivity.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database Error: " + error.getMessage());
                // Fallback: Even if DB fails, go to MainActivity (It handles offline/errors)
                navigateTo(MainActivity.class);
            }
        });
    }

    // --- Helper Methods ---

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(SplashActivity.this, activityClass);
        // Clear back stack so user cannot go back to Splash
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logAnalyticsEvent(String eventName, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("user_id", userId);
        mFirebaseAnalytics.logEvent(eventName, bundle);
        fbLogger.logEvent(eventName, bundle);
    }

    private void setUserProperty(String status, String userId) {
        mFirebaseAnalytics.setUserProperty("subscription_status", status);
        AppEventsLogger.setUserID(userId);
    }
}