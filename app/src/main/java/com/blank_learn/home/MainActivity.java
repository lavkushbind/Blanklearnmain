package com.blank_learn.home;

// NEW: Add these imports for the new logic
import android.content.Context;
import android.content.SharedPreferences;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.blank_learn.dark.R;
import com.blank_learn.demo.AllocationListFragment;
import com.blank_learn.payment.OneFragment;
import com.blank_learn.profile.EditFragment;
import com.blank_learn.profile.ProfileFragment;
import com.blank_learn.profile.YourSearchActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askNotificationPermission();

        Window window = getWindow();
        window.setNavigationBarColor(getResources().getColor(android.R.color.white));

        bottomNavigationView = findViewById(R.id.bottomNavigationView); // NEW: Find the view here

        if (isConnected()) {
            // MODIFIED: We now call our two new setup methods
            handleInitialFragment();
            setupBottomNavigationListener();
        } else {
            showNoInternetDialog();
        }
    }

    /**
     * NEW: This is the most important new method. It decides which fragment to show on startup.
     * It checks the flag we set in demoActivity.
     */
    private void handleInitialFragment() {
        // Read the persistent flag
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        boolean shouldShowDemoSuccess = prefs.getBoolean("SHOW_DEMO_SUCCESS_FRAGMENT", false);

        if (shouldShowDemoSuccess) {
            // The flag is true! The user just booked a demo.

            // IMPORTANT: Reset the flag so it doesn't show again on the next app start.
            prefs.edit().putBoolean("SHOW_DEMO_SUCCESS_FRAGMENT", false).apply();

            // Load the AllocationListFragment, as it's the perfect "success" screen.
            Log.d("MainActivity", "Demo success flag is true. Loading AllocationListFragment.");
            loadFragment(new AllocationListFragment());

            // Also update the bottom navigation to show the "My Class" tab as selected.
            bottomNavigationView.setSelectedItemId(R.id.classs);

        } else {
            // This is the normal flow. The flag is false, so load the default home fragment.
            Log.d("MainActivity", "No special navigation flag. Loading HomFragment.");
            loadFragment(new HomFragment());
        }
    }


    /**
     * MODIFIED: This method was renamed from setupUI() and now ONLY handles the click listener.
     * The initial fragment loading is now done in handleInitialFragment().
     */
    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    loadFragment(new HomFragment());
                } else if (itemId == R.id.notificationid) {
                    loadFragment(new OneFragment());
                } else if (itemId == R.id.profile) {
                    loadFragment(new ProfileFragment());
                } else if (itemId == R.id.classs) {
                    loadFragment(new AllocationListFragment());
                }
                return true;
            }
        });
    }

    /**
     * NEW: A reusable helper method to load fragments into the container.
     * This avoids repeating code.
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }


    // --- All other methods below are unchanged and correct ---

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("Permission", "Notification permission granted");
                    getAndUpdateFcmToken();
                } else {
                    Log.w("Permission", "Notification permission denied");
                    Toast.makeText(this, "Notifications will be disabled.", Toast.LENGTH_SHORT).show();
                }
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                getAndUpdateFcmToken();
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            getAndUpdateFcmToken();
        }
    }

    private void getAndUpdateFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Log.d("FCM_TOKEN", "Current token: " + token);
                sendRegistrationToServer(token);
            } else {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
            }
        });
    }

    private void sendRegistrationToServer(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null && token != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> Log.d("MainActivity", "FCM Token updated successfully from Activity"))
                    .addOnFailureListener(e -> Log.e("MainActivity", "Failed to update FCM Token from Activity", e));
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    public void navigateToAllocations() {
        Log.d("MainActivity", "navigateToAllocations() called. Switching to AllocationListFragment.");
        loadFragment(new AllocationListFragment());
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.classs);
        }
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("Retry", (dialog, which) -> {
                    if (isConnected()) {
                        handleInitialFragment();
                        setupBottomNavigationListener();
                    } else {
                        showNoInternetDialog();
                    }
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}