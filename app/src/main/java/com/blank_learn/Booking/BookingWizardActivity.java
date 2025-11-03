package com.blank_learn.Booking;


import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.blank_learn.dark.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// ... other necessary imports from your original code

import java.util.HashMap;

public class BookingWizardActivity extends AppCompatActivity implements
        Step1_ClassFragment.OnStepOneListener,
        Step2_ClassTypeFragment.OnStepTwoListener,
        Step3_DateTimeFragment.OnStepThreeListener,
        Step4_ConfirmationFragment.OnStepFourListener {

    private ProgressBar progressBar;

    // Data model to hold all selections
    private static class BookingData {
        String selectedClass;
        String selectedClassType;
        String selectedDate;
        String selectedTime;
    }
    private BookingData bookingData = new BookingData();

    // Firebase variables
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_wizard);

        progressBar = findViewById(R.id.progress_bar_wizard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (savedInstanceState == null) {
            // Load the first step (fragment)
            loadFragment(new Step1_ClassFragment(), false);
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null); // Allows user to press back to go to the previous step
        }
        transaction.commit();
    }

    // --- Callbacks from Fragments ---

    @Override
    public void onClassSelected(String selectedClass) {
        bookingData.selectedClass = selectedClass;
        loadFragment(new Step2_ClassTypeFragment(), true);
    }

    @Override
    public void onClassTypeSelected(String classType) {
        bookingData.selectedClassType = classType;
        loadFragment(new Step3_DateTimeFragment(), true);
    }

    @Override
    public void onDateTimeSelected(String date, String time) {
        bookingData.selectedDate = date;
        bookingData.selectedTime = time;

        // Pass all collected data to the final confirmation fragment
        Step4_ConfirmationFragment confirmationFragment = new Step4_ConfirmationFragment();
        Bundle args = new Bundle();
        args.putString("CLASS", bookingData.selectedClass);
        args.putString("TYPE", bookingData.selectedClassType);
        args.putString("DATE", bookingData.selectedDate);
        args.putString("TIME", bookingData.selectedTime);
        confirmationFragment.setArguments(args);

        loadFragment(confirmationFragment, true);
    }

    @Override
    public void onConfirmation() {
        // The final "Book Now" button was clicked in the last fragment
        // Start the actual Firebase booking logic
        progressBar.setVisibility(View.VISIBLE);
        findAndAllocateTeacher();
    }

    // --- Firebase Booking Logic (Copied from previous solution, no changes needed) ---
    private void findAndAllocateTeacher() {
        // ... PASTE THE ENTIRE findAndAllocateTeacher() METHOD HERE ...
        // ... PASTE THE isTeacherSuitable() METHOD HERE ...
        // ... PASTE THE assignTeacher() METHOD HERE ...
        // ... PASTE THE saveAllocationToDatabase() METHOD HERE ...
        // ... PASTE THE sendSystemNotifications() METHOD HERE ...
        // ... PASTE THE handleError() METHOD HERE ...

        // Example of the final success call inside saveAllocationToDatabase()
        // Instead of showSuccessScreen(), you will load a final success fragment.
        // E.g., loadFragment(new SuccessFragment(teacherName), false);
    }

}