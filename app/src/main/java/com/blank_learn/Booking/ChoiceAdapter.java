package com.blank_learn.Booking;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.blank_learn.dark.R;
import java.util.List;

public class ChoiceAdapter extends RecyclerView.Adapter<ChoiceAdapter.ChoiceViewHolder> {

    private final List<String> items;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public ChoiceAdapter(List<String> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choice_card, parent, false);
        return new ChoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceViewHolder holder, int position) {
        String item = items.get(position);
        holder.textView.setText(item);

        // Highlight the selected item
        holder.itemView.setSelected(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != holder.getAdapterPosition()) {
                notifyItemChanged(selectedPosition); // Un-highlight the old selection
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(selectedPosition); // Highlight the new selection
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChoiceViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ChoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.choice_text);
        }
    }
}