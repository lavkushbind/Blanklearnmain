package com.example.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.airbnb.lottie.LottieDrawable;
import com.example.dark.R;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.example.dark.databinding.About2Binding;
import com.example.dark.databinding.ActivityAboutBinding;
import com.example.dark.databinding.ActivityMain3Binding;
import com.example.loginandsignup.login;
import com.example.loginandsignup.signup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity3 extends AppCompatActivity {

    private LottieAnimationView animationView;
    private About2Binding binding;
    private boolean isVideoStarted = false;
    FirebaseAuth auth;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = About2Binding.inflate(getLayoutInflater());

        animationView = findViewById(R.id.profilimg);


        binding.profilimg.playAnimation();
        binding.profilimg.pauseAnimation();


//        animationView.playAnimation();
//        animationView.pauseAnimation();
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser();
        if(currentUser!= null)
        {
            Intent intent= new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }


        binding.button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( MainActivity3.this, login.class));
            }
        });
        binding.button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity3.this, signup.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        animationView.playAnimation();
        binding.profilimg.setRepeatCount(LottieDrawable.INFINITE); // Loop indefinitely

        binding.profilimg.playAnimation(); // Ensure it's playing when the activity resumes

    }

    @Override
    protected void onPause() {
//        animationView.pauseAnimation(); // Pause when the activity is paused
        super.onPause();
    }
}