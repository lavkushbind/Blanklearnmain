package com.blank_learn.newversion.doubtsolve;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.blank_learn.dark.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class doubtsolveActivity extends AppCompatActivity {

    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com);

        findViewById(R.id.btnCamera).setOnClickListener(v -> checkCameraPermission());
        findViewById(R.id.btnGallery).setOnClickListener(v -> galleryLauncher.launch("image/*"));
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            File photoFile = new File(getExternalFilesDir(null), "temp_cam.jpg");
            // FIX: Using FileProvider
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) startCrop(photoUri);
            });

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) startCrop(uri);
            });

    private void startCrop(Uri sourceUri) {
        String destName = "cropped_" + System.currentTimeMillis() + ".jpg";
        Uri destUri = Uri.fromFile(new File(getExternalFilesDir(null), destName));

        UCrop.Options options = new UCrop.Options();
        options.setToolbarTitle("Crop Question");
        options.setFreeStyleCropEnabled(true);

        UCrop.of(sourceUri, destUri).withOptions(options).start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            // Move to Solution Screen
            Intent intent = new Intent(doubtsolveActivity.this, SolutionActivity.class);
            intent.putExtra("imageUri", resultUri.toString());
            startActivity(intent);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Crop Failed", Toast.LENGTH_SHORT).show();
        }
    }
}