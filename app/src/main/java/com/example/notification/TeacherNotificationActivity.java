package com.example.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.blank_learn.dark.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class TeacherNotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<BookingModel> bookingList;
    private DatabaseReference databaseReference;
    private String teacherID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_notification);
        teacherID= FirebaseAuth.getInstance().getUid();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList);
        recyclerView.setAdapter(bookingAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("allocated_classes");

        // Fetch bookings for the logged-in teacher
        fetchDemoBookings();
    }

    private void fetchDemoBookings() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("allocated_classes");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Debugging step
                    Log.d("Firebase", "Snapshot Key: " + dataSnapshot.getKey());
                    Log.d("Firebase", "Full Data: " + dataSnapshot.getValue());

                    // Fetch className manually
                    String className = dataSnapshot.child("className").getValue(String.class);
                    String timeSlot = dataSnapshot.child("timeSlot").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String teacherID = dataSnapshot.child("teacherID").getValue(String.class);

                    Log.d("Firebase", "Fetched Class Name: " + className); // Debugging log

                    if (teacherID != null && teacherID.equals(teacherID)) {
                        bookingList.add(new BookingModel(teacherID, className, timeSlot, date));
                    }
                }
                bookingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error fetching data", error.toException());
                Toast.makeText(TeacherNotificationActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
