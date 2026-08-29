package com.blank_learn.newversion.Fragments;

import android.content.Intent;
import android.os.Bundle;
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
// Ensure this matches your XML filename (fragment_new_free_home.xml)
import com.blank_learn.dark.databinding.ActivityNewBinding;
import com.blank_learn.demo.ReviewAdapter;
import com.blank_learn.demo.ReviewModel;
import com.blank_learn.home.StoryAdapter;
import com.blank_learn.home.Story_model;
import com.blank_learn.newversion.O1.PhoneAuthActivity;
import com.blank_learn.newversion.O2.EducatorVideoAdapter;
import com.blank_learn.newversion.O2.EducatorVideoModel;
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

import java.util.ArrayList;
import java.util.List;

public class NewFreeHomeFragment extends Fragment {

    private ActivityNewBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    // Analytics
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    // Adapters
    private StoryAdapter storyAdapter;
    private ArrayList<Story_model> storyList;
    private EducatorVideoAdapter educatorVideoAdapter;
    private ArrayList<EducatorVideoModel> educatorVideoList;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityNewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Init Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // 2. Init Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        fbLogger = AppEventsLogger.newLogger(requireContext());

        // Log Screen View
        logEvent("screen_view_free_home", null);

        // 3. Init Lists
        storyList = new ArrayList<>();
        educatorVideoList = new ArrayList<>();
        reviewList = new ArrayList<>();

        // 4. Setup
        setupRecyclers();
        loadData();
        setupClickListeners();
        loadUserName();
    }

    private void loadUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseRef.child("Users").child(user.getUid()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.getValue(String.class);
                                if (name != null) binding.tvUserName.setText(name);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        } else {
            binding.tvUserName.setText("Guest User");
        }
    }

    private void setupRecyclers() {
        // Stories
        storyAdapter = new StoryAdapter(storyList, requireContext());
        binding.storiesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.storiesRecyclerView.setAdapter(storyAdapter);

        // Educators
        educatorVideoAdapter = new EducatorVideoAdapter(educatorVideoList, requireContext());
        binding.educatorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.educatorsRecyclerView.setAdapter(educatorVideoAdapter);

        // Reviews
        reviewAdapter = new ReviewAdapter(requireContext(), reviewList);
        binding.testimonialsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.testimonialsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadData() {
        // 1. Stories
        databaseRef.child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Story_model model = data.getValue(Story_model.class);
                    if (model != null) {
                        model.setStoryid(data.getKey());
                        storyList.add(model);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Educators
        databaseRef.child("VideoUploads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                educatorVideoList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    EducatorVideoModel model = data.getValue(EducatorVideoModel.class);
                    if (model != null) {
                        educatorVideoList.add(model);
                    }
                }
                educatorVideoAdapter.notifyDataSetChanged();

                // Analytics: Log Video List Load
                Bundle params = new Bundle();
                params.putInt("video_count", educatorVideoList.size());
                logEvent("video_list_loaded", params);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3. Reviews
        databaseRef.child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ReviewModel model = data.getValue(ReviewModel.class);
                    if (model != null) reviewList.add(model);
                }
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupClickListeners() {
        // 1. Book Demo
        View.OnClickListener demoListener = v -> {
            logEvent("click_book_demo", null); // Analytics

            if (mAuth.getCurrentUser() != null) {
                startActivity(new Intent(requireContext(), PlanBuilderActivity.class));
            } else {
                startActivity(new Intent(requireContext(), PhoneAuthActivity.class));
            }
        };

        binding.cardBookDemo.setOnClickListener(demoListener);
        binding.btnBookDemo.setOnClickListener(demoListener);

        // 2. Profile Click
        binding.cardProfile.setOnClickListener(v -> {
//            if (getActivity() instanceof MainActivity) {
//                ((MainActivity) getActivity()).navigateToFragment(new AiFragment());
//
//
//            } else {
//                Toast.makeText(requireContext(), "Navigation Error: Could not find host activity.", Toast.LENGTH_SHORT).show();
//            }
        });

        // 3. Locked Progress
        binding.cardStudentProgress.setOnClickListener(v -> {

//            Intent intent = new Intent(requireContext(), com.newversion.language.TutorSetupActivity.class);
//            startActivity(intent);

            logEvent("click_locked_progress", null);
            Toast.makeText(requireContext(), "Book a trial to unlock progress!", Toast.LENGTH_SHORT).show();
        });

        // 4. Share App
        binding.btnShare.setOnClickListener(v -> shareApp());

        // 5. Navigate to Doubt
        binding.cardAskDoubt.setOnClickListener(v -> {
            logEvent("nav_doubt_solve", null);
            loadFragment(new DoubtSolveFragment());
        });

        // 6. Navigate to Quiz
        binding.cardPlayQuiz.setOnClickListener(v -> {
            logEvent("nav_quiz", null);
            loadFragment(new QuizFragment());
        });
    }

    private void shareApp() {
        logEvent("share_app_clicked", null);
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Hey! Join DoubtSolve learning app: \n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + requireContext().getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {}
    }

    /**
     * Helper to Log Analytics Events
     */
    private void logEvent(String eventName, Bundle params) {
        if (params == null) params = new Bundle();
        // Google Analytics
        mFirebaseAnalytics.logEvent(eventName, params);
        // Facebook Pixel/SDK
        fbLogger.logEvent(eventName, params);
    }

    private void loadFragment(Fragment fragment) {
        if (getParentFragmentManager() != null) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            // 'R.id.container' must match the ID in activity_main.xml
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}