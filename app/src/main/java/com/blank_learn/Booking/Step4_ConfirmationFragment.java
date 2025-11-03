package com.blank_learn.Booking;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blank_learn.dark.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;

public class Step4_ConfirmationFragment extends Fragment {

    private OnStepFourListener mListener;

    public interface OnStepFourListener {
        void onConfirmation();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStepFourListener) {
            mListener = (OnStepFourListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnStepFourListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step4__confirmation, container, false);

        TextView summaryText = view.findViewById(R.id.summary_text);
        CheckBox commitmentCheckbox = view.findViewById(R.id.commitment_checkbox);
        Button confirmButton = view.findViewById(R.id.confirm_button);

        // Get data passed from the Activity
        Bundle args = getArguments();
        if (args != null) {
            String selectedClass = args.getString("CLASS");
            String selectedType = args.getString("TYPE");
            String selectedDate = args.getString("DATE");
            String selectedTime = args.getString("TIME");

            String summary = "Class: " + selectedClass + "\n" +
                    "Type: " + selectedType + "\n" +
                    "Date: " + selectedDate + "\n" +
                    "Time: " + selectedTime;
            summaryText.setText(summary);
        }

        confirmButton.setOnClickListener(v -> {
            if (commitmentCheckbox.isChecked()) {
                mListener.onConfirmation();
            } else {
                Toast.makeText(getContext(), "Please confirm your attendance.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}