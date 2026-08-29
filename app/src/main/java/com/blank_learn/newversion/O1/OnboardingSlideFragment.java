package com.blank_learn.newversion.O1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blank_learn.dark.R;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnboardingSlideFragment extends Fragment {

    private static final String ARG_IMAGE_RES = "arg_image_res";
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_DESCRIPTION = "arg_description";

    public static OnboardingSlideFragment newInstance(OnboardingItem item) {
        OnboardingSlideFragment fragment = new OnboardingSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES, item.getImage());
        args.putString(ARG_TITLE, item.getTitle());
        args.putString(ARG_DESCRIPTION, item.getDescription());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_slide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.image_onboarding);
        TextView titleView = view.findViewById(R.id.text_title);
        TextView descriptionView = view.findViewById(R.id.text_description);

        if (getArguments() != null) {
            imageView.setImageResource(getArguments().getInt(ARG_IMAGE_RES));
            titleView.setText(getArguments().getString(ARG_TITLE));
            descriptionView.setText(getArguments().getString(ARG_DESCRIPTION));
        }
    }
}