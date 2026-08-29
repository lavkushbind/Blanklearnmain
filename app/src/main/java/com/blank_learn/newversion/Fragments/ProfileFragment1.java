package com.blank_learn.newversion.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;
import com.bumptech.glide.Glide;
import com.blank_learn.newversion.O1.PhoneAuthActivity;
import com.blank_learn.newversion.O2.HelpSupportActivity;
import com.blank_learn.newversion.O2.PlanBuilderActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.razorpay.Checkout;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ProfileFragment1 extends Fragment {

    private static final String RAZORPAY_KEY_ID = "rzp_live_6vd9RApruseTAi";
    private static final String TAG = "ProfileFragment1";

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef, feesRef, subRef;
    private StorageReference storageRef;

    // UI Views (MUST match your new XML structure)
    private TextView tvName, tvPhone, tvPoints, tvClasses, tvRank, tvPlanName, tvPlanStatus;
    private TextView tvFeePlanType, tvStartDate, tvExpiryDate, tvDaysStatus;
    private LinearLayout layoutPaid, layoutUnpaid;
    private ProgressBar pbDays;
    private View btnPay1499, btnPay4000, btnUpgrade, btnLogout, btnRefer, btnEditProfile, btnHelp;
    private CardView cardProfileImage;
    private ImageView ivUserProfile;

    private ActivityResultLauncher<String> mGetContent;
    private ProgressDialog progressDialog;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        feesRef = FirebaseDatabase.getInstance().getReference("FeesPayments");
        subRef = FirebaseDatabase.getInstance().getReference("user_subscriptions");
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImages");

        // Analytics Init (Assuming you still need them, although imported classes were removed above)
        // Removed FirebaseAnalytics/AppEventsLogger imports/init to keep it clean, assuming they are not required in ProfileFragment context.

        initViews(view);
        setupImagePicker();

        loadUserProfile();
        checkFeesStatus(); // Checks local payment receipt for expiry visual
        checkSubscriptionStatus(); // Checks 'user_subscriptions' for class access & link
        setupClickListeners();
    }

    private void initViews(View view) {
        // Initialize all views based on the provided XML IDs
        tvName = view.findViewById(R.id.tvUserName);
        tvPhone = view.findViewById(R.id.tvUserEmail);
        tvPoints = view.findViewById(R.id.tvStatPoints);
        tvClasses = view.findViewById(R.id.tvStatClasses);
        tvRank = view.findViewById(R.id.tvStatRank);
        tvPlanName = view.findViewById(R.id.tvPlanName);
        tvPlanStatus = view.findViewById(R.id.tvPlanStatus);
        ivUserProfile = view.findViewById(R.id.ivUserProfile);
        cardProfileImage = view.findViewById(R.id.cardProfileImage);

        layoutPaid = view.findViewById(R.id.layoutPaid);
        layoutUnpaid = view.findViewById(R.id.layoutUnpaid);
        tvFeePlanType = view.findViewById(R.id.tvFeePlanType);
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvExpiryDate = view.findViewById(R.id.tvExpiryDate);
        tvDaysStatus = view.findViewById(R.id.tvDaysStatus);
        pbDays = view.findViewById(R.id.pbDays);

        // Important: Use findViewById on the actual Button view instances based on your XML structure
        btnPay1499 = view.findViewById(R.id.btnPay1499);
        btnPay4000 = view.findViewById(R.id.btnPay4000);
        btnUpgrade = view.findViewById(R.id.btnUpgrade);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnRefer = view.findViewById(R.id.cardRefer);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        progressDialog = new ProgressDialog(requireContext());
    }

    private void setupImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                imageUri = result;
                ivUserProfile.setImageURI(imageUri);
                uploadImageToFirebase();
            }
        });
    }

    private void setupClickListeners() {
        cardProfileImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // UPGRADE BUTTON NOW TRIGGERS SUBSCRIPTION CHECK/LAUNCH
        btnUpgrade.setOnClickListener(v -> {
            logEvent("click_profile_upgrade", null);
            checkSubscriptionAndLaunchClass(); // NEW FUNCTION
        });

        btnRefer.setOnClickListener(v -> shareApp());
        btnHelp.setOnClickListener(v -> startActivity(new Intent(requireContext(), HelpSupportActivity.class)));

        // Payment Buttons (If user is currently on Free Plan or Expired)
        btnPay1499.setOnClickListener(v -> startPayment(1499, "Individual Monthly Fees"));
        btnPay4000.setOnClickListener(v -> startPayment(4000, "Group Class Fees (3 Students)"));

        btnLogout.setOnClickListener(v -> {
            logEvent("logout_clicked", null);
            mAuth.signOut();
            Intent intent = new Intent(requireContext(), PhoneAuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // --- NEW FUNCTION: Checks subscription and launches link immediately ---
    private void checkSubscriptionAndLaunchClass() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        logEvent("profile_join_attempt", null);

        subRef.child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String orderStatus = snapshot.child("orderStatus").getValue(String.class);

                            // Check if status is 'paid' (case-insensitive)
                            if (orderStatus != null && orderStatus.trim().toLowerCase(Locale.ENGLISH).equals("paid")) {

                                // GET LINK (Assuming the key is 'class' based on previous context)
                                String meetLink = snapshot.child("class").getValue(String.class);

                                if (meetLink != null && !meetLink.isEmpty()) {
                                    Log.i(TAG, "Subscription Paid. Launching Meet link immediately.");
                                    launchMeet(meetLink);
                                } else {
                                    Toast.makeText(requireContext(), "Class link not yet configured for your subscription.", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                // Status is not paid (e.g., pending or null)
                                Toast.makeText(requireContext(), "Active subscription required to join live class.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(requireContext(), PlanBuilderActivity.class));
                            }
                        } else {
                            // Subscription node does not exist
                            Toast.makeText(requireContext(), "No active subscription found. Please purchase a plan.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(requireContext(), PlanBuilderActivity.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database Error fetching subscription status: ", error.toException());
                    }
                });
    }
    // ---------------------------------------------------------------------


    private void startPayment(int amount, String description) {
        // Razorpay setup remains the same...
        Checkout checkout = new Checkout();
        checkout.setKeyID(RAZORPAY_KEY_ID);
        try {
            JSONObject options = new JSONObject();
            options.put("name", "BlankLearn");
            options.put("description", description);
            options.put("theme.color", "#FFD700");
            options.put("currency", "INR");
            options.put("amount", amount * 100);

            JSONObject prefill = new JSONObject();
            prefill.put("contact", tvPhone.getText().toString());
            options.put("prefill", prefill);

            checkout.open(getActivity(), options);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFeesStatus() {
        if (mAuth.getCurrentUser() == null) return;
        feesRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                if (snapshot.exists()) {
                    Long payDate = snapshot.child("paymentDate").getValue(Long.class);
                    Long expDate = snapshot.child("expiryDate").getValue(Long.class);
                    String plan = snapshot.child("planType").getValue(String.class);
                    long now = System.currentTimeMillis();

                    if (expDate != null && now < expDate) {
                        layoutUnpaid.setVisibility(View.GONE);
                        layoutPaid.setVisibility(View.VISIBLE);

                        // Update UI fields based on XML IDs
                        tvFeePlanType.setText(plan != null ? plan.toUpperCase() : "PAID PLAN");

                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                        tvStartDate.setText(payDate != null ? sdf.format(new Date(payDate)) : "--");
                        tvExpiryDate.setText(sdf.format(new Date(expDate)));

                        long total = expDate - payDate;
                        long passed = now - payDate;
                        long remaining = TimeUnit.MILLISECONDS.toDays(expDate - now);

                        int progress = (int) ((passed * 100) / total);
                        pbDays.setProgress(progress);
                        tvDaysStatus.setText(remaining + " Days Remaining");
                    } else {
                        layoutUnpaid.setVisibility(View.VISIBLE);
                        layoutPaid.setVisibility(View.GONE);
                    }
                } else {
                    layoutUnpaid.setVisibility(View.VISIBLE);
                    layoutPaid.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading FeesPayments: " + error.getMessage());
            }
        });
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        dbRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || !snapshot.exists()) return;

                tvName.setText(snapshot.child("name").getValue(String.class));
                String ph = snapshot.child("phone").getValue(String.class);
                tvPhone.setText(ph != null ? ph : mAuth.getCurrentUser().getPhoneNumber());

                // Assuming stats data comes from here or another node (Add specific stat loading if needed)
                // tvPoints.setText(...);
                // tvClasses.setText(...);
                // tvRank.setText(...);

                String pic = snapshot.child("profilepic").getValue(String.class);
                if (pic != null && !pic.isEmpty()) {
                    Glide.with(requireContext()).load(pic).placeholder(R.drawable.userprofile).into(ivUserProfile);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkSubscriptionStatus() {
        if (mAuth.getCurrentUser() == null) return;
        subRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                if (snapshot.exists() && "paid".equals(snapshot.child("orderStatus").getValue(String.class))) {

                    // Update the Current Subscription Card (Card #4)
                    tvPlanName.setText(snapshot.child("planName").getValue(String.class) != null ?
                            snapshot.child("planName").getValue(String.class) : "Premium");
                    tvPlanStatus.setText("Active • Unlimited Access");
                    btnUpgrade.setVisibility(View.GONE); // Hide upgrade button if already paid
                } else {
                    tvPlanName.setText("Free Plan");
                    tvPlanStatus.setText("Upgrade to unlock Live Classes");
                    btnUpgrade.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            progressDialog.setMessage("Uploading Profile Picture...");
            progressDialog.show();
            StorageReference fileRef = storageRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            fileRef.putFile(imageUri).addOnSuccessListener(task -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                dbRef.child(mAuth.getCurrentUser().getUid()).child("profilepic").setValue(uri.toString());
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Profile Picture Updated", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Name");
        final EditText input = new EditText(requireContext());
        input.setText(tvName.getText().toString());
        builder.setView(input);
        builder.setPositiveButton("Save", (d, w) -> {
            String n = input.getText().toString().trim();
            if (!n.isEmpty() && !n.equals(tvName.getText().toString())) {
                dbRef.child(mAuth.getCurrentUser().getUid()).child("name").setValue(n);
                Toast.makeText(requireContext(), "Name updated.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (d, w) -> d.cancel());
        builder.show();
    }

    private void launchMeet(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Meet link", e);
            Toast.makeText(getContext(), "Could not open class link. Check if Meet app is installed.", Toast.LENGTH_LONG).show();
        }
    }

    private void logEvent(String eventName, Bundle params) {
        // Since FirebaseAnalytics and AppEventsLogger were removed from imports for simplicity,
        // this function must be implemented if you use analytics.
        // If you restore the analytics imports, put your logging logic here.
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Join BlankLearn! https://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
        startActivity(Intent.createChooser(intent, "Share"));
    }
}