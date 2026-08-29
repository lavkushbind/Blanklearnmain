package com.blank_learn.newversion.O2;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.blank_learn.dark.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ProgressBar progressBar;
    private ImageView btnClose;
    private View touchOverlay; // Click handler
    private ExoPlayer exoPlayer;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full Screen (No Status Bar)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_video_player);

        videoUrl = getIntent().getStringExtra("VIDEO_URL");

        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        btnClose = findViewById(R.id.btnClose);
        touchOverlay = findViewById(R.id.touchOverlay);

        // Close Activity
        btnClose.setOnClickListener(v -> finish());

        // Tap to Play/Pause
        touchOverlay.setOnClickListener(v -> {
            if (exoPlayer != null) {
                if (exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                } else {
                    exoPlayer.play();
                }
            }
        });

        if (videoUrl != null) {
            initializePlayer();
        } else {
            Toast.makeText(this, "Error: Link not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_ENDED) {
                    // Video khatam hone par dobara shuru se
                    exoPlayer.seekTo(0);
                    exoPlayer.pause();
                }
            }

            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(VideoPlayerActivity.this, "Cannot play video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exoPlayer != null && exoPlayer.isPlaying()) {
            exoPlayer.pause();
        }
    }
}