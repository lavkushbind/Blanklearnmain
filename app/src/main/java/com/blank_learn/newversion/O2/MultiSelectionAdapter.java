package com.blank_learn.newversion.O2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blank_learn.dark.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiSelectionAdapter extends RecyclerView.Adapter<MultiSelectionAdapter.MultiSelectionViewHolder> {

    private final List<String> items;
    private final Set<Integer> selectedPositions = new HashSet<>();
    private final OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(List<String> selectedItems);
    }

    public MultiSelectionAdapter(List<String> items, OnSelectionChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MultiSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selectable_card, parent, false);
        return new MultiSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiSelectionViewHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);

        // FIX: Target the TextView specifically for selection state
        holder.textView.setSelected(selectedPositions.contains(position));

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();

            if (selectedPositions.contains(currentPosition)) {
                selectedPositions.remove(currentPosition);
            } else {
                selectedPositions.add(currentPosition);
            }

            // Update UI
            notifyItemChanged(currentPosition);

            if (listener != null) {
                listener.onSelectionChanged(getSelectedItems());
            }
        });
    }

    private List<String> getSelectedItems() {
        List<String> selectedItemsList = new ArrayList<>();
        for (int position : selectedPositions) {
            selectedItemsList.add(items.get(position));
        }
        return selectedItemsList;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MultiSelectionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MultiSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_text);
        }
    }
}