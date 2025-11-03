package com.blank_learn.Booking;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.blank_learn.dark.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
 import com.blank_learn.dark.R;
import java.util.Arrays;
import java.util.List;

public class Step1_ClassFragment extends Fragment {

    private OnStepOneListener mListener;
    private RecyclerView recyclerView;
    private ChoiceAdapter adapter;

    public interface OnStepOneListener {
        void onClassSelected(String selectedClass);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStepOneListener) {
            mListener = (OnStepOneListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnStepOtneLisener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step1__class, container, false);

        recyclerView = view.findViewById(R.id.class_recycler_view);
        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        // Prepare the data
        List<String> classes = Arrays.asList("LKG", "UKG", "1", "2", "3", "4", "5", "6", "7", "8");

        // Create the adapter and set the click listener
        // When an item is clicked, it calls the main activity's method
        adapter = new ChoiceAdapter(classes, selectedClass -> {
            // The listener in the adapter gives us the selected item.
            // We pass this data back to the BookingWizardActivity.
            // We add "Class " prefix to match your original data structure.
            mListener.onClassSelected("Class " + selectedClass);
        });

        // Set the layout manager for a 3-column grid
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(adapter);
    }
}