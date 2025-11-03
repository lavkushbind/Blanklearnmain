package com.blank_learn;

import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.loginandsignup.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity_da extends AppCompatActivity {

    // Add a TAG for easy filtering in Logcat
    private static final String TAG = "UserListActivity";

    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private UserAdapter_run userAdapter;
    private List<Users> userList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list_da);

        Log.d(TAG, "onCreate: Activity started.");

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new UserAdapter_run(this, userList);
        usersRecyclerView.setAdapter(userAdapter);

        // IMPORTANT: Make sure this path is correct. Is it "Users" or "users"?
        // Case matters!
        String databasePath = "Users";
        databaseReference = FirebaseDatabase.getInstance().getReference(databasePath);
        Log.d(TAG, "onCreate: Firebase reference created for path: " + databasePath);

        fetchUsers();
    }

    private void fetchUsers() {
        Log.d(TAG, "fetchUsers: Starting to fetch data...");
        progressBar.setVisibility(View.VISIBLE);
        usersRecyclerView.setVisibility(View.GONE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Method entered. Data received from Firebase.");

                // CRITICAL CHECK: Does the data snapshot even exist?
                if (!dataSnapshot.exists()) {
                    Log.w(TAG, "onDataChange: Snapshot does not exist! The path might be wrong or the node is empty.");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UserListActivity_da.this, "No users found at this database path.", Toast.LENGTH_LONG).show();
                    return;
                }

                userList.clear();
                Log.d(TAG, "onDataChange: Found " + dataSnapshot.getChildrenCount() + " children.");

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Users user = snapshot.getValue(Users.class);
                        if (user != null) {
                            userList.add(user);
                            Log.d(TAG, "Successfully parsed user with UID: " + user.getUid());
                        } else {
                            Log.w(TAG, "Parsed user is null for key: " + snapshot.getKey());
                        }
                    } catch (Exception e) {
                        // This will catch POJO mapping errors!
                        Log.e(TAG, "Error parsing user data for key: " + snapshot.getKey(), e);
                    }
                }

                Log.d(TAG, "onDataChange: Finished processing. Final list size: " + userList.size());
                userAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                usersRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This is EXTREMELY important. This tells you about permissions and other errors.
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "onCancelled: Firebase database error: " + databaseError.getMessage());
                Log.e(TAG, "onCancelled: Details: " + databaseError.getDetails());
                Toast.makeText(UserListActivity_da.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}