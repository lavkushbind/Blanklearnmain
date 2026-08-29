package com.blank_learn.newversion.Commune;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityFragment extends Fragment {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private List<Post> postList;
    private DatabaseReference databaseRef;
    private ExtendedFloatingActionButton fabCreate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("community_posts");

        // Initialize UI
        recyclerView = view.findViewById(R.id.recyclerViewCommunity);
        fabCreate = view.findViewById(R.id.fabCreatePost);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        adapter = new CommunityAdapter(getContext(), postList);
        recyclerView.setAdapter(adapter);

        // Load Data
        fetchPosts();

        // Handle Create Post Click
        fabCreate.setOnClickListener(v -> {
            // Here you would open a Dialog or new Activity to type the post
            // For testing, let's push a dummy post
            createDummyPost();
        });

        return view;
    }

    private void fetchPosts() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                List<Post> pinnedPosts = new ArrayList<>();
                List<Post> normalPosts = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Post post = data.getValue(Post.class);
                    if (post != null) {
                        if (post.isPinned()) {
                            pinnedPosts.add(post);
                        } else {
                            normalPosts.add(post);
                        }
                    }
                }

                // Logic: Show pinned posts at top, then normal posts (reversed to show newest first)
                Collections.reverse(normalPosts);
                postList.addAll(pinnedPosts);
                postList.addAll(normalPosts);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDummyPost() {
        String id = databaseRef.push().getKey();
        // Example: Student post
        Post newPost = new Post("Rahul Kumar", "Does anyone have the notes for Chapter 4 Math?", false, false);
        if (id != null) databaseRef.child(id).setValue(newPost);
    }
}