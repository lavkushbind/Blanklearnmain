package com.blank_learn.payment;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.blank_learn.dark.R;

import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.databinding.About2Binding;
import com.blank_learn.dark.databinding.ActivityMain3Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

// Make sure your binding class name is correct

// Implement the Razorpay listener
public class PaymentActivity_teacher extends AppCompatActivity implements PaymentResultWithDataListener {

    // Keep binding variable if using ViewBinding
    private ActivityMain3Binding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    // Store amount in paise (Integer) for Razorpay
    private int selectedAmountInPaise = 0;
    private String selectedPlanName = "1 Month Plan"; // Default or initial plan

    private static final String TAG = "PaymentActivity";
    // --- IMPORTANT: Replace with your actual Razorpay Key ID ---
    // --- For production, load this securely (e.g., from build config) ---
    private static final String RAZORPAY_KEY_ID = "rzp_live_6vd9RApruseTAi"; // Use Test key for development

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Preload Razorpay checkout for faster loading
        Checkout.preload(getApplicationContext());

        setupPlanSelection();
        setupPayButton();

        // Set initial state (optional, depends on your default selection)
        selectPlan1Month(); // Example: Select 1 month plan initially
    }

    private void setupPlanSelection() {
        binding.plan1Months.setOnClickListener(v -> selectPlan1Month());
        binding.plan3Months.setOnClickListener(v -> selectPlan3Months());
    }

    private void selectPlan1Month() {
        binding.amountToPay.setText("₹299");
        binding.originalAmount.setText("₹499");
        // Update background using ContextCompat for compatibility
        binding.plan1Months.setBackground(ContextCompat.getDrawable(this, R.drawable.pay_bg)); // Selected bg
        binding.plan3Months.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_premium_card)); // Unselected bg

        selectedAmountInPaise = 299 * 100;
        selectedPlanName = "1 Month Premium";
        Log.d(TAG, "Selected Plan: " + selectedPlanName + ", Amount: " + selectedAmountInPaise);
    }

    private void selectPlan3Months() {
        binding.amountToPay.setText("₹899");
        binding.originalAmount.setText("₹1499");
        // Update background using ContextCompat for compatibility
        binding.plan3Months.setBackground(ContextCompat.getDrawable(this, R.drawable.pay_bg)); // Selected bg
        binding.plan1Months.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_premium_card)); // Unselected bg

        selectedAmountInPaise = 899 * 100; // Amount in paise
        selectedPlanName = "3 Months Premium";
        Log.d(TAG, "Selected Plan: " + selectedPlanName + ", Amount: " + selectedAmountInPaise);
    }


    private void setupPayButton() {
        binding.upgradeButton.setOnClickListener(v -> {
            if (selectedAmountInPaise <= 0) {
                Toast.makeText(this, "Please select a plan first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentUser == null) {
                Toast.makeText(this, "Please login to continue.", Toast.LENGTH_SHORT).show();
                // Optionally redirect to login screen
                return;
            }
            startPayment(selectedAmountInPaise, selectedPlanName);
        });
    }

    public void startPayment(int amount, String planDescription) {
        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
        checkout.setKeyID(RAZORPAY_KEY_ID); // Set your Key ID here

        /**
         * Set logo for checkout form
         * Set to Integer (resource ID)
         */
        checkout.setImage(R.drawable.background_logo); // Your app logo (Pass the Resource ID directly)
        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass payment options
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", "Blanklearn"); // App Name or Company Name
            options.put("description", "Subscription: " + planDescription);

            options.put("theme.color", "#01071B"); // Theme color for Razorpay checkout
            options.put("currency", "INR"); // Currency code
            options.put("amount", String.valueOf(amount)); // Amount in paise (e.g., 50000 for INR 500.00)
            options.put("retry.enabled", true); // Enable retry option on failure
            options.put("retry.max_count", 4);  // Max number of retries

            JSONObject prefill = new JSONObject();
            if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                prefill.put("email", currentUser.getEmail());
            }
            if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty()) {
                prefill.put("contact", currentUser.getPhoneNumber());
            } else {
                // If phone number isn't directly available, you might need to fetch it
                // from your user profile data in Firestore/Realtime DB if stored there.
                // prefill.put("contact", "USER_PHONE_FETCHED_ELSEWHERE");
            }

            options.put("prefill", prefill);

            // Add notes if needed (optional metadata)
            JSONObject notes = new JSONObject();
            notes.put("user_uid", currentUser.getUid());
            notes.put("plan_selected", planDescription);
            options.put("notes", notes);


            // Open Razorpay Checkout activity
            checkout.open(activity, options);

        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
            Toast.makeText(activity, "Error initiating payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- Razorpay PaymentResultWithDataListener Methods ---

    @Override
    public void onPaymentSuccess(String razorpayPaymentId, PaymentData paymentData) {

        try {
            String paymentId = paymentData.getPaymentId();
            String signature = paymentData.getSignature();
            String orderId = paymentData.getOrderId();
            String contact = paymentData.getUserContact();
            String email = paymentData.getUserEmail();

            Log.i(TAG, "Payment Successful:");
            Log.i(TAG, "Payment ID: " + paymentId);
            Log.i(TAG, "Order ID: " + orderId);
            Log.i(TAG, "Signature: " + signature);
            Log.i(TAG, "Contact: " + contact);
            Log.i(TAG, "Email: " + email);


            Toast.makeText(this, "Payment Successful! Payment ID: " + razorpayPaymentId, Toast.LENGTH_LONG).show();

            updateUserSubscriptionStatus(paymentId); // Pass paymentId if you want to store it


        } catch (Exception e) {
            Log.e(TAG, "Error processing successful payment data", e);
            Toast.makeText(this, "Payment Successful, but error processing data.", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUserSubscriptionStatus(String paymentId) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Calculate expiry date
            Calendar calendar = Calendar.getInstance();
            long paymentTimestamp = calendar.getTimeInMillis(); // Use current client time as payment time

            int validityDays = 30; // Default to 30 days (1 month)
            if (selectedPlanName != null && selectedPlanName.contains("3 Months")) {
                validityDays = 90; // Set to 90 days for 3 month plan
            } else if (selectedPlanName == null){
                Log.w(TAG, "selectedPlanName is null, defaulting to 30 days validity.");
                // Handle cases where plan name might not be set correctly
            }


            calendar.add(Calendar.DAY_OF_YEAR, validityDays);
            long expiryTimestamp = calendar.getTimeInMillis();

            // Prepare data to update
            Map<String, Object> updates = new HashMap<>();
            updates.put("verify", true);
            // Use ServerValue.TIMESTAMP for paymentDate if you prefer Firebase server time
            // updates.put("paymentDate", ServerValue.TIMESTAMP);
            updates.put("paymentDate", paymentTimestamp); // Using client timestamp here
            updates.put("expiryDate", expiryTimestamp);
            updates.put("currentPlan", selectedPlanName); // Store the plan name
            updates.put("lastPaymentId", paymentId); // Optionally store the payment ID

            userRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.i(TAG, "Firebase user subscription status updated successfully for user: " + userId);
                        Toast.makeText(PaymentActivity_teacher.this, "Subscription Activated!", Toast.LENGTH_SHORT).show();
                        // Optionally navigate the user or refresh UI here
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update Firebase user subscription status for user: " + userId, e);
                        // Inform the user, maybe offer support contact.
                        // This is critical - payment succeeded but activation failed in DB.
                        Toast.makeText(PaymentActivity_teacher.this, "Payment successful, but activation failed. Please contact support.", Toast.LENGTH_LONG).show();
                    });

        } else {
            Log.e(TAG, "Cannot update Firebase: User is not logged in.");
            // Handle case where user somehow got logged out between payment start and success
            Toast.makeText(this, "Payment successful, but could not find logged in user to update status.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPaymentError(int code, String description, PaymentData paymentData) {
        /**
         * Add your logic here for a failed payment response
         * Error codes: https://razorpay.com/docs/payments/payment-gateway/android-integration/standard/error-codes/
         */
        try {
            Log.e(TAG, "Payment Failed:");
            Log.e(TAG, "Code: " + code);
            Log.e(TAG, "Description: " + description);
            if (paymentData != null) {
                Log.e(TAG, "Order ID: " + paymentData.getOrderId());
                Log.e(TAG, "Payment ID: " + paymentData.getPaymentId()); // Might be null
                Log.e(TAG, "Contact: " + paymentData.getUserContact());
                Log.e(TAG, "Email: " + paymentData.getUserEmail());
            }

            Toast.makeText(this, "Payment Failed: " + description + " (Code: " + code + ")", Toast.LENGTH_LONG).show();

            // You can provide specific user feedback based on the 'code'
            if (code == Checkout.NETWORK_ERROR) {
                Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            } else if (code == Checkout.PAYMENT_CANCELED) {
                Toast.makeText(this, "Payment cancelled.", Toast.LENGTH_SHORT).show();
            } // Add more specific error handling if needed

        } catch (Exception e) {
            Log.e(TAG, "Error processing failed payment data", e);
            Toast.makeText(this, "Payment Failed.", Toast.LENGTH_LONG).show();
        }
    }
}
//public class PaymentActivity_teacher extends AppCompatActivity{
//    private RadioGroup planGroup;
//    private Button btnPay;
//    private int selectedAmount = 0;
//    FirebaseAuth firebaseAuth;
//    private String selectedPlan = "Basic";
//    private ActivityMain3Binding binding;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityMain3Binding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        binding.plan1Months.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                binding.plan1Months.setBackground(R.drawable.card_background);
//                binding.amountToPay.setText("₹299");
//                binding.originalAmount.setText("₹499");
//                binding.plan1Months.setBackground(getDrawable(R.drawable.pay_bg));
//                binding.plan3Months.setBackground(getDrawable(R.drawable.bg_rounded_premium_card));
//
//            }
//        });
//
//        binding.plan3Months.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                binding.plan3Months.setBackground(getDrawable(R.drawable.pay_bg));
//                binding.plan1Months.setBackground(getDrawable(R.drawable.bg_rounded_premium_card));
//
////                binding.plan1Months.setBackground(R.drawable.card_background);
//binding.amountToPay.setText("₹899");
//binding.originalAmount.setText("₹1499");
//
//            }
//        });
//        binding.upgradeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                razorpay();
//            }
//        });
//    }}
