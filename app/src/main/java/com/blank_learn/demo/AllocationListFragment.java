


package com.blank_learn.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.blank_learn.dark.R;
import com.google.firebase.auth.FirebaseUser;



public class AllocationListFragment extends Fragment {

    private RecyclerView recyclerView;
    private AllocationAdapter allocationAdapter;
    private List<AllocationData> allocationList;
    private boolean isTeacher;
    private String currentUserId;
    private Context mContext;

    public AllocationListFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allocation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.allocation_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        allocationList = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String role = dataSnapshot.child("role").getValue(String.class);
                        isTeacher = "teacher".equalsIgnoreCase(role);
                        loadAllocations();
                    } else {
                        Log.e("AllocationFragment", "User data not found for user ID: " + currentUserId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("AllocationFragment", "Database error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.e("AllocationFragment", "No user is currently signed in.");
        }
    }


    private void loadAllocations() {
        DatabaseReference allocationsRef = FirebaseDatabase.getInstance().
                getReference("allocated_classes");
        allocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allocationList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AllocationData allocation = snapshot.getValue(AllocationData.class);
                    if (allocation != null) {
                        if (isTeacher) {

                            if (allocation.getTeacherID() != null && allocation.getTeacherID().equals(currentUserId)) {
                                allocationList.add(allocation);
                            }
                        } else {
                            if (allocation.getStudentID() != null && allocation.getStudentID().equals(currentUserId)) {

                                allocationList.add(allocation);
                            }
                        }
                    }
                }

                if (mContext != null) {
                    allocationAdapter = new AllocationAdapter(allocationList, mContext, isTeacher);
                    recyclerView.setAdapter(allocationAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AllocationFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

}

