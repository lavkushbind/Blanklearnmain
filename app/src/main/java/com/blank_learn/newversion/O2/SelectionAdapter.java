package com.blank_learn.newversion.O2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blank_learn.dark.R;
import java.util.List;

public class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.SelectionViewHolder> {

    private final List<String> items;
    private int selectedPosition = -1;
    private final OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        void onItemSelected(String item);
    }

    public SelectionAdapter(List<String> items, OnItemSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selectable_card, parent, false);
        return new SelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectionViewHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);

        // FIX: Use the cached 'textView' from the holder instead of findViewById
        holder.textView.setSelected(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Refresh UI for both old and new items
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onItemSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SelectionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public SelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure R.id.item_text matches the ID in item_selectable_card.xml
            textView = itemView.findViewById(R.id.item_text);
        }
    }
}