package com.blank_learn.newversion.language;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import java.util.List;

// NOTE: This class now assumes that 'LevelModel' is a standalone class
// defined in the com.newversion.language package (or properly imported).

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelViewHolder> {

    // Fix 1: The List now correctly refers to the standalone LevelModel class
    private final List<LevelModel> levels;
    private int selectedPosition = -1;
    private final OnLevelSelectedListener listener;

    // Interface definition is fine here
    public interface OnLevelSelectedListener {
        void onLevelSelected(String levelName);
    }

    public LevelAdapter(List<LevelModel> levels, OnLevelSelectedListener listener) {
        this.levels = levels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level_card, parent, false);
        return new LevelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelViewHolder holder, int position) {
        LevelModel level = levels.get(position);

        // Fix 2: Use getters if LevelModel fields are private (Best practice)
        holder.tvTitle.setText(level.getTitle());
        holder.tvSubtitle.setText(level.getDescription());
        holder.ivIcon.setImageResource(level.getIconResId());

        // Highlight Logic
        holder.container.setSelected(selectedPosition == position);

        holder.container.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // UI Update
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            // Notify Fragment
            if (listener != null) {
                listener.onLevelSelected(level.getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return levels.size();
    }

    static class LevelViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ivIcon;

        public LevelViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.level_container);
            tvTitle = itemView.findViewById(R.id.tvLevelTitle);
            tvSubtitle = itemView.findViewById(R.id.tvLevelSubtitle);
            ivIcon = itemView.findViewById(R.id.ivLevelIcon);
        }
    }

    // REMOVED: public static class LevelModel {...} definition
}