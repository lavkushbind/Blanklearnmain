package com.blank_learn.newversion.O1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

// ANALYTICS LIBRARIES
import com.blank_learn.dark.R;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
// END ANALYTICS LIBRARIES

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class Onboarding_Slides extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button createPlanButton;
    private TabLayout tabLayout;
    private OnboardingAdapter adapter;

     private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_slides);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fbLogger = AppEventsLogger.newLogger(this);
        logOnboardingBeginEvent();

        viewPager = findViewById(R.id.view_pager_onboarding);
        createPlanButton = findViewById(R.id.button_create_plan);
        tabLayout = findViewById(R.id.tab_layout_indicator);

        setupAdapter();

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == adapter.getItemCount() - 1) {
                    showButtonWithAnimation();
                } else {
                    createPlanButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        createPlanButton.setOnClickListener(v -> {
            logOnboardingCompleteEvent();

            Intent mainIntent = new Intent(this, PhoneAuthActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });
    }

    private void logOnboardingBeginEvent() {
        mFirebaseAnalytics.logEvent("onboarding_begin", null);
         Bundle fbParams = new Bundle();
        fbParams.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, "onboarding");
        fbLogger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, fbParams);
        Log.d("ANALYTICS", "Logged Onboarding Begin Event");
    }

    private void logOnboardingCompleteEvent() {
        mFirebaseAnalytics.logEvent("onboarding_complete", null);
        fbLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL);
        Log.d("ANALYTICS", "Logged Onboarding Complete Event");
    }

    private void setupAdapter() {
        List<OnboardingItem> onboardingItems = new ArrayList<>();
        onboardingItems.add(new OnboardingItem(R.drawable.a, "The Evening Struggle?", "Tired after a long day? We get it. Making sure your child studies shouldn't be another chore."));
        onboardingItems.add(new OnboardingItem(R.drawable.b, "Imagine Your Peace of Mind.", "What if this entire responsibility was handled for you? Relax, while we ensure your child excels."));
        onboardingItems.add(new OnboardingItem(R.drawable.c, "Focus, Not Crowds.", "Our classes have a maximum of 3 students. This means 100% personal attention and guaranteed results."));
        onboardingItems.add(new OnboardingItem(R.drawable.d, "Mentors You Can Trust.", "Our experienced and verified teachers don't just teach, they become personal mentors for your child."));
        onboardingItems.add(new OnboardingItem(R.drawable.e, "Ready for a Smarter Way to Learn?", "Let's build the perfect learning plan for your child and reclaim your evenings."));

        adapter = new OnboardingAdapter(this, onboardingItems);
        viewPager.setAdapter(adapter);
    }

    private void showButtonWithAnimation() {
        createPlanButton.setVisibility(View.VISIBLE);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        createPlanButton.startAnimation(pulse);
    }
}