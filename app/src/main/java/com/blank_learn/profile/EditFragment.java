package com.blank_learn.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.blank_learn.dark.databinding.FragmentEditBinding;
import com.blank_learn.loginandsignup.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditFragment extends Fragment {

     private static final String TAG = "EDIT_PROFILE_DEBUG";

     private FragmentEditBinding binding;

     private FirebaseAuth auth;
    private DatabaseReference userRef;
    private ValueEventListener userValueListener;

    // --- FRAGMENT LIFECYCLE ---

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: Fragment view is created.");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // **CRITICAL CHECK 1: User Logged In?**
        if (currentUser == null) {
            Log.e(TAG, "FATAL ERROR: Current user is null. Cannot proceed.");
            Toast.makeText(getContext(), "Authentication Error! Please login again.", Toast.LENGTH_LONG).show();
            // Optionally, navigate to login screen
            return;
        }

        // Setup the database reference.
        // **CRITICAL CHECK 2: Is the path 'Users' correct?** Check your Firebase Console.
        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        Log.d(TAG, "Database reference set to: " + userRef.toString());

        // Setup UI
        setupClickListeners();
        // Load data from Firebase
        loadUserData();
    }

    // --- SETUP METHODS ---

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        Log.d(TAG, "showLoading: " + isLoading);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.mainContentScrollView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up all button clicks.");
        // Back Button
        binding.imageView4.setOnClickListener(v -> navigateToProfile());

        // Update Buttons
        binding.Updatename.setOnClickListener(v -> updateField("name", binding.nametext.getText().toString(), "Name"));
        binding.Updatepro.setOnClickListener(v -> updateField("profesion", binding.profesiontext.getText().toString(), "Profession"));
        binding.Update.setOnClickListener(v -> updateField("bio", binding.biotext.getText().toString(), "Bio"));
        binding.Updateemail.setOnClickListener(v -> updateField("email", binding.emailtex.getText().toString(), "Email"));
        binding.updateL.setOnClickListener(v -> updateField("linkedin", binding.linkdinurl.getText().toString(), "LinkedIn"));

    // --- FIREBASE DATA LOADING ---
    }
    private void loadUserData() {
        if (userRef == null) {
            Log.e(TAG, "loadUserData: userRef is null. Cannot load data.");
            return;
        }

        showLoading(true);

        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // **CRITICAL CHECK 3: Does data exist at the path?**
                if (!snapshot.exists()) {
                    showLoading(false);
                    Log.e(TAG, "onDataChange: FAILED. No data exists at path: " + userRef.toString());
                    Toast.makeText(getContext(), "User data not found in database.", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.d(TAG, "onDataChange: SUCCESS. Data received from Firebase.");

                // **CRITICAL CHECK 4: Can Firebase parse the data into our 'Users' class?**
                Users user = snapshot.getValue(Users.class);
                if (user == null) {
                    showLoading(false);
                    Log.e(TAG, "onDataChange: FAILED. 'snapshot.getValue(Users.class)' returned NULL.");
                    Log.e(TAG, "This means Firebase data structure does NOT match the Users.java model class.");
                    Log.e(TAG, "RAW FIREBASE DATA: " + snapshot.getValue()); // This prints the exact data for comparison
                    Toast.makeText(getContext(), "Error: Mismatch between app and database.", Toast.LENGTH_LONG).show();
                    return;
                }

                // If all checks pass, populate the UI
                Log.d(TAG, "onDataChange: Successfully parsed data into User object. Populating UI.");
                populateUi(user);
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                // **CRITICAL CHECK 5: Did we have permission to read the data?**
                Log.e(TAG, "onCancelled: Firebase data loading was cancelled or failed.", error.toException());
                Toast.makeText(getContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        userRef.addListenerForSingleValueEvent(userValueListener);
    }

    // --- FIREBASE DATA UPDATING ---

    private void updateField(String fieldKey, Object value, String displayName) {
        if (userRef == null) return;
        // Simple validation
        if (value.toString().trim().isEmpty() && !fieldKey.equals("bio")) {
            Toast.makeText(getContext(), displayName + " cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Updating field '" + fieldKey + "' with value: " + value.toString());
        userRef.child(fieldKey).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "SUCCESS: Updated " + displayName);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), displayName + " updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "FAILED: Could not update " + displayName, e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --- UI HELPER METHODS ---

    private void populateUi(Users user) {
        if (getContext() == null || binding == null) return; // Safety check
        binding.nametext.setText(user.getName());
        binding.profesiontext.setText(user.getProfesion());
        binding.biotext.setText(user.getBio());
        binding.emailtex.setText(user.getEmail());
         binding.linkdinurl.setText(user.getLinkedin());
    }

    private void navigateToProfile() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack(); // Go back to the previous fragment
        }
    }

    // --- MEMORY MANAGEMENT ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Fragment view is being destroyed.");
        // IMPORTANT: Remove listener to prevent memory leaks
        if (userRef != null && userValueListener != null) {
            userRef.removeEventListener(userValueListener);
            Log.d(TAG, "Firebase listener removed.");
        }
        binding = null; // Clean up view binding
    }
}