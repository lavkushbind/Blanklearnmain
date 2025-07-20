package com.blank_learn.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.blank_learn.dark.R; // Make sure your R file is imported correctly

import org.jitsi.meet.sdk.BroadcastEvent;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.net.MalformedURLException;
import java.net.URL;

// NO "implements JitsiMeetViewListener" here
public class StudentSessionActivity extends AppCompatActivity {

    private static final String TAG = "StudentSessionActivity";
    private JitsiMeetView jitsiMeetView;

    // The BroadcastReceiver that will handle Jitsi events
    private final BroadcastReceiver jitsiBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            BroadcastEvent event = new BroadcastEvent(intent);

            switch (event.getType()) {
                case CONFERENCE_JOINED:
                    Log.d(TAG, "Conference Joined: " + event.getData());
                    Toast.makeText(context, "You have joined the class!", Toast.LENGTH_SHORT).show();
                    // You could update UI here, e.g., show participants
                    break;

                case CONFERENCE_TERMINATED:
                    Log.d(TAG, "Conference Terminated: " + event.getData());
                    // The call has ended, so finish this activity
                    finish();
                    break;

                case PARTICIPANT_JOINED:
                    Log.d(TAG, "Participant Joined: " + event.getData());
                    // This is where you would update the side list of students
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_session);

        // Get the meeting code from the previous activity (e.g., from an EditText)
        String sessionName = getIntent().getStringExtra("MEETING_CODE");
        String userName = getIntent().getStringExtra("USER_NAME");

        if (sessionName == null || userName == null) {
            Toast.makeText(this, "Error: Meeting details not provided.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Jitsi server
        initializeJitsi();

        jitsiMeetView = new JitsiMeetView(this);

        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName(userName);

        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                .setRoom(sessionName)
                .setUserInfo(userInfo)
                .setFeatureFlag("add-people.enabled", false)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("live-streaming.enabled", false)
                .setFeatureFlag("chat.enabled", true) // Allow chat
                .setFeatureFlag("raise-hand.enabled", true) // Allow raise hand
                .setFeatureFlag("video-share.enabled", false)
                .build();

        jitsiMeetView.join(options);

        // Add the Jitsi view to your layout
        FrameLayout container = findViewById(R.id.jitsi_container);
        container.addView(jitsiMeetView);

        // Register the broadcast receiver to listen for Jitsi events
        registerJitsiBroadcastReceiver();

        // Setup control buttons
        setupControls();
    }

    private void setupControls() {
        Button btnMute = findViewById(R.id.btn_mute);
        Button btnCamera = findViewById(R.id.btn_camera);
        Button btnEndClass = findViewById(R.id.btn_end_class);

        // IMPORTANT: The JitsiMeetView doesn't have public methods like isAudioMuted()
        // So we need to manage the state ourselves.
        final boolean[] isMuted = {false};
        final boolean[] isCamOff = {false};

        btnMute.setOnClickListener(v -> {
            isMuted[0] = !isMuted[0];
            // Send a command to the view to mute/unmute
            Intent muteIntent = new Intent("org.jitsi.meet.sdk.SET_AUDIO_MUTED");
            muteIntent.putExtra("muted", isMuted[0]);
            LocalBroadcastManager.getInstance(this).sendBroadcast(muteIntent);

            btnMute.setText(isMuted[0] ? "Unmute" : "Mute");
        });

        btnCamera.setOnClickListener(v -> {
            isCamOff[0] = !isCamOff[0];
            // Send a command to the view to turn camera on/off
            Intent camIntent = new Intent("org.jitsi.meet.sdk.SET_VIDEO_MUTED");
            camIntent.putExtra("muted", isCamOff[0]);
            LocalBroadcastManager.getInstance(this).sendBroadcast(camIntent);

            btnCamera.setText(isCamOff[0] ? "Cam On" : "Cam Off");
        });

        btnEndClass.setOnClickListener(v -> {
//            jitsiMeetView.layout(h); // This triggers the CONFERENCE_TERMINATED event
        });
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
            throw new RuntimeException("Failed to initialize Jitsi");
        }
    }

    private void registerJitsiBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        // Add all the event types you want to listen for
        for (BroadcastEvent.Type type : BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.getAction());
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(jitsiBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hang up the call
        if (jitsiMeetView != null) {
            jitsiMeetView.dispose();
            jitsiMeetView = null;
        }
        // Unregister the receiver to avoid memory leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(jitsiBroadcastReceiver);
    }
}