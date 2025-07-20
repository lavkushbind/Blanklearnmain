package com.blank_learn.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.FragmentEditBinding;
import com.blank_learn.loginandsignup.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.text.TextUtils; // Import for TextUtils
import android.widget.Toast; // Import Toast

import com.google.firebase.database.DatabaseReference; // Import DatabaseReference


public class EditFragment extends Fragment {

    FragmentEditBinding binding;
    FirebaseAuth auth;
    // FirebaseStorage storage; // You initialize storage but don't seem to use it for updates here
    FirebaseDatabase database;
    DatabaseReference userRef; // Reference to the user node

    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        // storage = FirebaseStorage.getInstance(); // Only needed if uploading files
        database = FirebaseDatabase.getInstance();
        if (auth.getUid() != null) {
            userRef = database.getReference().child("Users").child(auth.getUid());
        } else {
            // Handle case where user is not logged in?
            // Maybe navigate away or show an error.
            Toast.makeText(getContext(), "Error: User not logged in.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditBinding.inflate(inflater, container, false);

        // Back navigation
        binding.imageView4.setOnClickListener(v -> {
            // Consider using NavController or requireActivity().getSupportFragmentManager()
            if (getFragmentManager() != null) {
                Fragment fragment = new ProfileFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, fragment); // Ensure R.id.container is correct
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        });

        // Load initial data
        loadUserData();

        // --- Update Listeners ---

        binding.Updatepro.setOnClickListener(view -> {
            String profession = binding.profesiontext.getText().toString().trim();
            if (!TextUtils.isEmpty(profession)) {
                updateField("profesion", profession, "Profession");
            } else {
                Toast.makeText(getContext(), "Profession cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        binding.Updatename.setOnClickListener(view -> {
            String name = binding.nametext.getText().toString().trim();
            if (!TextUtils.isEmpty(name)) {
                updateField("name", name, "Name");
            } else {
                Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // **** IMPORTANT: Check if 'facebookurl' should update 'charge' as a long ****
        // This seems unusual. The commented out code suggests it should be 'fb' as a String.
        // Assuming you want to update 'fb' as a String based on the commented code:
        binding.updatef.setOnClickListener(view -> {
            String fbUrl = binding.facebookurl.getText().toString().trim();
            // Optional: Add URL validation if needed
            updateField("charge", fbUrl, "charge"); // Update 'fb' field
        });

        /* // If you REALLY intended to update 'charge' with a number from 'facebookurl' EditText:
        binding.updatef.setOnClickListener(view -> {
            String chargeStr = binding.facebookurl.getText().toString().trim();
            if (!TextUtils.isEmpty(chargeStr)) {
                try {
                    long chargeValue = Long.parseLong(chargeStr);
                    updateField("charge", chargeValue, "Charge value"); // Update 'charge' field
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid number format for charge", Toast.LENGTH_SHORT).show();
                    binding.facebookurl.setError("Invalid number"); // Indicate error on EditText
                }
            } else {
                 // Decide if empty is allowed or means zero, or update only if not empty
                 updateField("charge", null, "Charge value"); // Or updateField("charge", 0L, "Charge value");
                 // Toast.makeText(getContext(), "Charge value cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        */


        binding.Updateemail.setOnClickListener(view -> {
            String email = binding.emailtex.getText().toString().trim();
            // Optional: Add basic email format validation
            if (!TextUtils.isEmpty(email)) {
                updateField("email", email, "Email");
            } else {
                Toast.makeText(getContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        binding.Update.setOnClickListener(view -> { // Assuming this updates Bio
            String bio = binding.biotext.getText().toString().trim();
            // Allow empty bio? Decide based on requirements
            updateField("bio", bio, "Bio");
        });

        binding.updateL.setOnClickListener(view -> {
            String linkedinUrl = binding.linkdinurl.getText().toString().trim();
            // Optional: Add URL validation if needed
            updateField("linkdin", linkedinUrl, "LinkedIn URL");
        });

        binding.updatei.setOnClickListener(view -> {
            String instagramUrl = binding.instagramurl.getText().toString().trim();
            // Optional: Add URL validation if needed
            updateField("instagram", instagramUrl, "Instagram URL");
        });

        binding.updatet.setOnClickListener(view -> {
            String twitterUrl = binding.tweeterurl.getText().toString().trim();
            // Optional: Add URL validation if needed
            updateField("tweeter", twitterUrl, "Twitter URL"); // Assuming field name is 'tweeter'
        });

        return binding.getRoot();
    }

    // Helper method to load user data
    private void loadUserData() {
        if (userRef == null) return; // Don't proceed if userRef is null

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getContext() != null) { // Check context too
                    Users user = snapshot.getValue(Users.class);
                    if (user != null) {
                        binding.nametext.setText(user.getName() != null ? user.getName() : "");
                        binding.profesiontext.setText(user.getProfesion() != null ? user.getProfesion() : "");
                        binding.biotext.setText(user.getBio() != null ? user.getBio() : "");
                        binding.emailtex.setText(user.getEmail() != null ? user.getEmail() : "");
                        // Load other fields if they exist in your Users model and XML
                        binding.facebookurl.setText(user.getFb() != null ? user.getFb() : ""); // Assuming 'fb' field
//                        binding.linkdinurl.setText(user.getLinkdin() != null ? user.getLinkdin() : "");
                        binding.instagramurl.setText(user.getInstagram() != null ? user.getInstagram() : "");
//                        binding.tweeterurl.setText(user.getTweeter() != null ? user.getTweeter() : "");
                        // If you have a 'charge' field of type Long/long in Users model:
                        // binding.someOtherEditTextForCharge.setText(user.getCharge() != null ? String.valueOf(user.getCharge()) : "");
                    }
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load user data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Helper method to update a specific field in Firebase
    private void updateField(String fieldName, Object value, String fieldDisplayName) {
        if (userRef == null) {
            Toast.makeText(getContext(), "Error: Cannot update. User reference is null.", Toast.LENGTH_LONG).show();
            return; // Don't proceed if userRef is null
        }

        // Optional: Show progress indicator here
        // binding.progressBar.setVisibility(View.VISIBLE);
        // Disable update buttons temporarily?

        userRef.child(fieldName).setValue(value)
                .addOnSuccessListener(unused -> {
                    // Optional: Hide progress indicator
                    // binding.progressBar.setVisibility(View.GONE);
                    if (getContext() != null) { // Check if fragment is still attached
                        Toast.makeText(getContext(), fieldDisplayName + " updated successfully!", Toast.LENGTH_SHORT).show();
                        // The EditText already shows the new value, no need to setText here.
                    }
                })
                .addOnFailureListener(e -> {
                    // Optional: Hide progress indicator
                    // binding.progressBar.setVisibility(View.GONE);
                    if (getContext() != null) { // Check if fragment is still attached
                        Toast.makeText(getContext(), "Failed to update " + fieldDisplayName + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // Optional: Reload data from Firebase to revert the EditText change if needed
                        // loadUserData();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Important: Nullify the binding object to prevent memory leaks
        binding = null;
    }
}


