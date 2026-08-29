package com.blank_learn.newversion.O1;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class OnboardingAdapter extends FragmentStateAdapter {

    private final List<OnboardingItem> onboardingItems;

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity, List<OnboardingItem> onboardingItems) {
        super(fragmentActivity);
        this.onboardingItems = onboardingItems;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OnboardingSlideFragment.newInstance(onboardingItems.get(position));
    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }
}