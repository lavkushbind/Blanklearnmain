package com.blank_learn.newversion.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.blank_learn.newversion.O1.PhoneAuthActivity;
import com.blank_learn.newversion.O2.PlanBuilderActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    // UI Views
    private TextView tvName, tvPhone, tvPoints, tvClasses, tvPlanName, tvPlanStatus;
    private View btnUpgrade, btnLogout, btnRefer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Init Views
        tvName = findViewById(R.id.tvUserName);
        tvPhone = findViewById(R.id.tvUserEmail);
        tvPoints = findViewById(R.id.tvStatPoints);
        tvClasses = findViewById(R.id.tvStatClasses);
        tvPlanName = findViewById(R.id.tvPlanName);
        tvPlanStatus = findViewById(R.id.tvPlanStatus);

        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnLogout = findViewById(R.id.btnLogout);
        btnRefer = findViewById(R.id.cardRefer);

        // Load Data
        loadUserProfile();
        checkSubscription();

        // --- Click Listeners ---

        // 1. Upgrade Button
        btnUpgrade.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PlanBuilderActivity.class));
        });

        // 2. Refer & Earn
        btnRefer.setOnClickListener(v -> {
            shareApp();
        });

        // 3. Logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, PhoneAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 4. Edit Profile (Placeholder)
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Toast.makeText(this, "Edit Profile Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Set Phone/Email from Auth
            String contact = user.getPhoneNumber();
            if(contact == null) contact = user.getEmail();
            tvPhone.setText(contact);

            // Fetch Name & Points from Database
            dbRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        Long points = snapshot.child("quiz_points").getValue(Long.class); // Assuming you save quiz points here

                        if (name != null) tvName.setText(name);
                        else tvName.setText("Student");

                        if (points != null) tvPoints.setText(String.valueOf(points));
                        else tvPoints.setText("0");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void checkSubscription() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            dbRef.child("user_subscriptions").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User is Paid
                        String plan = snapshot.child("planName").getValue(String.class); // Save plan name in DB e.g. "Gold Plan"
                        tvPlanName.setText(plan != null ? plan : "Premium Member");
                        tvPlanStatus.setText("Active • Valid Subscription");
                        tvPlanStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));

                        // Hide Upgrade Button if already paid
                        btnUpgrade.setVisibility(View.GONE);
                    } else {
                        // User is Free
                        tvPlanName.setText("Free Plan");
                        tvPlanStatus.setText("Upgrade to unlock Live Classes");
                        btnUpgrade.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Join me on DoubtSolve! Use my referral link to get free classes.\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Refer via"));
        } catch (Exception e) {
            // Error
        }
    }
}