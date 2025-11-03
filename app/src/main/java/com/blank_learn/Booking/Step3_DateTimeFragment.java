package com.blank_learn.Booking;

import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blank_learn.dark.R;

import android.content.Context;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
// ... other imports from Step1 ...
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.blank_learn.dark.R;// ... other imports ...
import androidx.fragment.app.Fragment;
import com.blank_learn.dark.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Step3_DateTimeFragment extends Fragment {

    private OnStepThreeListener mListener;
    private String selectedDate, selectedTime;

    // VERY IMPORTANT: We now store the selected VIEW (the card), not the TextView
    private View selectedDateView, selectedTimeView;

    public interface OnStepThreeListener {
        void onDateTimeSelected(String date, String time);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStepThreeListener) {
            mListener = (OnStepThreeListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnStepThreeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step3__date_time, container, false);
        LinearLayout dateContainer = view.findViewById(R.id.date_container);
        LinearLayout timeContainer = view.findViewById(R.id.time_container);

        // Populate Dates
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        SimpleDateFormat backendFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (int i = 0; i < 6; i++) {
            String displayDate = displayFormat.format(calendar.getTime());
            String backendDate = backendFormat.format(calendar.getTime());

            // THE FIX IS HERE: The variable is now of type View
            View dateCard = createChoiceChip(displayDate);

            dateCard.setOnClickListener(v -> {
                // Deselect the previously selected card, if any
                if (selectedDateView != null) {
                    selectedDateView.setSelected(false);
                }
                // Select the new card
                v.setSelected(true);
                // Store the reference to the new selected card
                selectedDateView = v;

                selectedDate = backendDate;
                checkAndProceed();
            });
            dateContainer.addView(dateCard);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Populate Times
        String[] times = {"4-5 PM", "5-6 PM", "6-7 PM", "7-8 PM", "8-9 PM"};
        for (String time : times) {

            // THE FIX IS HERE: The variable is now of type View
            View timeCard = createChoiceChip(time);

            timeCard.setOnClickListener(v -> {
                // Deselect the previously selected card, if any
                if (selectedTimeView != null) {
                    selectedTimeView.setSelected(false);
                }
                // Select the new card
                v.setSelected(true);
                // Store the reference to the new selected card
                selectedTimeView = v;

                selectedTime = time;
                checkAndProceed();
            });
            timeContainer.addView(timeCard);
        }

        return view;
    }

    /**
     * Checks if both a date and time have been selected, and if so,
     * informs the host activity to proceed to the next step.
     */
    private void checkAndProceed() {
        if (selectedDate != null && selectedTime != null) {
            mListener.onDateTimeSelected(selectedDate, selectedTime);
        }
    }

    /**
     * Helper method to create a modern, card-style choice view dynamically.
     * @param text The text to display on the card.
     * @return A View representing the choice card.
     */
    private View createChoiceChip(String text) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.item_choice_card, null, false);

        TextView textView = cardView.findViewById(R.id.choice_text);
        textView.setText(text);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) (8 * getResources().getDisplayMetrics().density);
        params.setMargins(0, 0, margin, 0);
        cardView.setLayoutParams(params);

        // This background drawable handles the selected state automatically
        cardView.setBackgroundResource(R.drawable.choice_card_background);

        return cardView;
    }
}