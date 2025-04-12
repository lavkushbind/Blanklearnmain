package com.example.One_Signal;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.Continue;
import com.onesignal.inAppMessages.IInAppMessage;

public class ApplicationClass extends Application {

    private static final String ONESIGNAL_APP_ID = "102b9dc4-8938-43bf-88b1-4df4b52d136d";

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        OneSignal.getNotifications().requestPermission(false, Continue.none());

        setupNotificationHandlers();

        setupInAppMessageHandlers();

        setupSubscriptionHandling();
    }

    private void setupInAppMessageHandlers() {
    }

    private void setupNotificationHandlers() {
        // Handle notification click events
        OneSignal.getNotifications().addClickListener(result -> {
            // Get the notification details
            String title = result.getNotification().getTitle();
            String body = result.getNotification().getBody();
            Log.d("OneSignal", "Notification clicked - Title: " + title + ", Body: " + body);

            // Handle actions (if any)
            if (result.getResult() != null && result.getResult().getActionId() != null) {
                String actionId = result.getResult().getActionId();
                Log.d("OneSignal", "Notification action clicked: " + actionId);
            }
        });
    }

    private void setupSubscriptionHandling() {
        // Listen for changes in subscription status
        OneSignal.getUser().getPushSubscription().addObserver(optedIn -> {
            Log.d("OneSignal", "Subscription status changed - Subscribed: " + optedIn);
        });
    }
}