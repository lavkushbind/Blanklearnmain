package com.blank_learn.demo;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.blank_learn.dark.R;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Currency;

public class PaymentActivityDemo extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "PaymentActivity";
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger metaLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This layout can be very simple, e.g., just a ProgressBar,
        // as Razorpay will show its own UI on top.
        setContentView(R.layout.activity_main_next_demo);

        // --- Initialize Analytics SDKs ---
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        metaLogger = AppEventsLogger.newLogger(this);

        // --- Get Data from Intent ---
        String amountString = getIntent().getStringExtra("amount");
        String userEmail = getIntent().getStringExtra("user_email");
        String userPhone = getIntent().getStringExtra("user_phone");

        // --- Validate Data ---
        if (amountString == null || amountString.isEmpty()) {
            Log.e(TAG, "Amount not provided in Intent. Closing activity.");
            Toast.makeText(this, "Error: Payment amount is missing.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if essential data is missing
            return;
        }

        // --- Start Payment ---
        initiateRazorpayPayment(this, amountString, userEmail, userPhone);
    }

    private void initiateRazorpayPayment(Activity activity, String amount, String email, String phone) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_live_6vd9RApruseTAi"); // Your LIVE Razorpay Key ID

        int amountInPaise;
        try {
            // Razorpay requires the amount to be in the smallest currency unit (paise for INR)
            amountInPaise = Integer.parseInt(amount) * 100;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid amount format provided: " + amount, e);
            Toast.makeText(activity, "Invalid payment amount.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Blanklearn");
            options.put("description", "1-on-1 Demo Class Booking");

            // IMPORTANT: Razorpay needs a URL for the image, not a local drawable.
            // Replace this with a public URL of your logo.
            options.put("image", R.drawable.background_logo); // Example URL

            options.put("theme.color", "#0A0D1C");
            options.put("currency", "INR");
            options.put("amount", amountInPaise);

            // Prefill user details for a better experience
            JSONObject prefill = new JSONObject();
            prefill.put("email", email);
            prefill.put("contact", phone);
            options.put("prefill", prefill);

            // Enable retry mechanism
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 3);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
            Toast.makeText(activity, "Error initializing payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        Log.d(TAG, "Payment Successful. Razorpay Payment ID: " + razorpayPaymentId);
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();

        // Log the purchase to both Firebase and Meta for tracking
        logPurchaseEvents(razorpayPaymentId);

        // Send a success result back to the previous activity (demoActivity2)
        Intent resultIntent = new Intent();
        resultIntent.putExtra("razorpay_payment_id", razorpayPaymentId);
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity
    }

    @Override
    public void onPaymentError(int code, String message) {
        Log.e(TAG, "Payment Failed. Code: " + code + ", Message: " + message);
        Toast.makeText(this, "Payment Failed: " + message, Toast.LENGTH_LONG).show();

        // Send a cancellation/failure result back to the previous activity
        setResult(RESULT_CANCELED);
        finish(); // Close this activity
    }

    /**
     * Logs the successful purchase to both Firebase and Meta platforms for analytics and ad optimization.
     * @param razorpayPaymentId The unique transaction ID from Razorpay.
     */
    private void logPurchaseEvents(String razorpayPaymentId) {
        double purchaseValue = 9.0;
        String currency = "INR";
        String itemId = "demo_class_booking_fee";

        // --- Log to Firebase Analytics (for Google Ads & Analytics) ---
        try {
            Bundle firebasePurchaseBundle = new Bundle();
            firebasePurchaseBundle.putDouble(FirebaseAnalytics.Param.VALUE, purchaseValue);
            firebasePurchaseBundle.putString(FirebaseAnalytics.Param.CURRENCY, currency);
            firebasePurchaseBundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, razorpayPaymentId);
            firebasePurchaseBundle.putString(FirebaseAnalytics.Param.ITEM_ID, itemId);
            firebasePurchaseBundle.putString("payment_method", "razorpay"); // Custom parameter

            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, firebasePurchaseBundle);
            Log.d(TAG, "Successfully logged PURCHASE event to Firebase Analytics.");

        } catch (Exception e) {
            Log.e(TAG, "Error logging PURCHASE event to Firebase Analytics", e);
        }

        // --- Log to Meta SDK (for Facebook/Instagram Ads) ---
        try {
            // Using logPurchase is crucial for ad optimization.
            Bundle metaParams = new Bundle();
            metaParams.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, itemId);
            metaParams.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product");

            metaLogger.logPurchase(
                    BigDecimal.valueOf(purchaseValue),
                    Currency.getInstance(currency),
                    metaParams
            );
            Log.d(TAG, "Successfully logged PURCHASED event to Meta SDK.");

        } catch (Exception e) {
            Log.e(TAG, "Error logging PURCHASED event to Meta SDK", e);
        }
    }
}