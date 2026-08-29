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

public class SubjectSelectionFragment extends Fragment {

    private PlanBuilderViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PlanBuilderViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.subjectRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));  

        List<String> subjects = Arrays.asList(
                "Mathematics", "Science","hindi",
                "English",
                "Physics",
                "Chemistry",
                "Biology"
        );

        // SelectionAdapter की जगह MultiSelectionAdapter का उपयोग करें
        MultiSelectionAdapter adapter = new MultiSelectionAdapter(subjects, selectedItems -> {
            // ViewModel को सिलेक्टेड आइटम्स की पूरी लिस्ट भेजें
            viewModel.selectedSubjects.setValue(selectedItems);
        });

        recyclerView.setAdapter(adapter);
    }
}