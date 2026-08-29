package com.blank_learn.newversion.O2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blank_learn.dark.databinding.ActivityNewBinding;
import com.blank_learn.demo.ReviewAdapter;
import com.blank_learn.demo.ReviewModel;
import com.blank_learn.home.StoryAdapter;
import com.blank_learn.home.Story_model;
import com.blank_learn.newversion.O1.PhoneAuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewActivity extends AppCompatActivity {

    private ActivityNewBinding binding;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    // Adapters & Lists
    private StoryAdapter storyAdapter;
    private ArrayList<Story_model> storyList;

    private EducatorVideoAdapter educatorVideoAdapter;
    private ArrayList<EducatorVideoModel> educatorVideoList;

    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize Binding
        binding = ActivityNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // 3. Initialize Lists
        storyList = new ArrayList<>();
        educatorVideoList = new ArrayList<>();
        reviewList = new ArrayList<>();

        // 4. Setup Recyclers
        setupStoriesRecyclerView();
        setupEducatorsRecyclerView();
        setupTestimonialsRecyclerView();

        // 5. Load Data
        loadStories();
        loadEducators();
        loadReviews();

        // 6. Click Listeners
        setupClickListeners();

        // 7. Update User Name (Optional)
        updateUI();
    }

    private void updateUI() {
        if (mAuth.getCurrentUser() != null) {
            // Agar user login hai to uska naam dikha sakte hain (Agar DB me save hai)
            binding.tvUserName.setText("Ready to Learn?");
        } else {
            binding.tvUserName.setText("Hello Guest!");
        }
    }

    // --- SETUP & LOAD STORIES ---
    private void setupStoriesRecyclerView() {
        storyAdapter = new StoryAdapter(storyList, this);
        binding.storiesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.storiesRecyclerView.setAdapter(storyAdapter);
    }

    private void loadStories() {
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
    }

    // --- SETUP & LOAD EDUCATORS ---
    private void setupEducatorsRecyclerView() {
        educatorVideoAdapter = new EducatorVideoAdapter(educatorVideoList, this);
        binding.educatorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.educatorsRecyclerView.setAdapter(educatorVideoAdapter);
    }

    private void loadEducators() {
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- SETUP & LOAD REVIEWS ---
    private void setupTestimonialsRecyclerView() {
        reviewAdapter = new ReviewAdapter(this, reviewList);
        binding.testimonialsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.testimonialsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadReviews() {
        databaseRef.child("reviews").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ReviewModel review = data.getValue(ReviewModel.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- CLICK LISTENERS ---
    private void setupClickListeners() {
        View.OnClickListener demoListener = v -> {
            if (mAuth.getCurrentUser() != null) {
                Intent intent = new Intent(NewActivity.this, PlanBuilderActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(NewActivity.this, PhoneAuthActivity.class);
                startActivity(intent);
            }
        };

        // Click listeners for both the card container and the button inside
        binding.cardBookDemo.setOnClickListener(demoListener);
        binding.btnBookDemo.setOnClickListener(demoListener);

        // Locked Progress Card Click - Show Toast or Upsell
        binding.cardStudentProgress.setOnClickListener(v -> {
            Toast.makeText(NewActivity.this, "Book a trial to unlock your progress dashboard!", Toast.LENGTH_LONG).show();
        });
    }
}