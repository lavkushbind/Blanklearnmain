package com.blank_learn.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ActivityStoryBinding;
import com.blank_learn.profile.ProActivity;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;


public class StoryActivity extends AppCompatActivity {
    ActivityStoryBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    private boolean isPlayerInitialized = false;

    FirebaseDatabase database;
    private DatabaseReference databaseReference;

    Intent intent;
    String postId;
    String userId;
    private SimpleExoPlayer exoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();
        postId = intent.getStringExtra("Link");
        userId = intent.getStringExtra("userid");
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("stories");

        exoPlayer = new SimpleExoPlayer.Builder(this).build();
        fetchVideoUrlFromFirebase();
        binding.exoplayerimage.setPlayer(exoPlayer);
        binding.exoplayerimage.setUseController(false);
        binding.exoplayerimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                } else {
                    exoPlayer.play();
                }
            }
        });


        ProgressBar progressBar = findViewById(R.id.progressBar3);
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        binding.profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userId != null) {
                    Intent intent = new Intent(StoryActivity.this, ProActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }
            }
        });

        binding.imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                shareVideo();
            }
        });
    }


    private void fetchVideoUrlFromFirebase() {
        databaseReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Story_model storyModel = dataSnapshot.getValue(Story_model.class);

                    if (storyModel != null && storyModel.getVideo() != null) {
                        MediaItem mediaItem = MediaItem.fromUri(storyModel.getVideo());
                        exoPlayer.setMediaItem(mediaItem);
                        exoPlayer.prepare();
                        exoPlayer.setPlayWhenReady(true);
                    } else {
//                        Toast.makeText(context, "Video not available", Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    Toast.makeText(context, "Post not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(context, "Error fetching video: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void shareVideo() {
//        MediaItem currentMediaItem = exoPlayer.getCurrentMediaItem();
//        if (currentMediaItem != null && currentMediaItem.playbackProperties != null) {
//            Uri videoUri = currentMediaItem.playbackProperties.uri;
//            String shareText = getString(R.string.share_video_text, videoUri.toString());
//
//            Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("video/*");
//            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
//
//            // Add URI using FileProvider if needed
//            if (isVideoFileSupported(videoUri)) {
//                shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
//            }
//
//            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_video)));
//        } else {
//            Toast.makeText(this, "No video to share", Toast.LENGTH_SHORT).show();
//        }
//    }

    private boolean isVideoFileSupported(Uri videoUri) {
        String type = getContentResolver().getType(videoUri);
        return type != null && type.startsWith("video/");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }
}