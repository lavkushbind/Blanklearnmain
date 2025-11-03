package com.blank_learn.Onboarding;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.blank_learn.dark.R;

public class WelcomeSlideFragment extends Fragment {

    private static final String ARG_TEXT = "argText";
    private static final String ARG_LOTTIE_RES_ID = "argLottieResId";

    public static WelcomeSlideFragment newInstance(String text, int lottieResId) {
        WelcomeSlideFragment fragment = new WelcomeSlideFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putInt(ARG_LOTTIE_RES_ID, lottieResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_slide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView welcomeText = view.findViewById(R.id.welcome_text);
        LottieAnimationView lottieIllustration = view.findViewById(R.id.lottie_illustration);

        if (getArguments() != null) {
            welcomeText.setText(getArguments().getString(ARG_TEXT));
            lottieIllustration.setAnimation(getArguments().getInt(ARG_LOTTIE_RES_ID));
        }
    }
}