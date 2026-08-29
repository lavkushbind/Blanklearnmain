package com.blank_learn.newversion.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;
import com.blank_learn.newversion.doubtsolve.SolutionActivity;
import com.yalantis.ucrop.UCrop;

// Analytics Imports
import com.google.firebase.analytics.FirebaseAnalytics;
import com.facebook.appevents.AppEventsLogger;

import java.io.File;

public class DoubtSolveFragment extends Fragment {

    private Uri photoUri;

    // Analytics
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_com, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        fbLogger = AppEventsLogger.newLogger(requireContext());

        // Log Screen View
        logEvent("doubt_screen_viewed", null);

        // 2. Click Listeners with Analytics
        view.findViewById(R.id.btnCamera).setOnClickListener(v -> {
            logEvent("camera_clicked", null);
            checkCameraPermission();
        });

        view.findViewById(R.id.btnGallery).setOnClickListener(v -> {
            logEvent("gallery_clicked", null);
            galleryLauncher.launch("image/*");
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = new File(requireContext().getExternalFilesDir(null), "temp_cam.jpg");
            photoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".provider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    startCrop(photoUri);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    startCrop(uri);
                }
            });

    private void startCrop(Uri sourceUri) {
        String destName = "cropped_" + System.currentTimeMillis() + ".jpg";
        Uri destUri = Uri.fromFile(new File(requireContext().getExternalFilesDir(null), destName));

        UCrop.Options options = new UCrop.Options();
        options.setToolbarTitle("Crop Question");
        options.setFreeStyleCropEnabled(true);

        UCrop.of(sourceUri, destUri)
                .withOptions(options)
                .start(requireContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);

            // Analytics: Log Successful Crop
            logEvent("crop_success", null);

            Intent intent = new Intent(requireContext(), SolutionActivity.class);
            intent.putExtra("imageUri", resultUri.toString());
            startActivity(intent);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            // Analytics: Log Failure
            logEvent("crop_failed", null);
            Toast.makeText(requireContext(), "Crop Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper Method for Analytics
    private void logEvent(String eventName, Bundle params) {
        if (params == null) params = new Bundle();
        // Google Analytics
        mFirebaseAnalytics.logEvent(eventName, params);
        // Facebook Pixel/SDK
        fbLogger.logEvent(eventName, params);
    }
}