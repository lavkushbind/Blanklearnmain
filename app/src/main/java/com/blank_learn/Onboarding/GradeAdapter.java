package com.blank_learn.Onboarding;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<String> gradeList;
    private Context context;
    private int selectedPosition = -1;
    private OnGradeSelectedListener listener;

     public interface OnGradeSelectedListener {
        void onGradeSelected(String grade);
    }

    public GradeAdapter(Context context, List<String> gradeList, OnGradeSelectedListener listener) {
        this.context = context;
        this.gradeList = gradeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
        String grade = gradeList.get(position);
        holder.gradeName.setText(grade);

         if (selectedPosition == position) {
 //            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.purple_500));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.ic_launcher_background)); // एक हल्का कलर बनाएं
        } else {
 //            holder.cardView.setStrokeColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        }

         holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            listener.onGradeSelected(grade);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return gradeList.size();
    }

     public static class GradeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView gradeName;

        public GradeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_grade);
            gradeName = itemView.findViewById(R.id.tv_grade_name);
        }
    }
}