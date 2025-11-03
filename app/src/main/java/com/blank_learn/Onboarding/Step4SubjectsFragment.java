package com.blank_learn.Onboarding;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blank_learn.dark.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class Step4SubjectsFragment extends Fragment {

    private ProfileSetupViewModel viewModel;
    private ChipGroup chipGroup;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step4_subjects, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chipGroup = view.findViewById(R.id.chip_group_subjects);
        nextButton = view.findViewById(R.id.btn_next_subjects);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileSetupViewModel.class);

        // ChipGroup में कोई भी चिप सिलेक्ट या डीसिलेक्ट होने पर यह Listener चलेगा
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // अगर कोई भी चिप सिलेक्टेड है (checkedIds खाली नहीं है), तो Next बटन को एनेबल करें
            nextButton.setEnabled(!checkedIds.isEmpty());
        });

        nextButton.setOnClickListener(v -> {
            List<String> selectedSubjects = new ArrayList<>();
            // सभी सिलेक्टेड चिप IDs को पाएं
            List<Integer> checkedChipIds = chipGroup.getCheckedChipIds();

            for (Integer id : checkedChipIds) {
                Chip chip = chipGroup.findViewById(id);
                selectedSubjects.add(chip.getText().toString());
            }

            // ViewModel में सब्जेक्ट्स की लिस्ट सेव करें
            viewModel.setSubjects(selectedSubjects);

            // अगली स्क्रीन (Step 5) पर जाएं
            ((ProfileSetupActivity) requireActivity()).navigateToFragment(new Step5TimingsFragment() );
        });
    }
}