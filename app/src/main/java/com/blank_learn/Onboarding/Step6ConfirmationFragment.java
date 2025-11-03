package com.blank_learn.Onboarding;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blank_learn.dark.R;
import com.google.android.material.button.MaterialButton;

public class Step6ConfirmationFragment extends Fragment {

    private ProfileSetupViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step6_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI एलिमेंट्स को एक्सेस करें
        TextView header = view.findViewById(R.id.header_text_confirm);
        TextView summaryName = view.findViewById(R.id.summary_name);
        TextView summaryGradeBoard = view.findViewById(R.id.summary_grade_board);
        TextView summarySubjects = view.findViewById(R.id.summary_subjects);
        Button findTutorButton = view.findViewById(R.id.btn_find_tutor);
        TextView addAnotherLink = view.findViewById(R.id.link_add_another);

        // शेयर्ड ViewModel को पाएं
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileSetupViewModel.class);

        // ViewModel से फाइनल डेटा पाएं और उसे UI पर सेट करें
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                // 1. हेडिंग सेट करें
                header.setText("All Set! " + profile.getName() + "'s profile is ready.");

                // 2. समरी कार्ड भरें
                summaryName.setText("Name: " + profile.getName());
                summaryGradeBoard.setText("Grade: " + profile.getGrade() + " (" + profile.getBoard() + ")");

                // Subjects की लिस्ट को एक कॉमा-सेपरेटेड स्ट्रिंग में बदलें
                String subjectsText = String.join(", ", profile.getSubjects());
                summarySubjects.setText("Focus Subjects: " + subjectsText);
            }
        });

        // "Find a Tutor" बटन का लॉजिक
        findTutorButton.setOnClickListener(v -> {
            // TODO: यहाँ से ऐप की मेन स्क्रीन (जैसे DashboardActivity) पर जाएं
            // Intent intent = new Intent(getActivity(), DashboardActivity.class);
            // startActivity(intent);

            // इस Activity को खत्म कर दें ताकि यूजर 'Back' दबाकर वापस न आ सके
            // if (getActivity() != null) {
            //    getActivity().finish();
            // }
            Toast.makeText(getContext(), "Navigating to Tutor Dashboard...", Toast.LENGTH_SHORT).show();
        });

        // "Add Another Child" लिंक का लॉजिक
        addAnotherLink.setOnClickListener(v -> {
            // TODO: दूसरे बच्चे को जोड़ने का फ्लो शुरू करें
            // इसके लिए आप ViewModel को रीसेट करके पहले स्टेप पर वापस जा सकते हैं।
            Toast.makeText(getContext(), "Functionality to add another child coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
}