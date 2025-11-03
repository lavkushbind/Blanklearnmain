package com.blank_learn.Onboarding;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.blank_learn.dark.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Optional: Add simple fade-in animations for logo and tagline
        ImageView logo = findViewById(R.id.logo_image);
        TextView tagline = findViewById(R.id.tagline_text);

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1500);
        logo.startAnimation(fadeIn);
        tagline.startAnimation(fadeIn);

        // Handler to start the WelcomeActivity after the delay
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish(); // Close this activity so the user can't go back to it
        }, SPLASH_DELAY);
    }
}