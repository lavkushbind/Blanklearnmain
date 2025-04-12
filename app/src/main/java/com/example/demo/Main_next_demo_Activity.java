package com.example.demo;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.blank_learn.dark.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.home.MainActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

public class Main_next_demo_Activity extends AppCompatActivity implements PaymentResultListener {

    private String allocationId;
    private String amount;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_next_demo);

        allocationId = getIntent().getStringExtra("allocationId");
//        amount = getIntent().getStringExtra("price");

        intent = getIntent();
        amount = intent.getStringExtra("price");
        if (allocationId == null || allocationId.isEmpty()) {
            Log.e("PaymentActivity", "allocationId is null or empty");
            Toast.makeText(this, "Error: allocationId not provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if allocationId is missing
            return;
        }

        // Initiate Razorpay payment immediately on creation
        initiateRazorpayPayment(this);
    }

    private void initiateRazorpayPayment(Activity activity) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_live_6vd9RApruseTAi");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Blanklearn");
            options.put("description", "Enrollment fee");
            options.put("image", R.drawable.background_logo);
            options.put("theme.color", "#0A0D1C");
            options.put("currency", "INR");
            options.put("amount", 100000);
            options.put("prefill.email", "user@example.com");
            options.put("prefill.contact", "9235044520");

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
        Toast.makeText(this, "Payment Successful: " + razorpayPaymentId, Toast.LENGTH_SHORT).show();

        updatePaymentStatusInDatabase(allocationId, "paid");

    }

    @Override
    public void onPaymentError(int code, String message) {
        Log.e("PaymentActivity", "Payment Failed: code=" + code + ", message=" + message);
        Intent intent=  new Intent(Main_next_demo_Activity.this, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updatePaymentStatusInDatabase(String allocationId, String newStatus) {
        DatabaseReference allocationRef = FirebaseDatabase.getInstance().getReference("allocated_classes").child(allocationId);

        allocationRef.child("paymentStatus").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PaymentUpdate", "Payment status updated successfully for allocationId: " + allocationId + " to: " + newStatus);
                    Intent intent=  new Intent(Main_next_demo_Activity.this, MainActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("PaymentUpdate", "Failed to update payment status for allocationId: " + allocationId + ": " + e.getMessage());
                    Intent intent=  new Intent(Main_next_demo_Activity.this, MainActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
    }
}