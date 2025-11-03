package com.blank_learn.Onboarding;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.BoardViewHolder> {

    private List<Board> boardList;
    private Context context;
    private int selectedPosition = -1;
    private OnBoardSelectedListener listener;

    public interface OnBoardSelectedListener {
        void onBoardSelected(Board board);
    }

    public BoardAdapter(Context context, List<Board> boardList, OnBoardSelectedListener listener) {
        this.context = context;
        this.boardList = boardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        Board board = boardList.get(position);
        holder.boardName.setText(board.getName());
        holder.boardLogo.setImageResource(board.getLogoResId());

        if (selectedPosition == position) {
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, R.color.purple_500));
        } else {
            holder.cardView.setStrokeColor(ContextCompat.getColor(context, android.R.color.transparent));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            listener.onBoardSelected(board);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return boardList.size();
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView boardLogo;
        TextView boardName;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_board);
            boardLogo = itemView.findViewById(R.id.iv_board_logo);
            boardName = itemView.findViewById(R.id.tv_board_name);
        }
    }
}