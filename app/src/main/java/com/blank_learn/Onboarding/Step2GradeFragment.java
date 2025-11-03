package com.blank_learn.Onboarding;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.google.android.material.button.MaterialButton;
import java.util.Arrays;
import java.util.List;

public class Step2GradeFragment extends Fragment {

    private ProfileSetupViewModel viewModel;
    private TextView headerText;
    private RecyclerView recyclerView;
    private Button nextButton;
    private GradeAdapter adapter;
    private String selectedGrade = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step2_grade, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI एलिमेंट्स को एक्सेस करें
        headerText = view.findViewById(R.id.header_text_grade);
        recyclerView = view.findViewById(R.id.recycler_view_grades);
        nextButton = view.findViewById(R.id.btn_next_grade);

        // शेयर्ड ViewModel को पाएं
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileSetupViewModel.class);

        // ViewModel से बच्चे का नाम पाएं और हेडिंग में सेट करें
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null && profile.getName() != null) {
                headerText.setText("Great! What grade is " + profile.getName() + " in?");
            }
        });

        setupRecyclerView();

        // "Next" बटन का लॉजिक
        nextButton.setOnClickListener(v -> {
            if (selectedGrade != null) {
                viewModel.setGrade(selectedGrade); // ViewModel में ग्रेड सेव करें
                // अगली स्क्रीन (Step 3) पर जाएं
                ((ProfileSetupActivity) requireActivity()).navigateToFragment(new Step3BoardFragment() );
            }
        });
    }

    private void setupRecyclerView() {
        // यहाँ आप अपनी क्लास की लिस्ट डालें
        List<String> grades = Arrays.asList(
                "Class 3", "Class 4", "Class 5",
                "Class 6", "Class 7", "Class 8",
                "Class 9", "Class 10"
        );

        // Adapter बनाएं और उसे बताएं कि ग्रेड सिलेक्ट होने पर क्या करना है
        adapter = new GradeAdapter(getContext(), grades, grade -> {
            selectedGrade = grade; // सिलेक्टेड ग्रेड को सेव करें
            nextButton.setEnabled(true); // "Next" बटन को एनेबल करें
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 कॉलम की ग्रिड
        recyclerView.setAdapter(adapter);
    }
}