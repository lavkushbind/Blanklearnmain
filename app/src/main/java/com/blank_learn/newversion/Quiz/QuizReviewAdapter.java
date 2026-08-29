package com.blank_learn.newversion.Quiz;

import android.graphics.Color;
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

public class QuizReviewAdapter extends RecyclerView.Adapter<QuizReviewAdapter.ViewHolder> {

    private List<QuizReviewModel> list;

    public QuizReviewAdapter(List<QuizReviewModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizReviewModel model = list.get(position);

        // 1. Set Question & Number
        holder.tvQNumber.setText(String.format("Question %02d", (position + 1)));
        holder.tvQuestion.setText(model.getQuestion());

        // 2. Set Explanation
        holder.tvExplanation.setText(model.getExplanation());

        // 3. Logic for Correct/Wrong
        if (model.isCorrect()) {
            // === CORRECT ANSWER ===
            holder.tvUserAnswer.setText(model.getUserAnswer());
            holder.tvUserAnswer.setTextColor(Color.parseColor("#00E676")); // Neon Green

            // Set Check Icon (Green)
            holder.ivStatus.setImageResource(android.R.drawable.checkbox_on_background);
            holder.ivStatus.setColorFilter(Color.parseColor("#00E676"));

            // Hide "Correct Answer" section since user was right
            holder.layoutCorrectAns.setVisibility(View.GONE);

        } else {
            // === WRONG ANSWER ===
            holder.tvUserAnswer.setText(model.getUserAnswer());
            holder.tvUserAnswer.setTextColor(Color.parseColor("#FF4444")); // Red

            // Set Cross/Delete Icon (Red)
            holder.ivStatus.setImageResource(android.R.drawable.ic_delete);
            holder.ivStatus.setColorFilter(Color.parseColor("#FF4444"));

            // Show "Correct Answer" section
            holder.layoutCorrectAns.setVisibility(View.VISIBLE);
            holder.tvCorrectAnswer.setText(model.getCorrectAnswer());
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ViewHolder Class linking to XML IDs
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvQNumber, tvQuestion, tvUserAnswer, tvCorrectAnswer, tvExplanation;
        ImageView ivStatus;
        LinearLayout layoutCorrectAns;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Linking IDs from item_quiz_review.xml
            tvQNumber = itemView.findViewById(R.id.tvQNumber);
            tvQuestion = itemView.findViewById(R.id.tvQuestionText);
            tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);

            ivStatus = itemView.findViewById(R.id.ivStatus);
            layoutCorrectAns = itemView.findViewById(R.id.layoutCorrectAns);
        }
    }
}