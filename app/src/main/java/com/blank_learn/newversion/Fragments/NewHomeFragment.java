package com.blank_learn.newversion.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ActivityNewHomeBinding;
import com.blank_learn.home.StoryAdapter;
import com.blank_learn.home.Story_model;
import com.blank_learn.newversion.O2.PlanBuilderActivity;

// Firebase & Analytics Imports
import com.google.firebase.analytics.FirebaseAnalytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class NewHomeFragment extends Fragment {

    private ActivityNewHomeBinding binding;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    // Analytics
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    // Stories
    private ArrayList<Story_model> storyList;
    private StoryAdapter storyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityNewHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Init Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // 2. Init Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        fbLogger = AppEventsLogger.newLogger(requireContext());

        // Log Screen View
        logEvent("screen_view_paid_home", null);

        // 3. Init Lists
        storyList = new ArrayList<>();

        // 4. Setup
        setupStoriesRecyclerView();
        setupClickListeners();
        loadUserName();
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseReference.child("Users").child(user.getUid()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.getValue(String.class);
                                binding.tvUserName.setText(name != null ? name : "User");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void setupStoriesRecyclerView() {
        storyAdapter = new StoryAdapter(storyList, requireContext());
        binding.storiesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.storiesRecyclerView.setAdapter(storyAdapter);

        // Fetch Stories
        databaseReference.child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Story_model storyModel = dataSnapshot.getValue(Story_model.class);
                    if (storyModel != null) {
                        storyModel.setStoryid(dataSnapshot.getKey());
                        storyList.add(storyModel);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupClickListeners() {
        // 1. Join Class
        View.OnClickListener joinListener = v -> {
            logEvent("click_join_class", null);
            checkSubscriptionAndJoinClass();
        };
        binding.cardJoinClass.setOnClickListener(joinListener);
        binding.btnJoinClass.setOnClickListener(joinListener);

        // 2. WhatsApp Support
        if (binding.ivWhatsappSupport != null) {
            binding.ivWhatsappSupport.setOnClickListener(v -> {
                logEvent("click_whatsapp_support", null);
                fetchNumberAndOpenWhatsApp();
            });
        }

        // 3. Share App
        binding.btnShare.setOnClickListener(v -> shareApp());

        // 4. Navigate to Doubt Fragment
        binding.cardAskDoubt.setOnClickListener(v -> {
            logEvent("nav_doubt_solve", null);
            loadFragment(new DoubtSolveFragment());
        });

        // 5. Navigate to Quiz Fragment
        binding.cardPlayQuiz.setOnClickListener(v -> {
            logEvent("nav_quiz", null);
            loadFragment(new QuizFragment());
        });
    }

    private void loadFragment(Fragment fragment) {
        if (getParentFragmentManager() != null) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void checkSubscriptionAndJoinClass() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("HomeFragmentDebug", "Checking subscription for UID: " + currentUser.getUid());

        databaseReference.child("user_subscriptions").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            String orderStatus = snapshot.child("orderStatus").getValue(String.class);

                            // *** CRITICAL FIX: Case-insensitive check for 'paid' ***
                            if (orderStatus != null && orderStatus.trim().toLowerCase(Locale.ENGLISH).equals("paid")) {

                                // We retrieve class details here
                                String meetLink = snapshot.child("class").getValue(String.class);
                                String selectedSlot = snapshot.child("selectedSlot").getValue(String.class);

                                Log.d("HomeFragmentDebug", "Subscription PAID. Link: " + meetLink + ", Slot: " + selectedSlot);

                                if (meetLink != null && !meetLink.isEmpty() && selectedSlot != null) {
                                    // *** TIME VALIDATION IS REMOVED HERE ***
                                    launchMeet(meetLink);
                                } else {
                                    // This fires if status is paid, but link/slot is missing in DB
                                    Toast.makeText(requireContext(), "Class details pending (Link missing in database).", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w("HomeFragmentDebug", "Subscription Status is NOT 'paid'. Status found: " + orderStatus);
                                Toast.makeText(requireContext(), "Active subscription required.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(requireContext(), PlanBuilderActivity.class));
                            }
                        } else {
                            Log.w("HomeFragmentDebug", "Subscription Node does not exist for this user.");
                            Toast.makeText(requireContext(), "Active subscription required.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(requireContext(), PlanBuilderActivity.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("HomeFragmentDebug", "Database Error fetching subscription: ", error.toException());
                    }
                });
    }

    // --- Removed validateTimeAndLaunch method entirely ---

    private void launchMeet(String url) {
        try {
            logEvent("launch_meet_success", null);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not open link or app not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNumberAndOpenWhatsApp() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        databaseReference.child("user_subscriptions").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String supportNumber = "+919235044520";
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("teacher_phone")) {
                                supportNumber = snapshot.child("teacher_phone").getValue(String.class);
                            } else if (snapshot.hasChild("support_phone")) {
                                supportNumber = snapshot.child("support_phone").getValue(String.class);
                            }
                        }
                        openWhatsApp(supportNumber);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        openWhatsApp("+919572569482");
                    }
                });
    }

    private void openWhatsApp(String phoneNumber) {
        try {
            String message = "Hello, I need help regarding my class.";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + URLEncoder.encode(message, "UTF-8");
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "WhatsApp not installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareApp() {
        logEvent("share_app_clicked", null);
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Join DoubtSolve! https://play.google.com/store/apps/details?id=" + requireContext().getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {}
    }

    private void logEvent(String eventName, Bundle params) {
        if (params == null) params = new Bundle();
        mFirebaseAnalytics.logEvent(eventName, params);
        fbLogger.logEvent(eventName, params);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}