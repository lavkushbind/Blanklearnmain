package com.example.demo;

import com.blank_learn.dark.R;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TeacherAllocationFragment extends Fragment {

    private RecyclerView recyclerView;
    private AllocationAdapter allocationAdapter;
    private List<AllocationData> allocationList = new ArrayList<>();
    private String currentUserId;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_teacher_allocation, container, false);  // Replace with your layout ID

        recyclerView = view.findViewById(R.id.teacherRecyclerViewAllocations);  // Replace with your RecyclerView ID
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            currentUserId = user.getUid();
            loadAllocations();
        } else
        {
            Log.e("TeacherAllocationFragment", "No user is currently signed in.");
        }

        return view;
    }

    private void loadAllocations() {
        DatabaseReference allocationsRef = FirebaseDatabase.getInstance().getReference("allocated_classes");
        allocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allocationList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AllocationData allocation = snapshot.getValue(AllocationData.class);
                    if (allocation != null) {
                        // Teachers see only their own allocations.
                        if (allocation.getTeacherID() != null && allocation.getTeacherID().equals(currentUserId)) {
                            allocationList.add(allocation);
                        }
                    }
                }

                if (mContext != null) {
                    allocationAdapter = new AllocationAdapter(allocationList, mContext, true);  // isTeacher = true
                    recyclerView.setAdapter(allocationAdapter);
                    allocationAdapter.notifyDataSetChanged(); // Refresh the adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TeacherAllocationFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
}