package com.blank_learn.newversion.language;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;

import java.util.Arrays;
import java.util.List;

public class LanguageSelectionFragment extends Fragment implements LanguageAdapter.OnLanguageSelectedListener {

    private TutorViewModel viewModel;
    private LanguageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the fragment_language_setup layout
        return inflater.inflate(R.layout.fragment_language_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TutorViewModel.class);

        // --- 1. Native Language Setup (Assuming Hindi is selected by default/fixed) ---
        // We will just update the button text visually and set the value in ViewModel
        Button btnNative = view.findViewById(R.id.btnNativeLang);
        btnNative.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Native Language is set to Hindi.", Toast.LENGTH_SHORT).show()
        );
        // Default set value to trigger initial validation in Activity
        viewModel.nativeLanguage.setValue("Hindi");


        // --- 2. Target Language Setup (RecyclerView) ---

        // Prepare Data (You need these drawable icons in your project)
        List<LanguageModel> targetLangs = Arrays.asList(
                new LanguageModel("English", "en", R.drawable.ic_flame_legendary),
                new LanguageModel("French", "fr", R.drawable.ic_flame_legendary),
                new LanguageModel("Spanish", "es", R.drawable.ic_flame_legendary),
                new LanguageModel("German", "de", R.drawable.ic_flame_legendary),
                new LanguageModel("Japanese", "ja", R.drawable.ic_flame_legendary),
                new LanguageModel("Korean", "ko", R.drawable.ic_flame_legendary)
        );

        // Adapter setup (rvTargetLanguages)
        adapter = new LanguageAdapter(targetLangs, this);
        RecyclerView rv = view.findViewById(R.id.rvTargetLanguages);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rv.setAdapter(adapter);
    }

    @Override
    public void onLanguageSelected(String langCode) {
        // Update ViewModel (This triggers validation in TutorSetupActivity)
        viewModel.targetLanguage.setValue(langCode);
    }
}