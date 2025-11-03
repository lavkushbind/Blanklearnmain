package com.blank_learn.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.demo.ReviewModel;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<ReviewModel> reviewList;

    public ReviewAdapter(Context context, List<ReviewModel> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Yahan review_item_layout.xml ko use karein (jo aapne pehle banaya tha)
        View view = LayoutInflater.from(context).inflate(R.layout.review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);

        // Data ko views par set karein
        holder.textViewName.setText(review.getName());
        holder.textViewStatusTag.setText(review.getStatusTag());
        holder.textViewReview.setText(review.getReviewText());
        holder.textViewAttribution.setText(review.getAttribution());
        holder.ratingBar.setRating(review.getRating());

        // Glide se image load karein
        if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(review.getImageUrl())
                    .placeholder(R.drawable.profileuser) // Optional: show a placeholder while loading
                    .into(holder.profileImage);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // ViewHolder class jo review_item_layout.xml ke views ko hold karegi
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView textViewName, textViewStatusTag, textViewReview, textViewAttribution;
        ImageView imageViewVerified;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewStatusTag = itemView.findViewById(R.id.text_view_status_tag);
            textViewReview = itemView.findViewById(R.id.text_view_review);
            textViewAttribution = itemView.findViewById(R.id.text_view_attribution);
            imageViewVerified = itemView.findViewById(R.id.image_view_verified);
            ratingBar = itemView.findViewById(R.id.rating_bar);
        }
    }
}