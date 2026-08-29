package com.blank_learn.newversion.Commune;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public CommunityAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.txtUserName.setText(post.getUserName());
        holder.txtContent.setText(post.getContent());
        holder.txtLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.txtCommentCount.setText(String.valueOf(post.getCommentCount()));

        // Logic for Pinned Posts
        if (post.isPinned()) {
            holder.layoutPinned.setVisibility(View.VISIBLE);
            // Use a slightly different background color for pinned posts to make them stand out
            holder.itemView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.layoutPinned.setVisibility(View.GONE);
        }

        // Logic for Teacher Badge
        if (post.isTeacher()) {
            holder.chipTeacher.setVisibility(View.VISIBLE);
        } else {
            holder.chipTeacher.setVisibility(View.GONE);
        }

        // Load Avatar (using placeholder if URL is empty for this example)
        if(post.getUserAvatarUrl() != null && !post.getUserAvatarUrl().isEmpty()){
            Glide.with(context).load(post.getUserAvatarUrl()).circleCrop().into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtContent, txtTime, txtLikeCount, txtCommentCount;
        ImageView imgAvatar;
        LinearLayout layoutPinned;
        Chip chipTeacher;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtCommentCount = itemView.findViewById(R.id.txtCommentCount);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            layoutPinned = itemView.findViewById(R.id.layoutPinned);
            chipTeacher = itemView.findViewById(R.id.chipTeacher);
        }
    }
}