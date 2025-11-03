package com.blank_learn.Onboarding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blank_learn.dark.R;
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
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class Step3BoardFragment extends Fragment {

    private ProfileSetupViewModel viewModel;
    private TextView headerText;
    private RecyclerView recyclerView;
    private Button nextButton;
    private BoardAdapter adapter;
    private String selectedBoardName = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step3_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        headerText = view.findViewById(R.id.header_text_board);
        recyclerView = view.findViewById(R.id.recycler_view_boards);
        nextButton = view.findViewById(R.id.btn_next_board);

        viewModel = new ViewModelProvider(requireActivity()).get(ProfileSetupViewModel.class);

        // ViewModel से बच्चे का नाम पाएं और हेडिंग में सेट करें
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null && profile.getName() != null) {
                headerText.setText("Which educational board does " + profile.getName() + " follow?");
            }
        });

        setupRecyclerView();

        nextButton.setOnClickListener(v -> {
            if (selectedBoardName != null) {
                viewModel.setBoard(selectedBoardName); // ViewModel में बोर्ड सेव करें
                // अगली स्क्रीन (Step 4) पर जाएं
                ((ProfileSetupActivity) requireActivity()).navigateToFragment(new Step4SubjectsFragment() );
            }
        });
    }

    private void setupRecyclerView() {
        // बोर्ड्स की लिस्ट लोगो के साथ बनाएं
        List<Board> boards = new ArrayList<>();
        boards.add(new Board("CBSE", R.drawable.lastlogo));
        boards.add(new Board("ICSE", R.drawable.background_logo));
        boards.add(new Board("State Board", R.drawable.logofix));
        boards.add(new Board("Other", R.drawable.background_logo)); // 'Other' के लिए भी एक लोगो बना लें

        // Adapter बनाएं
        adapter = new BoardAdapter(getContext(), boards, board -> {
            selectedBoardName = board.getName();
            nextButton.setEnabled(true);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }
}