package com.blank_learn; // Apna package name check kar lein

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.blank_learn.dark.R; // R file ka path check kar lein
import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {

    private List<DateModel> dateList;
    private Context context;
    private OnDateClickListener listener;

    // Interface for click events
    public interface OnDateClickListener {
        void onDateClick(int position);
    }

    public DateAdapter(Context context, List<DateModel> dateList, OnDateClickListener listener) {
        this.context = context;
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_slot, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateModel dateModel = dateList.get(position);

        holder.tvDayName.setText(dateModel.getDayName());
        holder.tvDateNumber.setText(dateModel.getDateNumber());

        // Update background and text color based on selection
        if (dateModel.isSelected()) {
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_date_selected));
            holder.tvDayName.setTextColor(Color.BLACK);
            holder.tvDateNumber.setTextColor(Color.BLACK);
        } else {
            holder.container.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_date_unselected));
            holder.tvDayName.setTextColor(Color.WHITE);
            holder.tvDateNumber.setTextColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView tvDayName, tvDateNumber;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.date_slot_container);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDateNumber = itemView.findViewById(R.id.tv_date_number);
        }
    }
}