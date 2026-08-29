package com.blank_learn.newversion.O2;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;
import com.blank_learn.profile.EditFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

// RAZORPAY
import com.razorpay.PaymentResultListener;

import com.blank_learn.newversion.Fragments.DoubtSolveFragment;
import com.blank_learn.newversion.Fragments.NewFreeHomeFragment;
import com.blank_learn.newversion.Fragments.NewHomeFragment;
import com.blank_learn.newversion.Fragments.ProfileFragment1;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {

    private BottomNavigationView bottomNavigationView;
    private boolean isPaidUser = false;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        askNotificationPermission();

        Window window = getWindow();
        window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (isConnected()) {
            checkUserSubscriptionAndInit();
        } else {
            showNoInternetDialog();
        }
    }

    // ====================================================
    // RAZORPAY SUCCESS/ERROR HANDLERS
    // ====================================================
    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            long now = System.currentTimeMillis();
            long expiry = now + (30L * 24 * 60 * 60 * 1000); // Default 30 Days

            // 1. Update Profile Fees Card Node
            HashMap<String, Object> fees = new HashMap<>();
            fees.put("paymentDate", now);
            fees.put("expiryDate", expiry);
            fees.put("planType", "Individual Monthly Plan");
            fees.put("txnId", razorpayPaymentId);
            dbRef.child("FeesPayments").child(uid).setValue(fees);

            // 2. Update Subscription Node (Unlock Premium Home)
            HashMap<String, Object> sub = new HashMap<>();
            sub.put("orderStatus", "paid");
            sub.put("planName", "Premium Plan");
            dbRef.child("user_subscriptions").child(uid).setValue(sub)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                        checkUserSubscriptionAndInit(); // Refresh Home Screen
                    });
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_SHORT).show();
    }

    // ====================================================
    // SUBSCRIPTION & NAVIGATION
    // ====================================================
    private void checkUserSubscriptionAndInit() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            dbRef.child("user_subscriptions").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && "paid".equals(snapshot.child("orderStatus").getValue(String.class))) {
                                isPaidUser = true;
                                loadFragment(new NewHomeFragment());
                            } else {
                                isPaidUser = false;
                                loadFragment(new NewFreeHomeFragment());
                            }
                            setupBottomNavigationListener();
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            loadFragment(new NewFreeHomeFragment());
            setupBottomNavigationListener();
        }
    }

    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(isPaidUser ? new NewHomeFragment() : new NewFreeHomeFragment());
            } else if (itemId == R.id.nav_quiz) {
                loadFragment(new EditFragment());
            } else if (itemId == R.id.nav_doubt) {
                loadFragment(new DoubtSolveFragment());
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment1());
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.container, fragment).commit();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this).setTitle("No Internet")
                .setMessage("Please check connection.").setPositiveButton("Retry", (d, w) -> {
                    if (isConnected()) checkUserSubscriptionAndInit(); else showNoInternetDialog();
                }).show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } else {
            getAndUpdateFcmToken();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> getAndUpdateFcmToken());

    private void getAndUpdateFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (mAuth.getCurrentUser() != null) {
                    dbRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("fcmToken").setValue(task.getResult());
                }
            }
        });
    }

    public void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit();
    }
}