package com.blank_learn.Onboarding;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.blank_learn.PhoneAuthActivity;
import com.blank_learn.dark.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button nextButton;
    private WelcomePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        nextButton = findViewById(R.id.button_next);
        TextView skipText = findViewById(R.id.text_skip);

        pagerAdapter = new WelcomePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Link the TabLayout with the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // We don't need to set text or icons here since we're using custom drawables
        }).attach();

        // Handle button text and visibility
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == pagerAdapter.getItemCount() - 1) {
                    nextButton.setText("Get Started");
                } else {
                    nextButton.setText("Next");
                }
            }
        });

        // Button click listeners
        nextButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < pagerAdapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                navigateToHome();
            }
        });

        skipText.setOnClickListener(v -> navigateToHome());
    }

    private void navigateToHome() {
        // Intent to your main app screen (e.g., MainActivity, HomeActivity)
         Intent intent = new Intent(WelcomeActivity.this, ProfileSetupActivity.class);
         startActivity(intent);
        finish();
    }
}