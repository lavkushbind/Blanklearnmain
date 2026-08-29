package com.blank_learn.newversion.O2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ListItemPlanBinding;
import java.util.ArrayList;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private final Context context;
    private final ArrayList<Plan> planList;
    private final OnPlanSelectedListener listener;

    public interface OnPlanSelectedListener {
        void onPlanSelected(Plan plan);
    }

    public PlanAdapter(Context context, ArrayList<Plan> planList, OnPlanSelectedListener listener) {
        this.context = context;
        this.planList = planList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        Plan plan = planList.get(position);

        holder.binding.tvPlanName.setText(plan.getPlanName());

        // Price and Duration
        holder.binding.tvPrice.setText("₹" + plan.getPrice());
        holder.binding.tvDuration.setText("/ " + plan.getDuration());

        // Clear previous views to avoid duplication on scroll
// Clear previous views
        holder.binding.featuresContainer.removeAllViews();

        if (plan.getFeatures() != null) {
            for (String featureText : plan.getFeatures()) {
                TextView featureTextView = new TextView(context);
                featureTextView.setText("• " + featureText); // Simple bullet point add kiya
                featureTextView.setTextSize(15f);

                // Text Color White (Dark Theme ke liye)
                featureTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));

                // No Icon Logic Here

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8); // Spacing thodi kam ki hai bina icon ke
                featureTextView.setLayoutParams(params);

                holder.binding.featuresContainer.addView(featureTextView);
            }
        }

        // Button Click
        holder.binding.btnChoosePlan.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlanSelected(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        ListItemPlanBinding binding;
        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ListItemPlanBinding.bind(itemView);
        }
    }
}