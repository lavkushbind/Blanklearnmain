package com.example.home;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.dark.R;
import android.widget.Toast;

import com.example.notification.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PaymentActivity_demo extends AppCompatActivity implements RazorpayPaymentResultWithDataListener {
String scheduleId;

Intent intent;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_demo);

        database= FirebaseDatabase.getInstance();

        intent = getIntent();
        scheduleId = intent.getStringExtra("demo_id");
        startPayment();
    }

    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_live_6vd9RApruseTAi");
        checkout.setImage(R.drawable.lastlogo);
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Blanklearn");
            options.put("description", "₹9 Payment");
            options.put("theme.color", "#0A2FF8" );
            options.put("currency", "INR");
            options.put("amount", 100000);
            options.put("prefill.email", "blanklearn.com@example.com");
            options.put("prefill.contact", "9569998205");
            checkout.open(this, options);
        } catch ( Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID, PaymentData paymentData) {


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("UserSchedules").child(userId);
        Map<String, Object> paymentStatusUpdate = new HashMap<>();
        paymentStatusUpdate.put("paymentStatus", "paid");
        databaseRef.child(scheduleId).updateChildren(paymentStatusUpdate)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                    } else {
                    }
                });



    }

    @Override
    public void onPaymentError(int code, String response, PaymentData paymentData) {
        Toast.makeText(this, "Payment Failed ", Toast.LENGTH_SHORT).show();

    }
}
