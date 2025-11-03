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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class Step1NameFragment extends Fragment {

    private ProfileSetupViewModel viewModel; // सभी स्क्रीन्स के लिए कॉमन डेटा होल्डर
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private Button nextButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML लेआउट को इस Fragment से जोड़ें
        return inflater.inflate(R.layout.fragment_step1_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // हमारी Activity से शेयर्ड ViewModel को पाएं
        // यह ViewModel डेटा को एक स्क्रीन से दूसरी स्क्रीन तक ले जाएगा
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileSetupViewModel.class);

        // XML में बनाए गए UI एलिमेंट्स को यहाँ एक्सेस करें
        nameInputLayout = view.findViewById(R.id.name_input_layout);
        nameEditText = view.findViewById(R.id.et_child_name);
        nextButton = view.findViewById(R.id.btn_next);

        // "Next" बटन पर क्लिक करने पर क्या हो, यह यहाँ लिखें
        nextButton.setOnClickListener(v -> {
            // यूजर द्वारा टाइप किया गया नाम निकालें और फालतू स्पेस हटा दें
            String name = nameEditText.getText().toString().trim();

            // जाँचें कि नाम खाली तो नहीं है
            if (name.isEmpty()) {
                // अगर खाली है, तो एक एरर मैसेज दिखाएं
                nameInputLayout.setError("Name cannot be empty");
            } else {
                // अगर नाम सही है, तो एरर हटा दें (अगर पहले दिखाया गया हो)
                nameInputLayout.setError(null);

                // नाम को ViewModel में सेव करें ताकि अगली स्क्रीन उसे इस्तेमाल कर सके
                viewModel.setName(name);

                // अपनी मेन Activity को बताएं कि अब अगली स्क्रीन (Step 2) दिखानी है
                // यह मानते हुए कि आपकी Activity का नाम ProfileSetupActivity है
                ((ProfileSetupActivity) requireActivity()).navigateToFragment(new Step2GradeFragment() );
            }
        });
    }
}