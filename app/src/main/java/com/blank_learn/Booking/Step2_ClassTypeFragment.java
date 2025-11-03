package com.blank_learn.Booking;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
// ... other imports from Step1 ...
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.blank_learn.dark.R;
// ...

public class Step2_ClassTypeFragment extends Fragment {

    private OnStepTwoListener mListener;

    public interface OnStepTwoListener {
        void onClassTypeSelected(String classType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnStepTwoListener) {
            mListener = (OnStepTwoListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnStepTwoListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // You can create a new layout or reuse the old one
        View view = inflater.inflate(R.layout.fragment_step2__class_type, container, false);
        LinearLayout classTypeContainer = view.findViewById(R.id.class_container);

        String[] types = {"Personal 1-on-1", "Small Group (3-5 Students)"};
        for (String type : types) {
            TextView typeView = createChoiceChip(type);
            typeView.setOnClickListener(v -> mListener.onClassTypeSelected(type));
            classTypeContainer.addView(typeView);
        }
        return view;
    }

    private TextView createChoiceChip(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.selector_choice_chip);
        textView.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.selector_choice_text));
        int paddingHorizontal = (int) (16 * getResources().getDisplayMetrics().density);
        int paddingVertical = (int) (10 * getResources().getDisplayMetrics().density);
        textView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        return textView;
    }
}