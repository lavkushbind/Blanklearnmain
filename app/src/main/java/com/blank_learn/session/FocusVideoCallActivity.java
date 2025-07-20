package com.blank_learn.session;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blank_learn.dark.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FocusVideoCallActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 102;
    private static final String TAG = "FocusVideoCallActivity";

    // UI Elements
    private EditText sessionNameEditText, userNameEditText;
    private Button joinSessionButton;
    private LinearLayout joinLayout;
    private LinearLayout lobbyLayout;

    // Firebase
    private DatabaseReference participantRef;
    private ValueEventListener statusListener;

    private String pendingSessionName;
    private String pendingUserName;

    public enum FocusStatus implements Serializable {
        FOCUSED,
        LOOKING_AWAY,
        EYES_CLOSED,
        NO_FACE_DETECTED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // UI Views ko initialize karein
        sessionNameEditText = findViewById(R.id.sessionNameEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        joinSessionButton = findViewById(R.id.joinSessionButton);
        joinLayout = findViewById(R.id.joinLayout);
        lobbyLayout = findViewById(R.id.lobbyLayout);

        initializeJitsi();

        joinSessionButton.setOnClickListener(v -> {
            String sessionName = sessionNameEditText.getText().toString().trim();
            String userName = userNameEditText.getText().toString().trim();

            if (TextUtils.isEmpty(sessionName) || TextUtils.isEmpty(userName)) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            pendingSessionName = sessionName;
            pendingUserName = userName;
            enterLobbyAndWaitForAdmission(sessionName, userName);
        });

        registerReceivers();
    }

    private void enterLobbyAndWaitForAdmission(String sessionName, String studentName) {
        joinLayout.setVisibility(View.GONE);
        lobbyLayout.setVisibility(View.VISIBLE);

        // Sabse Zaroori Step: Student ke naam ko ek valid Firebase Key banayein.
        // Saare special characters jaise '.', '#', '$', '[', ']', ' ' ko hata dein.
        String studentId = studentName.replaceAll("[.#$\\[\\]]", "").replaceAll("\\s+", "");

        // Agar naam saaf karne ke baad khali ho jaaye, to user ko error dikhayein.
        if (TextUtils.isEmpty(studentId)) {
            Toast.makeText(this, "Please enter a valid name without special characters.", Toast.LENGTH_LONG).show();
            joinLayout.setVisibility(View.VISIBLE);
            lobbyLayout.setVisibility(View.GONE);
            return;
        }

        // Ab hum 'studentId' (saaf kiya hua naam) ko key ki tarah use karenge.
        participantRef = FirebaseDatabase.getInstance().getReference("classes")
                .child(sessionName)
                .child("participants")
                .child(studentId);

        Map<String, Object> participantData = new HashMap<>();
        participantData.put("id", studentId);
        participantData.put("name", studentName); // Asli naam bhi save karein taaki web app mein dikha sakein
        participantData.put("status", "waiting");

        // Listener lagayein taaki pata chale ki data save hua ya nahi.
        participantRef.setValue(participantData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIREBASE_WRITE", "SUCCESS: Student data saved to Firebase. Ab status sunna shuru hoga.");
                    // Data save hone ke BAAD hi hum status sunna shuru karenge.
                    attachStatusListener();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE_WRITE", "FAILURE: Firebase mein data save nahi hua. Error: " + e.getMessage());
                    Toast.makeText(FocusVideoCallActivity.this, "Could not join lobby. Check connection or security rules.", Toast.LENGTH_LONG).show();
                    joinLayout.setVisibility(View.VISIBLE);
                    lobbyLayout.setVisibility(View.GONE);
                });
    }

    // Ek alag function banaya gaya hai taaki code saaf rahe.
    private void attachStatusListener() {
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("status")) {
                    String status = snapshot.child("status").getValue(String.class);
                    Log.d("LOBBY_CHECK", "Firebase status: " + status);
                    if ("admitted".equals(status)) {
                        Log.d(TAG, "User admitted! Jitsi launch kar raha hoon.");
                        checkPermissionsAndProceed();

                        if (participantRef != null) {
                            participantRef.removeEventListener(this);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase listener cancelled", error.toException());
            }
        };
        participantRef.addValueEventListener(statusListener);
    }

    private void checkPermissionsAndProceed() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_CAMERA);
            }
        }
        if (permissionsToRequest.isEmpty()) {
            startFocusServiceAndLaunchJitsi(pendingSessionName, pendingUserName);
        } else {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                startFocusServiceAndLaunchJitsi(pendingSessionName, pendingUserName);
            } else {
                Toast.makeText(this, "Camera and Foreground Service permissions are required.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startFocusServiceAndLaunchJitsi(String sessionName, String userName) {
        Intent serviceIntent = new Intent(this, FocusTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName(userName);
        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                .setRoom(sessionName)
                .setUserInfo(userInfo)
                .setFeatureFlag("add-people.enabled", false)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("live-streaming.enabled", false)
                .setFeatureFlag("meeting-password.enabled", false)
                .setFeatureFlag("video-share.enabled", false)
                .setFeatureFlag("welcomepage.enabled", false)
                .build();
        JitsiMeetActivity.launch(this, options);
    }

    private void initializeJitsi() {
        try {
            URL serverURL = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions defaultOptions = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setFeatureFlag("welcomepage.enabled", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize Jitsi", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void registerReceivers() {
        IntentFilter resultsFilter = new IntentFilter(FocusTrackingService.ACTION_RESULTS_READY);
        ContextCompat.registerReceiver(this, resultsBroadcastReceiver, resultsFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(jitsiBroadcastReceiver,
                new IntentFilter(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction()));
    }

    private final BroadcastReceiver jitsiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastEvent.Type.CONFERENCE_TERMINATED.getAction())) {
                Intent stopIntent = new Intent(FocusTrackingService.ACTION_STOP_SERVICE);
                sendBroadcast(stopIntent);
            }
        }
    };

    private final BroadcastReceiver resultsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FocusTrackingService.ACTION_RESULTS_READY)) {
                ArrayList<FocusStatus> myData = (ArrayList<FocusStatus>) intent.getSerializableExtra(FocusTrackingService.EXTRA_FOCUS_DATA);
                Intent resultsIntent = new Intent(FocusVideoCallActivity.this, Result_both_Activity.class);
                resultsIntent.putExtra("MY_FOCUS_DATA_LIST", myData);
                resultsIntent.putExtra("PARTNER_FOCUS_DATA_LIST", (ArrayList<FocusStatus>) null);
                startActivity(resultsIntent);
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(jitsiBroadcastReceiver);
        unregisterReceiver(resultsBroadcastReceiver);
        if (participantRef != null && statusListener != null) {
            participantRef.removeEventListener(statusListener);
        }
    }
}