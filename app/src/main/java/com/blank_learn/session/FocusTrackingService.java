package com.blank_learn.session;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// A LifecycleOwner is required for CameraX, so we create a custom one for the Service
public class FocusTrackingService extends Service implements LifecycleOwner {

    private static final String TAG = "FocusTrackingService";
    public static final String ACTION_STOP_SERVICE = "com.dark.accountable.ACTION_STOP_SERVICE";
    public static final String ACTION_RESULTS_READY = "com.dark.accountable.ACTION_RESULTS_READY";
    public static final String EXTRA_FOCUS_DATA = "FOCUS_DATA_LIST";
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "FocusServiceChannel";

    // --- ML Kit and CameraX variables ---
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private ProcessCameraProvider cameraProvider;

    // --- Data Collection ---
    private final ArrayList<FocusVideoCallActivity.FocusStatus> focusDataPerFrame = new ArrayList<>();
    private int awayFramesCounter = 0;
    private int tiredFramesCounter = 0;
    private static final int FRAME_THRESHOLD_FOR_DISTRACTION = 10;
    private static final float HEAD_ANGLE_THRESHOLD = 20.0f;
    private static final float EYE_OPEN_PROB_THRESHOLD = 0.4f;

    // --- Custom Lifecycle for CameraX ---
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    // --- Broadcast Receiver to stop the service ---
    private final BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                stopTrackingAndSendResults();
            }
        }
    };

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);


        // BEST PRACTICE: Use ContextCompat to handle version differences automatically.
        IntentFilter stopFilter = new IntentFilter(ACTION_STOP_SERVICE);
        ContextCompat.registerReceiver(this, stopServiceReceiver, stopFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Accountable Focus Session")
                .setContentText("Actively tracking focus...")
                .setSmallIcon(R.mipmap.ic_launcher) // Make sure you have this icon
                .build();
        startForeground(NOTIFICATION_ID, notification);
        Log.d(TAG, "onStartCommand: Service has been started and is in foreground.");

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
        initializeAndStartCamera();

        return START_NOT_STICKY;
    }

    private void initializeAndStartCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cameraProviderFuture.addListener(() -> {
                try {
                    cameraProvider = cameraProviderFuture.get();
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
                    cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis); // No preview needed
                    Log.d(TAG, "Camera is bound and tracking started.");

                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Camera start failed.", e);
                }
            }, getMainExecutor());
        }
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
                    Log.v(TAG, "analyzeFrame: Face detection success. Number of faces: " + faces.size());

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Face detection failed", e);
                    imageProxy.close();
                });
    }

    private void processFaceResults(List<Face> faces) {
        FocusVideoCallActivity.FocusStatus finalStatusForThisFrame;
        if (faces.isEmpty()) {
            finalStatusForThisFrame = FocusVideoCallActivity.FocusStatus.NO_FACE_DETECTED;
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
                finalStatusForThisFrame = FocusVideoCallActivity.FocusStatus.EYES_CLOSED;
            } else if (awayFramesCounter > FRAME_THRESHOLD_FOR_DISTRACTION) {
                finalStatusForThisFrame = FocusVideoCallActivity.FocusStatus.LOOKING_AWAY;
            } else {
                finalStatusForThisFrame = FocusVideoCallActivity.FocusStatus.FOCUSED;
            }
        }
        focusDataPerFrame.add(finalStatusForThisFrame);
        Log.v(TAG, "Frame processed. Status: " + finalStatusForThisFrame);
    }

    private void stopTrackingAndSendResults() {
        Log.d(TAG, "Stopping tracking and sending results.");
        // Stop camera and ML Kit
        if (cameraProvider != null) cameraProvider.unbindAll();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (faceDetector != null) faceDetector.close();

        // Send results via broadcast
        Intent resultsIntent = new Intent(ACTION_RESULTS_READY);
        resultsIntent.putExtra(EXTRA_FOCUS_DATA, focusDataPerFrame);
        sendBroadcast(resultsIntent);

        stopSelf(); // Stop the service
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        unregisterReceiver(stopServiceReceiver);
        Log.d(TAG, "Service destroyed.");
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
    @NonNull @Override public Lifecycle getLifecycle() { return lifecycleRegistry; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Focus Tracking Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
} 