package com.blank_learn.newversion.language;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blank_learn.dark.R;

public class TutorSetupActivity extends AppCompatActivity {

    private TutorViewModel viewModel;
    private ImageView btnBack;
    private Button btnNext;

    // Flow Management
    private int currentStep = 0;
    private final Fragment[] steps = new Fragment[]{
            new LanguageSelectionFragment(),
            new LevelSelectionFragment(),
            // Future step: User Profile/Review
    };
    private final int totalSteps = steps.length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_setup);

        viewModel = new ViewModelProvider(this).get(TutorViewModel.class);

        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);

        // Initial setup
        showStep(currentStep);
        observeViewModel();

        btnNext.setOnClickListener(v -> handleNextStep());
        btnBack.setOnClickListener(v -> goBack());
    }

    private void showStep(int step) {
        // Update Back Button Visibility
        btnBack.setVisibility(step > 0 ? View.VISIBLE : View.INVISIBLE);

        // Load Fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, steps[step])
                .commit();

        // Always validate after loading a new step
        validateCurrentStep();
    }

    private void handleNextStep() {
        if (currentStep < totalSteps - 1) {
            currentStep++;
            showStep(currentStep);
        } else {
            // Final Step: Launch the Live Tutor Mode!
            launchLiveTutor();
        }
    }

    private void goBack() {
        if (currentStep > 0) {
            currentStep--;
            showStep(currentStep);
        } else {
            super.onBackPressed();
        }
    }

    private void observeViewModel() {
        // Observe changes to language selections
        viewModel.targetLanguage.observe(this, lang -> {
            if (currentStep == 0) validateCurrentStep();
        });

        // Observe changes to level selection
        viewModel.selectedLevel.observe(this, level -> {
            if (currentStep == 1) validateCurrentStep();
        });
    }

    private void validateCurrentStep() {
        boolean isEnabled = false;

        switch (currentStep) {
            case 0: // Language Selection
                // Check if both Native and Target are selected
                isEnabled = viewModel.nativeLanguage.getValue() != null &&
                        !viewModel.nativeLanguage.getValue().isEmpty() &&
                        viewModel.targetLanguage.getValue() != null &&
                        !viewModel.targetLanguage.getValue().isEmpty();
                break;
            case 1: // Level Selection
                // Check if Level is selected
                isEnabled = viewModel.selectedLevel.getValue() != null &&
                        !viewModel.selectedLevel.getValue().isEmpty();
                break;
        }

        // Apply visual feedback
        btnNext.setEnabled(isEnabled);
        btnNext.setAlpha(isEnabled ? 1.0f : 0.5f);
        btnNext.setText(currentStep == totalSteps - 1 ? "Start Learning" : "Continue");
    }

    private void launchLiveTutor() {
        // Here, we launch the TutorChatFragment/Activity in a full-screen mode
        // and pass the selected settings (Language, Level)
        Toast.makeText(this, "Starting " + viewModel.targetLanguage.getValue() + " conversation!", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, LiveTutorActivity.class); // Create this next
        intent.putExtra("NATIVE_LANG", viewModel.nativeLanguage.getValue());
        intent.putExtra("TARGET_LANG", viewModel.targetLanguage.getValue());
        intent.putExtra("LEVEL", viewModel.selectedLevel.getValue());
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }
}