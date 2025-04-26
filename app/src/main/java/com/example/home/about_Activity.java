package com.example.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.palette.graphics.Palette;

import android.view.View;
import android.view.ViewTreeObserver;

import com.example.dark.databinding.ActivityAboutBinding;
import com.example.loginandsignup.login;
import com.example.loginandsignup.signup;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class about_Activity extends AppCompatActivity {

    private ActivityAboutBinding binding;
    private boolean isVideoStarted = false;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    private ExoPlayer exoPlayer1, exoPlayer2, exoPlayer3, exoPlayer4, exoPlayer5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        currentUser=auth.getCurrentUser();
        if(currentUser!= null)
        {
            Intent intent= new Intent(about_Activity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Initialize ExoPlayers
        exoPlayer1 = new ExoPlayer.Builder(this).build();
        exoPlayer2 = new ExoPlayer.Builder(this).build();
        exoPlayer3 = new ExoPlayer.Builder(this).build();

        binding.playerView1.setPlayer(exoPlayer1);
        binding.playerView1.setPlayer(exoPlayer2);
        binding.playerView3.setPlayer(exoPlayer3);

        // Disable controls and set transparency if needed
        disableControlsAndSetAlpha();

        // Retrieve video URLs from Firebase Realtime Database
//        fetchAndPlayVideo("ui/video/1st", exoPlayer1);
//        fetchAndPlayVideo("ui/video/2nd", exoPlayer2);
//        fetchAndPlayVideo("ui/video/3rd", exoPlayer3);
//        fetchAndPlayVideo("ui/video/1st", exoPlayer4);
//        fetchAndPlayVideo("ui/video/1st", exoPlayer5);

        animateColorTransition(binding.view10, Color.WHITE, Color.parseColor("#05B50C"), 2000);
        binding.button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(about_Activity.this, login.class));
            }
        });
        binding.button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(about_Activity.this, signup.class));
            }
        });


        binding.scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = binding.scrollView.getScrollY();
                int newColor = calculateColorChange(scrollY);
                binding.view10.setBackgroundColor(newColor);
                binding.view11.setBackgroundColor(newColor);
//                binding.view17.setBackgroundColor(newColor);
                binding.view18.setBackgroundColor(newColor);
            }
        });




    }

    private void animateColorTransition(View view, int startColor, int endColor, int duration) {
        ObjectAnimator colorAnim = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), startColor, endColor);
        colorAnim.setDuration(duration);
        colorAnim.start();
    }
    private int calculateColorChange(int scrollY) {
        int scrollRange = Math.min(scrollY, 1000);
        float factor = (float) scrollRange / 1000;

        return (int) new ArgbEvaluator().evaluate(factor, Color.WHITE, Color.BLUE);
    }

    private void disableControlsAndSetAlpha() {
        binding.playerView1.setUseController(false);
        binding.playerView1.setUseController(false);
        binding.playerView3.setUseController(false);
    }

    private void fetchAndPlayVideo(String path, ExoPlayer exoPlayer) {
        FirebaseDatabase.getInstance().getReference(path)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String videoUrl = snapshot.getValue(String.class);
                        if (videoUrl != null) {
                            playVideo(videoUrl, exoPlayer);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void playVideo(String url, ExoPlayer exoPlayer) {
        MediaItem mediaItem = MediaItem.fromUri(url);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
    }
    private boolean isViewVisible(View view) {
        int[] scrollBounds = new int[2];
        binding.scrollView.getLocationOnScreen(scrollBounds);
        int scrollViewTop = scrollBounds[0];
        int scrollViewBottom = scrollBounds[1] + binding.scrollView.getHeight();

        int[] viewBounds = new int[2];
        view.getLocationOnScreen(viewBounds);
        int viewTop = viewBounds[1];
        int viewBottom = viewTop + view.getHeight();

        return (viewTop >= scrollViewTop && viewBottom <= scrollViewBottom);
    }
    private void extractColorsFromVideo() {
        exoPlayer1.addListener(new Player.Listener() {
            @Override
            public void onRenderedFirstFrame() {
                Bitmap bitmap = binding.playerView2.getVideoSurfaceView().getDrawingCache();

                if (bitmap != null) {
                    Palette.from(bitmap).generate(palette -> {
                        int dominantColor = palette.getDominantColor(0x000000);
                        applyBackgroundColor(dominantColor);
                    });
                }
            }
        });
    }

    private void applyBackgroundColor(int color) {
        binding.constraint.setBackgroundColor(color);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayers();
    }

    private void releasePlayers() {
        if (exoPlayer1 != null) {
            exoPlayer1.release();
            exoPlayer1 = null;
        }
        if (exoPlayer2 != null) {
            exoPlayer2.release();
            exoPlayer2 = null;
        }
        if (exoPlayer3 != null) {
            exoPlayer3.release();
            exoPlayer3 = null;
        }

    }
}
