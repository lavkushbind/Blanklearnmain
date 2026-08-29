package com.blank_learn.newversion.Ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;

public class AiFragment extends Fragment {

    // Global Declarations
    private AutoCompleteTextView ddNative, ddTarget;
    private LinearLayout cardLvl1, cardLvl2, cardLvl3;
    private String selectedLevel = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Assuming your setup layout is R.layout.activity_ai_setup or R.layout.fragment_ai_setup
        return inflater.inflate(R.layout.fragment_ai, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. VIEWS INITIALIZATION ---
        ddNative = view.findViewById(R.id.ddNativeLang);
        ddTarget = view.findViewById(R.id.ddTargetLang);
        cardLvl1 = view.findViewById(R.id.cardLevel1);
        cardLvl2 = view.findViewById(R.id.cardLevel2);
        cardLvl3 = view.findViewById(R.id.cardLevel3);

        if (ddNative == null || ddTarget == null) {
            Toast.makeText(requireContext(), "Error: Dropdown views not found in XML!", Toast.LENGTH_LONG).show();
            return;
        }

        setupDropdowns();

        // Setup Level Selection Logic
        cardLvl1.setOnClickListener(v -> selectLevel(1));
        cardLvl2.setOnClickListener(v -> selectLevel(2));
        cardLvl3.setOnClickListener(v -> selectLevel(3));

        // Start Button
        view.findViewById(R.id.btnStartLearning).setOnClickListener(v -> {
            String nativeLang = ddNative.getText().toString();
            String targetLang = ddTarget.getText().toString();

            if (selectedLevel.isEmpty()) {
                Toast.makeText(requireContext(), "Please select your level", Toast.LENGTH_SHORT).show();
                return;
            }

            savePreferences(nativeLang, targetLang, selectedLevel);
            navigateToLearningMode();
        });
    }

    private void navigateToLearningMode() {
        Fragment nextFragment;

        switch (selectedLevel) {
            case "Absolute Beginner":
                nextFragment = new FragWordPractice();
                break;
            case "Beginner":
                nextFragment = new FragRolePlay();
                break;
            case "Professional":
                nextFragment = new FragVoiceCall();
                break;
            default:
                Toast.makeText(requireContext(), "Invalid level selected.", Toast.LENGTH_SHORT).show();
                return;
        }

        // --- THE CRITICAL FIX ---
        // Check if the hosting Activity is AiSetupActivity and call its loadFragment method
        if (getActivity() instanceof AiSetupActivity) {
            ((AiSetupActivity) getActivity()).loadFragment(nextFragment);
        } else {
            Toast.makeText(requireContext(), "Navigation Error: Host activity not found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDropdowns() {
        String[] languages = {"Hindi", "English", "Spanish", "French", "German", "Japanese"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, languages);

        ddNative.setAdapter(adapter);
        ddTarget.setAdapter(adapter);

        ddNative.setText("Hindi", false);
        ddTarget.setText("English", false);
    }

    private void selectLevel(int level) {
        // Reset all
        cardLvl1.setSelected(false);
        cardLvl2.setSelected(false);
        cardLvl3.setSelected(false);

        // Set Selected
        if (level == 1) {
            cardLvl1.setSelected(true);
            selectedLevel = "Absolute Beginner";
        } else if (level == 2) {
            cardLvl2.setSelected(true);
            selectedLevel = "Beginner";
        } else {
            cardLvl3.setSelected(true);
            selectedLevel = "Professional";
        }
    }

    private void savePreferences(String nat, String tgt, String lvl) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AiTutorPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("native_lang", nat);
        editor.putString("target_lang", tgt);
        editor.putString("user_level", lvl);
        editor.apply();
    }
}