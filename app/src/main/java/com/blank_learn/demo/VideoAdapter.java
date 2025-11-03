package com.blank_learn.demo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.demo.VideoInfo;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class VideoAdapter extends FirebaseRecyclerAdapter<VideoInfo, VideoAdapter.VideoViewHolder> {

    // Yeh variable batayega ki abhi konsa video chal raha hai.
    private VideoViewHolder currentlyPlayingHolder = null;

    public VideoAdapter(@NonNull FirebaseRecyclerOptions<VideoInfo> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull VideoViewHolder holder, int position, @NonNull VideoInfo model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    // Yeh method sure karega ki activity stop hone par koi video na chale.
    public void stopAnyPlayingVideo() {
        if (currentlyPlayingHolder != null) {
            currentlyPlayingHolder.stopPlayback();
            currentlyPlayingHolder = null; // Reset a
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ProgressBar progressBar;
        ImageView playIcon;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoViewItem);
            progressBar = itemView.findViewById(R.id.progressBarItem);
            playIcon = itemView.findViewById(R.id.videoPlayIcon);
        }

        void bind(VideoInfo videoInfo) {
            // Har naye item ke liye state reset karna.
            stopPlayback();

            Uri videoUri = Uri.parse(videoInfo.getVideoUrl());
            videoView.setVideoURI(videoUri);

            progressBar.setVisibility(View.VISIBLE);
            playIcon.setVisibility(View.GONE);

            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                mp.seekTo(1); // Pehla frame dikhane ke liye
                mp.setOnSeekCompleteListener(mp1 -> playIcon.setVisibility(View.VISIBLE));
                mp.setLooping(true);
            });

            // Dono click listeners ek hi function call karenge.
            playIcon.setOnClickListener(v -> handlePlayPause());
            videoView.setOnClickListener(v -> handlePlayPause());
        }

        /**
         * YEH METHOD THEEK KIYA GAYA HAI
         * This is the corrected method.
         */
        private void handlePlayPause() {
            if (videoView.isPlaying()) {
                // Agar video chal raha hai, to use roko.
                videoView.pause();
                playIcon.setVisibility(View.VISIBLE);
                // Sabse zaroori: Adapter ko batao ki ab koi video nahi chal raha.
                if (currentlyPlayingHolder == this) {
                    currentlyPlayingHolder = null;
                }
            } else {
                // Agar video ruka hua hai, to use chalao.

                // Pehle, check karo ki koi aur video to nahi chal raha.
                if (currentlyPlayingHolder != null) {
                    // Agar chal raha hai, to use band karo.
                    currentlyPlayingHolder.stopPlayback();
                }

                // Ab is video ko chalao.
                videoView.start();
                playIcon.setVisibility(View.GONE);

                // Adapter ko batao ki ab yeh wala video chal raha hai.
                currentlyPlayingHolder = this;
            }
        }

        /**
         * Yeh video ko poori tarah se rok deta hai.
         */
        void stopPlayback() {
            if(videoView.isPlaying()){
                videoView.stopPlayback();
            }
            // Hamesha play icon dikhao jab video ruka hua ho.
            playIcon.setVisibility(View.VISIBLE);
            // Progress bar ko hide karo.
            progressBar.setVisibility(View.GONE);
        }
    }
}