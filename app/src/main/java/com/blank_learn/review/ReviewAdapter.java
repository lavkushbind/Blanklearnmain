package com.blank_learn.review; // Replace with your package name

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils; // For formatting timestamp
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast; // For error messages

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.blank_learn.dark.R;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    ArrayList<ReviewItem> reviewList;
   Context context;


    public ReviewAdapter(ArrayList<ReviewItem> reviewList, Context context) {
        this.context = context;
        this.reviewList = reviewList;
    }
    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review_card, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem currentItem = reviewList.get(position);

        holder.tvReviewerName.setText(currentItem.getReviewerName());

        if (currentItem.getTimestamp() > 0) {
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                    currentItem.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            holder.tvReviewTimestamp.setText(timeAgo);
        } else {
            holder.tvReviewTimestamp.setText("");
        }

        holder.ratingBarReview.setRating(currentItem.getRating());
        holder.tvReviewText.setText(currentItem.getReviewText());

        // Load profile image using Glide
        if (currentItem.getReviewerProfileImageUrl() != null && !currentItem.getReviewerProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(currentItem.getReviewerProfileImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.profileuser)
                            .error(R.drawable.profileuser)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.ivReviewerProfile);
        } else {
            holder.ivReviewerProfile.setImageResource(R.drawable.profileuser);
        }


        if (currentItem.hasVideo()) {
            holder.videoContainer.setVisibility(View.VISIBLE);
            holder.videoViewReview.setVisibility(View.VISIBLE);
            holder.pbVideoLoading.setVisibility(View.VISIBLE);
            holder.ivPlayOverlay.setVisibility(View.GONE);

            Uri videoUri = Uri.parse(currentItem.getVideoUrl());
            holder.videoViewReview.setVideoURI(videoUri);

            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(holder.videoViewReview);
            holder.videoViewReview.setMediaController(mediaController);

            holder.videoViewReview.setOnPreparedListener(mp -> {
                holder.pbVideoLoading.setVisibility(View.GONE);
                holder.ivPlayOverlay.setVisibility(View.VISIBLE);
                // mp.setVolume(0f, 0f); // Mute by default if needed
            });

            holder.videoViewReview.setOnErrorListener((mp, what, extra) -> {
                holder.pbVideoLoading.setVisibility(View.GONE);
                // Optionally hide the container or show an error icon
                // holder.videoContainer.setVisibility(View.GONE);
                Toast.makeText(context, "Cannot play video", Toast.LENGTH_SHORT).show();
                return true;
            });

            holder.videoViewReview.setOnCompletionListener(mp -> {
                // Show play button again when video finishes
                holder.ivPlayOverlay.setVisibility(View.VISIBLE);
                // If you want it to loop, you can call mp.start() here.
            });


            holder.ivPlayOverlay.setOnClickListener(v -> {
                if (!holder.videoViewReview.isPlaying()) {
                    holder.videoViewReview.start();
                }
                holder.ivPlayOverlay.setVisibility(View.GONE);
            });

        } else {
            holder.videoContainer.setVisibility(View.GONE);
            holder.videoViewReview.setVisibility(View.GONE);
            holder.pbVideoLoading.setVisibility(View.GONE);
            holder.ivPlayOverlay.setVisibility(View.GONE);
            if (holder.videoViewReview.isPlaying()) {
                holder.videoViewReview.stopPlayback();
            }
            holder.videoViewReview.setMediaController(null);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ReviewViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.videoViewReview.isPlaying()) {
            holder.videoViewReview.stopPlayback();
        }
        holder.videoViewReview.setMediaController(null);
        holder.videoViewReview.setOnPreparedListener(null);
        holder.videoViewReview.setOnErrorListener(null);
        holder.videoViewReview.setOnCompletionListener(null);
        holder.videoContainer.setVisibility(View.GONE);
        holder.ivPlayOverlay.setVisibility(View.VISIBLE); // Reset play overlay
    }


    @Override
    public int getItemCount() {
        return reviewList.size(); // Or list_review.size() - THIS IS LINE 154
    }

    // Add a method to update the list and notify the adapter
    public void updateReviews(List<ReviewItem> newReviews) {
        reviewList.clear();
        reviewList.addAll(newReviews);
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivReviewerProfile;
        TextView tvReviewerName, tvReviewTimestamp, tvReviewText;
        RatingBar ratingBarReview;
        FrameLayout videoContainer;
        VideoView videoViewReview;
        ProgressBar pbVideoLoading;
        ImageView ivPlayOverlay;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReviewerProfile = itemView.findViewById(R.id.iv_reviewer_profile);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            tvReviewTimestamp = itemView.findViewById(R.id.tv_review_timestamp);
            ratingBarReview = itemView.findViewById(R.id.rating_bar_review);
            tvReviewText = itemView.findViewById(R.id.tv_review_text);
            videoContainer = itemView.findViewById(R.id.video_container);
            videoViewReview = itemView.findViewById(R.id.video_view_review);
            pbVideoLoading = itemView.findViewById(R.id.pb_video_loading);
            ivPlayOverlay = itemView.findViewById(R.id.iv_play_overlay);
        }
    }
}