package com.blank_learn.newversion.O2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blank_learn.dark.R;

import java.util.Arrays;
import java.util.List;

public class ClassSelectionFragment extends Fragment {

    private PlanBuilderViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use requireActivity() to get the ViewModel scoped to the Activity
        viewModel = new ViewModelProvider(requireActivity()).get(PlanBuilderViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.classRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<String> classes = Arrays.asList("LKG", "UKG", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th");

        SelectionAdapter adapter = new SelectionAdapter(classes, selectedClass -> {
            viewModel.selectedClass.setValue(selectedClass);
        });

        recyclerView.setAdapter(adapter);
    }
}