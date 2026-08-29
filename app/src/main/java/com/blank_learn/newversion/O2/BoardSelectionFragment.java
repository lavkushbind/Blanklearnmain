package com.blank_learn.newversion.O2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blank_learn.dark.R;

import java.util.Arrays;
import java.util.List;

public class BoardSelectionFragment extends Fragment {

    private PlanBuilderViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the updated layout
        return inflater.inflate(R.layout.fragment_board_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PlanBuilderViewModel.class);

        // Find the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.boardRecyclerView);

        // Set its layout manager to be vertical
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create the list of boards
        List<String> boards = Arrays.asList("CBSE", "ICSE", "State Board", "IB", "IGCSE");

        // Reuse the same SelectionAdapter!
        SelectionAdapter adapter = new SelectionAdapter(boards, selectedBoard -> {
            viewModel.selectedBoard.setValue(selectedBoard);
        });

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);
    }
}