package com.example.home;

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

import com.example.dark.R;
import com.example.demo.AllocationListFragment;
import com.example.payment.OneFragment;
import com.example.profile.ProfileFragment;
import com.example.profile.YourSearchActivity;
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


        if (isConnected()) {
            setupUI();
        } else {
            showNoInternetDialog();
        }
    }
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                    Log.d("Permission", "Notification permission granted");
                    // You might want to retrieve and update the token here if previously denied
                    getAndUpdateFcmToken();
                } else {
                    // Inform user about consequences
                    Log.w("Permission", "Notification permission denied");
                    Toast.makeText(this, "Notifications will be disabled.", Toast.LENGTH_SHORT).show();
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level 33 and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permission already granted
                getAndUpdateFcmToken(); // Good place to ensure token is up-to-date
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: Display an educational UI explaining why the permission is needed
                // Then, request the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS); // Request again after rationale
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            getAndUpdateFcmToken(); // On older versions, permission is implicitly granted - just get token
        }
    }

    // Helper function to get and update token
    private void getAndUpdateFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Log.d("FCM_TOKEN", "Current token: " + token);
                sendRegistrationToServer(token); // Call the method from your service or implement similar logic here
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

    // In your Activity's onCreate:

    private void setupUI() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container, new HomFragment());
        fragmentTransaction.commit();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                int itemId = item.getItemId(); // Get the item ID

                if (itemId == R.id.home) {
                    fragmentTransaction.replace(R.id.container, new HomFragment());
                } else if (itemId == R.id.notificationid) {
                    fragmentTransaction.replace(R.id.container, new OneFragment());
                } else if (itemId == R.id.search) {
                    fragmentTransaction.replace(R.id.container, new YourSearchActivity());
                } else if (itemId == R.id.profile) {
                    fragmentTransaction.replace(R.id.container, new ProfileFragment());
                } else if (itemId == R.id.classs) {
                    fragmentTransaction.replace(R.id.container, new AllocationListFragment());
                }
                fragmentTransaction.commit();
                return true;
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isConnected()) {
                            setupUI();
                        } else {
                            showNoInternetDialog();
                        }
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}


