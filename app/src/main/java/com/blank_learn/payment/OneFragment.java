package com.blank_learn.payment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blank_learn.dark.databinding.FragmentOneBinding;
import com.blank_learn.loginandsignup.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.blank_learn.dark.R;

public class OneFragment extends Fragment {
    private FragmentOneBinding binding;
    private one_adapter search_course_adapter;
    private List<Users> allPosts;
    private List<Users> filteredPosts;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOneBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        initializeRecyclerView();
        initializeFirebase();

        return binding.getRoot();
    }

    private void initializeRecyclerView() {
        allPosts = new ArrayList<>();
        filteredPosts = new ArrayList<>();
        search_course_adapter = new one_adapter((ArrayList<Users>) filteredPosts, getContext());
        binding.recyclerOne.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerOne.setAdapter(search_course_adapter);
    }

    private void initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                allPosts.clear();
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    Users post = postSnapshot.getValue(Users.class);
//                    if (post.getStoryid() != null && post.getBio()!= null && post.getCharge()!= 0  ) {
//
//                        if (post != null) {
//                            post.setUserID(postSnapshot.getKey());
//                            allPosts.add(post);
//
//                        }
//                    }}
//                filterAndDisplayPosts("");
//            }
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allPosts.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    try {
                        // We put the potentially crashing code inside the 'try' block
                        Users post = postSnapshot.getValue(Users.class);

                        // The code below will only run if getValue() was successful
                        if (post != null) { // This check is now more useful
                            if (post.getStoryid() != null && post.getBio() != null && post.getCharge() != 0) {
                                post.setUserID(postSnapshot.getKey());
                                allPosts.add(post);
                            }
                        }
                    } catch (com.google.firebase.database.DatabaseException e) {
                        // --- CATCH THE ERROR HERE ---
                        // If we are here, it means this specific user had bad data.
                        // We can log the error to help us find the bad user in the database.
                        Log.e("FirebaseReader", "Could not parse user: " + postSnapshot.getKey(), e);
                        // By catching the error, we prevent the app from crashing and the loop continues.
                    }
                }
                // After the loop finishes (skipping any bad users), display the good ones.
                filterAndDisplayPosts("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

        binding.searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAndDisplayPosts(newText);
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.message_options_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_edit_text);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search posts");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAndDisplayPosts(newText);
                return true;
            }
        });
    }

    private void filterAndDisplayPosts(String query) {
        filteredPosts.clear();
        if (TextUtils.isEmpty(query)) {
            filteredPosts.addAll(allPosts);
        } else {
            for (Users post : allPosts) {
                if (postContainsQuery(post, query)) {
                    filteredPosts.add(post);
                }
            }
        }
        search_course_adapter.notifyDataSetChanged();
    }
    private boolean postContainsQuery(Users post, String query) {
        return (post.getBio() != null && post.getBio().toLowerCase().contains(query.toLowerCase())) ||
                (post.getName() != null && post.getName().toLowerCase().contains(query.toLowerCase())) ||
                (post.getEmail() != null && post.getEmail().toLowerCase().contains(query.toLowerCase())) ||
                (post.getPhone() != null && post.getPhone().toLowerCase().contains(query.toLowerCase()));
    }



}