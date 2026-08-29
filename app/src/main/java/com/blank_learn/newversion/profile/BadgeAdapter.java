package com.blank_learn.newversion.profile;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private Context context;
    private List<Badge> badgeList;

    public BadgeAdapter(Context context, List<Badge> badgeList) {
        this.context = context;
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badgeList.get(position);

        holder.txtBadgeName.setText(badge.getName());
        holder.imgBadge.setImageResource(badge.getImageResId());

        if (badge.isUnlocked()) {
            // UNLOCKED: Show full color
            holder.imgBadge.clearColorFilter();
            holder.imgBadge.setAlpha(1.0f);
            holder.imgLock.setVisibility(View.GONE);
        } else {
            // LOCKED: Make Grayscale (Black & White)
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); // 0 means no color
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

            holder.imgBadge.setColorFilter(filter);
            holder.imgBadge.setAlpha(0.5f); // Fade it out slightly
            holder.imgLock.setVisibility(View.VISIBLE); // Show lock icon
        }
    }

    @Override
    public int getItemCount() { return badgeList.size(); }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBadge, imgLock;
        TextView txtBadgeName;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBadge = itemView.findViewById(R.id.imgBadge);
            imgLock = itemView.findViewById(R.id.imgLock);
            txtBadgeName = itemView.findViewById(R.id.txtBadgeName);
        }
    }
}