package com.blank_learn.Challenge;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
// ChallengeIntroActivity.java
import android.animation.Animator;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.blank_learn.dark.databinding.ActivityChallengeIntroBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChallengeIntroActivity extends AppCompatActivity {

    private ActivityChallengeIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChallengeIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fetchQuestThemeAndSetupUI();

        binding.btnStartCountdown.setOnClickListener(v -> startCountdownAnimation());
    }

    private void fetchQuestThemeAndSetupUI() {
        DatabaseReference dailyQuestInfoRef = FirebaseDatabase.getInstance().getReference("dailyQuestInfo");
        dailyQuestInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("questTitle").getValue(String.class);
                    String theme = snapshot.child("theme").getValue(String.class);

                    updateUiForTheme(title, theme);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChallengeIntroActivity.this, "Could not load quest info", Toast.LENGTH_SHORT).show();
                // Fallback to a default theme
                updateUiForTheme("Today's Challenge", "default");
            }
        });
    }

    private void updateUiForTheme(String title, String theme) {
        binding.tvChallengeTitle.setText("Get Ready for\n" + title + "!");

        switch (theme) {
            case "space":
                binding.ivBackground.setImageResource(R.drawable.bg_space);
                binding.tvRewards.setText("🏆 Unlock the 'Galaxy Explorer' Badge!");
                break;
            case "wildlife":
                binding.ivBackground.setImageResource(R.drawable.bg_jungle);
                binding.tvRewards.setText("🏆 Earn up to 200 XP!");
                break;
            default:
                // binding.ivBackground.setImageResource(R.drawable.bg_default);
                binding.tvRewards.setText("🏆 Add to your weekly leaderboard score!");
                break;
        }
    }

    private void startCountdownAnimation() {
        binding.contentLayout.setVisibility(View.GONE);
        binding.lottieCountdown.setVisibility(View.VISIBLE);
        binding.lottieCountdown.playAnimation();

        binding.lottieCountdown.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation finished, now start the quiz
                Intent intent = new Intent(ChallengeIntroActivity.this, QuizActivity.class); // <-- Next screen
                startActivity(intent);
                finish(); // Prevent user from coming back here
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }
}