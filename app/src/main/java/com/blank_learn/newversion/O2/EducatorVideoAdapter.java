package com.blank_learn.newversion.O2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ListItemEducatorVideoBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class EducatorVideoAdapter extends RecyclerView.Adapter<EducatorVideoAdapter.VideoViewHolder> {

    private ArrayList<EducatorVideoModel> videoList;
    private Context context;

    public EducatorVideoAdapter(ArrayList<EducatorVideoModel> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_educator_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        EducatorVideoModel video = videoList.get(position);

        // 1. Load Image (Thumbnail)
        // Make sure ivEducatorThumbnail is VISIBLE in XML
        Glide.with(context)
                .load(video.getVideoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bg_home_gradient)
                .into(holder.binding.ivEducatorThumbnail);

        // 2. Hide Player View in List (List me player ki zarurat nahi)
        holder.binding.playerView.setVisibility(View.GONE);
        holder.binding.progressBar.setVisibility(View.GONE);
        holder.binding.ivEducatorThumbnail.setVisibility(View.VISIBLE);
        holder.binding.ivPlayButton.setVisibility(View.VISIBLE);

        // 3. Click Listener -> Open New Activity
        holder.binding.videoContainer.setOnClickListener(v -> {
            String url = video.getVideoUrl();
            if (url != null && !url.isEmpty()) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("VIDEO_URL", url);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ListItemEducatorVideoBinding binding;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListItemEducatorVideoBinding.bind(itemView);
        }
    }
}