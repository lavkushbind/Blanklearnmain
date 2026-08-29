package com.blank_learn.newversion.language;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import java.util.List;

// NOTE: This class now assumes that 'LanguageModel' is a standalone class
// defined in the com.newversion.language package (or properly imported).

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private final List<LanguageModel> languages;
    private int selectedPosition = -1;
    private final OnLanguageSelectedListener listener;

    public interface OnLanguageSelectedListener {
        void onLanguageSelected(String langCode);
    }

    public LanguageAdapter(List<LanguageModel> languages, OnLanguageSelectedListener listener) {
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_option, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageModel lang = languages.get(position);

        // Using getters from the external LanguageModel class
        holder.tvLanguageName.setText(lang.getName());
        holder.ivFlag.setImageResource(lang.getFlagResId());

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
                listener.onLanguageSelected(lang.getCode());
            }
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        ImageView ivFlag;
        TextView tvLanguageName;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.language_container);
            ivFlag = itemView.findViewById(R.id.ivFlag);
            tvLanguageName = itemView.findViewById(R.id.tvLanguageName);
        }
    }

    // REMOVED: Nested static class LanguageModel definition here
}