
        package com.blank_learn.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import com.blank_learn.dark.R;
import com.blank_learn.home.MainActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

public class Main_next_demo_Activity extends AppCompatActivity implements PaymentResultListener {

     private static final String TAG = "PaymentActivity";
    private String allocationId;
    private ImageView logoImageView; // ImageView के लिए एक वेरिएबल बनाएँ

    private int selectedPriceInRupees; // कीमत को रुपये में स्टोर करने के लिए
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_next_demo);
        logoImageView = findViewById(R.id.logoImageView);

        // 2. आपका लोगो URL
        String logoUrl = "https://firebasestorage.googleapis.com/v0/b/dark-6191f.appspot.com/o/review_images%2F1753481804618.jpg?alt=media&token=f2157ecf-dad8-4827-8ebf-c41e2ed1042c";

        // 3. Glide का उपयोग करके URL से इमेज लोड करें
        Glide.with(this) // Context पास करें
                .load(logoUrl) // आपका इमेज URL
                .into(logoImageView);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Intent से डेटा प्राप्त करें
        Intent intent = getIntent();
        allocationId = intent.getStringExtra("allocationId");

        // **सुधार 1: कीमत को एक int के रूप में प्राप्त करें, String के रूप में नहीं**
        // 0 एक डिफ़ॉल्ट मान है अगर 'price' नहीं मिलता है
        selectedPriceInRupees = intent.getIntExtra("price", 0);

        // जाँच करें कि allocationId और कीमत मान्य हैं
        if (allocationId == null || allocationId.isEmpty()) {
            Log.e(TAG, "Error: allocationId is null or empty.");
            Toast.makeText(this, "Error: Course ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (selectedPriceInRupees <= 0) {
            Log.e(TAG, "Error: Invalid price received: " + selectedPriceInRupees);
            Toast.makeText(this, "Error: Invalid price.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // एक्टिविटी बनते ही तुरंत Razorpay पेमेंट शुरू करें
        initiateRazorpayPayment(this);
    }

    private void initiateRazorpayPayment(Activity activity) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_live_6vd9RApruseTAi"); // आपका लाइव की

        // **सुधार 2: कीमत को पैसे में बदलें (1 रुपया = 100 पैसे)**
        long amountInPaisa = selectedPriceInRupees * 100L;

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Blanklearn");
            options.put("description", "Course Enrollment Fee");
            // एक पारदर्शी लोगो के बजाय अपने ऐप का आइकन उपयोग करना बेहतर है
            options.put("image", R.mipmap.ic_launcher);
            options.put("theme.color", "#0A0D1C");
            options.put("currency", "INR");
            options.put("amount", amountInPaisa); // यहाँ पैसे में राशि डालें

            // यूजर की जानकारी पहले से भरें (इसे डायनामिक रूप से प्राप्त करना सबसे अच्छा है)
            JSONObject prefill = new JSONObject();
            prefill.put("email", "user@example.com"); // इसे असल यूजर ईमेल से बदलें
            prefill.put("contact", "9235044520"); // इसे असल यूजर फोन से बदलें
            options.put("prefill", prefill);

            // पेमेंट विफल होने पर फिर से प्रयास करने का विकल्प
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
        Log.d(TAG, "Payment Successful: " + razorpayPaymentId);
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();

        // **सुधार 3: Firebase Analytics में सही कीमत लॉग करें**
        Bundle purchaseBundle = new Bundle();
        purchaseBundle.putDouble(FirebaseAnalytics.Param.VALUE, selectedPriceInRupees); // हार्डकोडेड वैल्यू के बजाय असल कीमत का उपयोग करें
        purchaseBundle.putString(FirebaseAnalytics.Param.CURRENCY, "INR");
        purchaseBundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, razorpayPaymentId);
        purchaseBundle.putString(FirebaseAnalytics.Param.ITEM_ID, allocationId); // आइटम आईडी के रूप में allocationId का उपयोग करें
        purchaseBundle.putString(FirebaseAnalytics.Param.PAYMENT_TYPE, "razorpay");

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, purchaseBundle);

        // Meta SDK इवेंट लॉगिंग (अगर आपने इसे सेटअप किया है)
        // logMetaPurchaseEvent(selectedPriceInRupees, razorpayPaymentId);

        updatePaymentStatusInDatabase(allocationId, "paid", razorpayPaymentId);
    }

    @Override
    public void onPaymentError(int code, String message) {
        Log.e(TAG, "Payment Failed: code=" + code + ", message=" + message);
        Toast.makeText(this, "Payment Failed: " + message, Toast.LENGTH_LONG).show();
        navigateToHome();
    }

    private void updatePaymentStatusInDatabase(String allocationId, String newStatus, String paymentId) {
        DatabaseReference allocationRef = FirebaseDatabase.getInstance().getReference("allocated_classes").child(allocationId);

        // पेमेंट आईडी को भी सेव करना एक अच्छी प्रैक्टिस है
        allocationRef.child("paymentStatus").setValue(newStatus);
        allocationRef.child("razorpayPaymentId").setValue(paymentId)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payment status updated successfully for allocationId: " + allocationId);
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update payment status for allocationId: " + allocationId, e);
                    // फिर भी यूजर को होम पर भेजें और एक टोस्ट दिखाएँ
                    Toast.makeText(this, "Payment successful, but failed to update record.", Toast.LENGTH_LONG).show();
                    navigateToHome();
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(Main_next_demo_Activity.this, MainActivity.class);
        // **सुधार 4: बेहतर नेविगेशन**
        // यह पेमेंट एक्टिविटी को बैक स्टैक से हटा देगा
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // इस एक्टिविटी को पूरी तरह से बंद कर दें
    }


}