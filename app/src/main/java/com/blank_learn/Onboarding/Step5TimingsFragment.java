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

import com.blank_learn.dark.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class Step5TimingsFragment extends Fragment {

    private ProfileSetupViewModel viewModel;
    private TextView headerText;
    private ChipGroup chipGroup;
    private Button finishButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step5_timings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI एलिमेंट्स को एक्सेस करें
        headerText = view.findViewById(R.id.header_text_timings);
        chipGroup = view.findViewById(R.id.chip_group_timings);
        finishButton = view.findViewById(R.id.btn_finish_setup);

        // शेयर्ड ViewModel को पाएं
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileSetupViewModel.class);

        // ViewModel से बच्चे का नाम पाएं और हेडिंग में सेट करें
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null && profile.getName() != null) {
                headerText.setText("When is the best time for " + profile.getName() + " to learn?");
            }
        });

        // जब कोई चिप सिलेक्ट हो तो 'Finish Setup' बटन को एनेबल करें
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // क्योंकि यह singleSelection है, checkedIds में या तो 0 या 1 ID होगी
            finishButton.setEnabled(!checkedIds.isEmpty());
        });

        // 'Finish Setup' बटन का लॉजिक
        finishButton.setOnClickListener(v -> {
            // सिलेक्टेड चिप की ID पाएं
            int selectedChipId = chipGroup.getCheckedChipId();

            if (selectedChipId != View.NO_ID) { // NO_ID का मतलब -1, यानी कुछ भी सिलेक्टेड नहीं है
                Chip selectedChip = chipGroup.findViewById(selectedChipId);
                String selectedTiming = selectedChip.getText().toString();

                // ViewModel में समय को सेव करें
                viewModel.setPreferredTiming(selectedTiming);

                // आखिरी Confirmation स्क्रीन (Step 6) पर जाएं
                ((ProfileSetupActivity) requireActivity()).navigateToFragment(new Step6ConfirmationFragment() );
            }
        });
    }
}