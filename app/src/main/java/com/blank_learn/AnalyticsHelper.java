package com.blank_learn;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * A helper class to centralize all analytics logging for Firebase and Meta (Facebook).
 * This promotes code reuse and ensures consistent event tracking.
 */
public final class AnalyticsHelper {
    private static final String TAG = "AnalyticsHelper";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AnalyticsHelper() {}

    /**
     * Logs a successful user sign-up (new registration) event to both Firebase and Meta.
     * @param context The application context.
     * @param method The method used for registration (e.g., "phone", "google").
     */
    public static void logSignUp(Context context, String method) {
        Log.d(TAG, "Logging SIGN_UP event for method: " + method);

        // Firebase Analytics Event
        Bundle firebaseBundle = new Bundle();
        firebaseBundle.putString(FirebaseAnalytics.Param.METHOD, method);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SIGN_UP, firebaseBundle);

        // Meta (Facebook) SDK Event
        AppEventsLogger logger = AppEventsLogger.newLogger(context);
        Bundle metaParams = new Bundle();
        metaParams.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, method);
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, metaParams);
    }

    /**
     * Logs a successful user login event to Firebase.
     * @param context The application context.
     * @param method The method used for login (e.g., "phone", "google").
     */
    public static void logLogin(Context context, String method) {
        Log.d(TAG, "Logging LOGIN event for method: " + method);

        // Firebase Analytics Event
        Bundle firebaseBundle = new Bundle();
        firebaseBundle.putString(FirebaseAnalytics.Param.METHOD, method);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.LOGIN, firebaseBundle);
    }
}