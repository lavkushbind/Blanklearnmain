package com.blank_learn.newversion.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;

import java.util.ArrayList;
import java.util.List;

public class ProFragment extends Fragment {

    private RecyclerView recyclerBadges;
    private BadgeAdapter badgeAdapter;
    private List<Badge> badgeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pro, container, false);

        // 1. SETUP STATS (Populating the reusable cards)
        setupStat(view.findViewById(R.id.cardStatPoints), "🏆", "Total Points", "2,540");
        setupStat(view.findViewById(R.id.cardStatClasses), "🎓", "Classes", "24");
        setupStat(view.findViewById(R.id.cardStatStreak), "🔥", "Streak", "12 Days");

        // 2. SETUP BADGES RECYCLERVIEW
        recyclerBadges = view.findViewById(R.id.recyclerBadges);
        // Grid Layout with 3 columns looks modern
        recyclerBadges.setLayoutManager(new GridLayoutManager(getContext(), 3));

        loadDummyBadges();

        badgeAdapter = new BadgeAdapter(getContext(), badgeList);
        recyclerBadges.setAdapter(badgeAdapter);

        return view;
    }

    // Helper method to populate stat cards nicely
    private void setupStat(View card, String emoji, String label, String value) {
        TextView txtEmoji = card.findViewById(R.id.statEmoji);
        TextView txtLabel = card.findViewById(R.id.statLabel);
        TextView txtValue = card.findViewById(R.id.statValue);

        txtEmoji.setText(emoji);
        txtLabel.setText(label);
        txtValue.setText(value);
    }

    private void loadDummyBadges() {
        badgeList = new ArrayList<>();
        // Using built-in android drawables for demo, replace with your assets
        badgeList.add(new Badge("Beginner", android.R.drawable.star_big_on, true));
        badgeList.add(new Badge("Math Whiz", android.R.drawable.star_big_on, true));
        badgeList.add(new Badge("Scientist", android.R.drawable.star_big_on, true));
        badgeList.add(new Badge("Scholar", android.R.drawable.star_big_on, false)); // Locked
        badgeList.add(new Badge("Genius", android.R.drawable.star_big_on, false));  // Locked
        badgeList.add(new Badge("Legend", android.R.drawable.star_big_on, false));  // Locked
    }
}