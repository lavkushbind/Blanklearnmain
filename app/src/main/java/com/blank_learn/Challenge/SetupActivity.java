package com.blank_learn.Challenge;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blank_learn.dark.R;

// SetupActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.databinding.ActivitySetupBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private ActivitySetupBinding binding;
    private DatabaseReference questPreferencesRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Handle user not logged in case
            finish();
            return;
        }

        // Personalize greeting
        String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "there";
        binding.tvGreeting.setText("Hi " + userName + "! I'm Sparky.\nLet's personalize your adventure!");

        questPreferencesRef = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()).child("questPreferences");

        setupChipListeners();

        binding.btnCreateProfile.setOnClickListener(v -> savePreferences());
    }

    private void setupChipListeners() {
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        // Listener for the class group
        for (int i = 0; i < binding.chipGroupClass.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupClass.getChildAt(i);
            chip.setOnClickListener(view -> {
                view.startAnimation(bounce);
                updateConfirmationText();
            });
        }

        // Listener for the interests group
        for (int i = 0; i < binding.chipGroupInterests.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupInterests.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                buttonView.startAnimation(bounce);
                updateConfirmationText();
            });
        }
    }

    private void updateConfirmationText() {
        List<String> selectedInterests = new ArrayList<>();
        for (int id : binding.chipGroupInterests.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            selectedInterests.add(chip.getText().toString());
        }

        if (selectedInterests.isEmpty()) {
            binding.tvConfirmation.setText("");
            return;
        }

        StringBuilder confirmation = new StringBuilder("Awesome! We'll craft quests about ");
        for (int i = 0; i < selectedInterests.size(); i++) {
            confirmation.append(selectedInterests.get(i));
            if (i < selectedInterests.size() - 2) {
                confirmation.append(", ");
            } else if (i == selectedInterests.size() - 2) {
                confirmation.append(" and ");
            }
        }
        confirmation.append(" just for you.");
        binding.tvConfirmation.setText(confirmation.toString());
    }

    private void savePreferences() {
        // Validation
        int selectedClassId = binding.chipGroupClass.getCheckedChipId();
        if (selectedClassId == View.NO_ID) {
            Toast.makeText(this, "Please select your class", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedInterests = new ArrayList<>();
        for (int id : binding.chipGroupInterests.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            selectedInterests.add(chip.getText().toString());
        }

        if (selectedInterests.isEmpty()) {
            Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedClass = ((Chip) findViewById(selectedClassId)).getText().toString();

        // Save to Firebase
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("class", selectedClass);
        preferences.put("interests", selectedInterests);

        questPreferencesRef.setValue(preferences).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Created!", Toast.LENGTH_SHORT).show();
                // Navigate to the next screen
                Intent intent = new Intent(SetupActivity.this, ChallengeIntroActivity.class);
                startActivity(intent);
                finish(); // Prevent user from going back to setup
            } else {
                Toast.makeText(this, "Failed to save preferences.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}