package com.blank_learn.newversion.language;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
// Assuming TutorViewModel, LevelAdapter, and LevelModel are accessible (e.g., in this package or properly imported)

import java.util.Arrays;
import java.util.List;

// Note: Aapko LevelAdapter, LevelModel aur TutorViewModel ko sahi package mein banana hoga.
// Agar ye current package mein nahi hain, to yahan imports chahiye honge.

public class LevelSelectionFragment extends Fragment implements LevelAdapter.OnLevelSelectedListener {

    private TutorViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_level_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel Setup (Assuming TutorViewModel is accessible)
        viewModel = new ViewModelProvider(requireActivity()).get(TutorViewModel.class);

        // --- Data Preparation ---
        // Assuming LevelModel is accessible
        List<LevelModel> levels = Arrays.asList(
                new LevelModel(
                        "Absolute Beginner",
                        "I know very few words. Focus on basic phrases and reading simple sentences. (Shabd aur uchcharan par dhyaan.)",
                        R.drawable.ic_flame_legendary
                ),
                new LevelModel(
                        "Beginner",
                        "I understand basic phrases but struggle to speak in real-time. Start with simple role-playing scenarios.",
                        R.drawable.ic_flame_legendary
                ),
                new LevelModel(
                        "Intermediate",
                        "I can hold conversations but need better grammar, fluency, and complex vocabulary.",
                        R.drawable.ic_flame_legendary
                ),
                new LevelModel(
                        "Professional",
                        "I speak fluently, but need high-level correction and academic discussion practice.",
                        R.drawable.ic_flame_legendary
                )
        );

        // --- RecyclerView Setup ---
        RecyclerView rv = view.findViewById(R.id.rvLevels);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter setup (Assuming LevelAdapter is accessible)
        // CRITICAL FIX: 'this' is passed successfully because the Fragment implements the Listener
        LevelAdapter adapter = new LevelAdapter(levels, this);
        rv.setAdapter(adapter);
    }

    /**
     * CRITICAL: Implementation of the OnLevelSelectedListener interface method.
     * This method is called by the adapter when a user clicks a level card.
     */
    @Override
    public void onLevelSelected(String levelName) {
        // Update ViewModel (This tells the hosting Activity that selection is complete)
        viewModel.selectedLevel.setValue(levelName);
        Toast.makeText(requireContext(), "Level set to: " + levelName, Toast.LENGTH_SHORT).show();
    }
}