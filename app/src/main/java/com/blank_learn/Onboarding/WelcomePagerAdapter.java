package com.blank_learn.Onboarding;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.blank_learn.dark.R;

public class WelcomePagerAdapter extends FragmentStateAdapter {

    public WelcomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return WelcomeSlideFragment.newInstance(
                        "For your busy life, we've got their learning covered.",
                        R.raw.anim1
                );
            case 1:
                return WelcomeSlideFragment.newInstance(
                        "Learn from top-vetted tutors in 1-on-1 or small group settings.",
                        R.raw.anim2
                );
            case 2:
                return WelcomeSlideFragment.newInstance(
                        "Book a FREE trial session and witness the difference.",
                        R.raw.anim3
                );
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // We have 3 screens
    }
}