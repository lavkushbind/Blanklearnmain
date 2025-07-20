package com.blank_learn.session; // Your package name is correct

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blank_learn.dark.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusSessionActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final long SESSION_DURATION_MS = 30000; // 30 seconds for testing

    private PreviewView previewView;
    private TextView timerText, statusText;

    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    private ArrayList<FocusStatus> focusDataPerFrame = new ArrayList<>();
    private CountDownTimer countDownTimer;

    private int awayFramesCounter = 0;
    private int tiredFramesCounter = 0;
    private static final int FRAME_THRESHOLD_FOR_DISTRACTION = 10;
    private static final float HEAD_ANGLE_THRESHOLD = 20.0f;
    private static final float EYE_OPEN_PROB_THRESHOLD = 0.4f;

    public enum FocusStatus implements Serializable {
        FOCUSED,
        LOOKING_AWAY,
        EYES_CLOSED,
        NO_FACE_DETECTED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure you have a layout file named 'activity_focus_session.xml'
        setContentView(R.layout.activity_focus_session);

        previewView = findViewById(R.id.previewView);
        timerText = findViewById(R.id.timerText);
        statusText = findViewById(R.id.statusText);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
                faceDetector = FaceDetection.getClient(options);

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
                startFocusTimer();

            } catch (ExecutionException | InterruptedException e) {
                Log.e("FocusApp", "Camera start failed.", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyzeFrame(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    processFaceResults(faces);
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e("FocusApp", "Face detection failed", e);
                    imageProxy.close();
                });
    }

    private void processFaceResults(List<Face> faces) {
        FocusStatus finalStatusForThisFrame;
        if (faces.isEmpty()) {
            finalStatusForThisFrame = FocusStatus.NO_FACE_DETECTED;
            awayFramesCounter = 0;
            tiredFramesCounter = 0;
        } else {
            Face face = faces.get(0);
            float eulerY = face.getHeadEulerAngleY();
            float eulerX = face.getHeadEulerAngleX();
            boolean isLookingAway = Math.abs(eulerY) > HEAD_ANGLE_THRESHOLD || Math.abs(eulerX) > HEAD_ANGLE_THRESHOLD;

            Float leftEyeProb = face.getLeftEyeOpenProbability();
            Float rightEyeProb = face.getRightEyeOpenProbability();
            boolean areEyesClosed = (leftEyeProb != null && leftEyeProb < EYE_OPEN_PROB_THRESHOLD) &&
                    (rightEyeProb != null && rightEyeProb < EYE_OPEN_PROB_THRESHOLD);

            if (areEyesClosed) {
                tiredFramesCounter++;
                awayFramesCounter = 0;
            } else if (isLookingAway) {
                awayFramesCounter++;
                tiredFramesCounter = 0;
            } else {
                awayFramesCounter = 0;
                tiredFramesCounter = 0;
            }

            if (tiredFramesCounter > FRAME_THRESHOLD_FOR_DISTRACTION) {
                finalStatusForThisFrame = FocusStatus.EYES_CLOSED;
            } else if (awayFramesCounter > FRAME_THRESHOLD_FOR_DISTRACTION) {
                finalStatusForThisFrame = FocusStatus.LOOKING_AWAY;
            } else {
                finalStatusForThisFrame = FocusStatus.FOCUSED;
            }
        }
        focusDataPerFrame.add(finalStatusForThisFrame);
        updateUI(finalStatusForThisFrame);
    }

    private void updateUI(FocusStatus status) {
        // ✅ FIXED: Added the UI update logic that was missing.
        runOnUiThread(() -> {
            switch (status) {
                case FOCUSED:
                    statusText.setText("Status: Focused");
                    statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
                    break;
                case LOOKING_AWAY:
                    statusText.setText("Status: Looking Away");
                    statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
                    break;
                case EYES_CLOSED:
                    statusText.setText("Status: Tiredness Detected");
                    statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                    break;
                case NO_FACE_DETECTED:
                    statusText.setText("Status: No Face Detected");
                    statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
                    break;
            }
        });
    }

    private void startFocusTimer() {
        countDownTimer = new CountDownTimer(SESSION_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // ✅ FIXED: Added the timer text update logic that was missing.
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }
            @Override
            public void onFinish() {
                timerText.setText("00:00");
                calculateResultsAndFinish();
            }
        }.start();
    }

    private void calculateResultsAndFinish() {
        if (countDownTimer != null) countDownTimer.cancel();
        cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();

        int focusedCount = 0, awayCount = 0, tiredCount = 0;
        for (FocusStatus status : focusDataPerFrame) {
            if (status == FocusStatus.FOCUSED) focusedCount++;
            else if (status == FocusStatus.LOOKING_AWAY) awayCount++;
            else if (status == FocusStatus.EYES_CLOSED) tiredCount++;
        }

        int totalFrames = focusDataPerFrame.size();
        if (totalFrames == 0) totalFrames = 1;
        int focusPercentage = (focusedCount * 100) / totalFrames;
        double avgFps = (double) totalFrames / (SESSION_DURATION_MS / 1000.0);
        if (avgFps == 0) avgFps = 1;
        int awayTimeInSeconds = (int) (awayCount / avgFps);
        int tiredTimeInSeconds = (int) (tiredCount / avgFps);

        Intent intent = new Intent(FocusSessionActivity.this, FocusSessionActivity.class);
        intent.putExtra("FOCUS_DATA_LIST", focusDataPerFrame);
        intent.putExtra("FOCUS_PERCENTAGE", focusPercentage);
        intent.putExtra("AWAY_TIME", awayTimeInSeconds);
        intent.putExtra("TIRED_TIME", tiredTimeInSeconds);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // It's a good practice to check for null before shutting down
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}