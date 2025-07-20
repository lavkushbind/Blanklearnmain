
package com.blank_learn.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.blank_learn.dark.R;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.blank_learn.home.MainActivity;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger; // <-- META SDK IMPORT
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

import java.math.BigDecimal; // <-- IMPORT for currency
import java.util.Currency;   // <-- IMPORT for currency

public class PaymentActivity_demo extends AppCompatActivity implements PaymentResultListener {

    private String allocationId;
    private AppEventsLogger logger; // <-- META SDK LOGGER
    private double paymentAmount = 100.00; // Define amount in a variable (10000 paise = 100 INR)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_next_demo);

        // Initialize Meta SDK Logger
        logger = AppEventsLogger.newLogger(this); // <-- INITIALIZE LOGGER

        // Get the allocationId from the intent
        allocationId = getIntent().getStringExtra("allocationId");

        // Check if allocationId is null or empty
        if (allocationId == null || allocationId.isEmpty()) {
            Log.e("PaymentActivity", "allocationId is null or empty");
            Toast.makeText(this, "Error: allocationId not provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if allocationId is missing
            return;
        }

        // META SDK INTEGRATION: Log InitiateCheckout event
        // This tells Meta that a user has started the payment process.
        logInitiateCheckoutEvent();

        // Initiate Razorpay payment immediately on creation
        initiateRazorpayPayment(this);
    }

    private void initiateRazorpayPayment(Activity activity) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_live_6vd9RApruseTAi"); // IMPORTANT: Consider moving keys to a secure place.

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Blanklearn");
            options.put("description", "Enrollment fee");
            // options.put("image", R.drawable.background_logo); // This needs to be a URL, not a drawable resource.
            options.put("theme.color", "#0A0D1C");
            options.put("currency", "INR");
            options.put("amount", (int)(paymentAmount * 100)); // Amount in paise (10000)
            options.put("prefill.email", "user@example.com"); // You should fetch the actual user's email
            options.put("prefill.contact", "9235044520"); // You should fetch the actual user's contact

            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 3);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch (Exception e) {
            Log.e("PaymentActivity", "Error in starting Razorpay Checkout", e);
            Toast.makeText(activity, "Error initializing Razorpay: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        Log.d("PaymentActivity", "Payment Successful: " + razorpayPaymentId);
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();

        // META SDK INTEGRATION: Log the Purchase event. This is your MOST IMPORTANT event.
        logPurchaseEvent(razorpayPaymentId);

        updatePaymentStatusInDatabase(allocationId, "paid");
    }

    @Override
    public void onPaymentError(int code, String message) {
        Log.e("PaymentActivity", "Payment Failed: code=" + code + ", message=" + message);
        Toast.makeText(this, "Payment Failed. Please try again.", Toast.LENGTH_LONG).show();

        // Don't redirect immediately. Let the user see the error and decide to retry.
        // I have removed the automatic redirect to MainActivity on failure.
        // You can add a "Back" or "Retry" button for the user.
    }

    private void updatePaymentStatusInDatabase(String allocationId, String newStatus) {
        DatabaseReference allocationRef = FirebaseDatabase.getInstance().getReference("allocated_classes").child(allocationId);

        allocationRef.child("paymentStatus").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PaymentUpdate", "Payment status updated successfully for allocationId: " + allocationId);
                    Intent intent = new Intent(PaymentActivity_demo.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("PaymentUpdate", "Failed to update payment status for allocationId: " + allocationId, e);
                    // Even if DB update fails, the payment was successful.
                    // You should still take the user to the main screen and maybe handle the DB failure silently.
                    Intent intent = new Intent(PaymentActivity_demo.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
    }

    /**
     * META SDK EVENT: Logs when a user starts the checkout process.
     */
    private void logInitiateCheckoutEvent() {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product");
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "demo_enrollment_fee");
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, "INR");
        logger.logEvent(AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT, paymentAmount, params);
        Log.d("MetaEvent", "Logged 'InitiateCheckout' event.");
    }

    /**
     * META SDK EVENT: Logs when a user successfully completes a purchase.
     * This uses logPurchase, which is the standard method for this.
     * @param razorpayPaymentId The transaction ID from Razorpay.
     */
    private void logPurchaseEvent(String razorpayPaymentId) {
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "product_group");
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, "demo_enrollment"); // A general ID for this product type
        params.putString("razorpay_payment_id", razorpayPaymentId);

        // The logPurchase method is specifically designed for purchases
        logger.logPurchase(BigDecimal.valueOf(paymentAmount), Currency.getInstance("INR"), params);
        Log.d("MetaEvent", "Logged 'Purchase' event for amount " + paymentAmount);
    }
}