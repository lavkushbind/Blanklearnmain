package com.blank_learn.newversion.Ai;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;

import androidx.fragment.app.Fragment;

public class AiSetupActivity extends AppCompatActivity {


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ai_setup);

            // Load Setup Fragment initially
            if (savedInstanceState == null) {
                loadFragment(new AiFragment());
            }
        }

        public void loadFragment(Fragment fragment) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.aiFragmentContainer, fragment)
                    .addToBackStack(null) // Allow back navigation
                    .commit();
        }
    }