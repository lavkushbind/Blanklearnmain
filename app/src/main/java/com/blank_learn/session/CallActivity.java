package com.blank_learn.session;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = "CallActivity";
    private static final String STUN_SERVER = "stun:stun.l.google.com:19302";
    private String roomCode;
    private String myParticipantId;
    private String teacherParticipantId;

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private AudioTrack localAudioTrack;
    private SurfaceViewRenderer teacherVideoView;
    private EglBase eglBase;

    private DatabaseReference roomRef;
    private ChildEventListener signalsListener;
    private ValueEventListener roomStatusListener;
    private ValueEventListener teacherMuteListener;

    private FloatingActionButton micButton;
    private TextView statusText;

    private boolean isMicOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        roomCode = getIntent().getStringExtra("ROOM_CODE");
        if (roomCode == null || roomCode.isEmpty()) {
            finish();
            return;
        }

        teacherVideoView = findViewById(R.id.teacher_video_view);
        micButton = findViewById(R.id.mic_button);
        statusText = findViewById(R.id.status_text);
        findViewById(R.id.end_call_button).setOnClickListener(v -> endCall());

        micButton.setOnClickListener(v -> toggleMic());

        // Initialize WebRTC
        initializeWebRTC();

        // Connect to Firebase and join room
        joinRoom();
    }

    private void initializeWebRTC() {
        eglBase = EglBase.create();
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableInternalTracer(true)
                .createInitializationOptions());

        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglBase.getEglBaseContext()))
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglBase.getEglBaseContext(), true, true))
                .setOptions(new PeerConnectionFactory.Options());

        peerConnectionFactory = builder.createPeerConnectionFactory();

        // Initialize SurfaceViewRenderer
        teacherVideoView.init(eglBase.getEglBaseContext(), null);
        teacherVideoView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        teacherVideoView.setMirror(false);
        teacherVideoView.setEnableHardwareScaler(true);

        // Create local audio track
        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);
    }

    private void joinRoom() {
        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomCode);
        myParticipantId = roomRef.child("participants").push().getKey();

        Map<String, Object> participantData = new HashMap<>();
        participantData.put("role", "student");
        participantData.put("mic_muted", false); // Initially unmuted, teacher will control

        // Add self to participants list
        roomRef.child("participants").child(myParticipantId).setValue(participantData)
                .addOnSuccessListener(aVoid -> findTeacherAndInitiateCall());

        // Listen for when the teacher ends the call
        roomStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ("ended".equals(snapshot.getValue(String.class))) {
                    Toast.makeText(CallActivity.this, "Class has ended.", Toast.LENGTH_LONG).show();
                    endCall();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomRef.child("status").addValueEventListener(roomStatusListener);
    }

    private void findTeacherAndInitiateCall() {
        roomRef.child("participants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    if ("teacher".equals(child.child("role").getValue(String.class))) {
                        teacherParticipantId = child.getKey();
                        statusText.setText("Connecting to Teacher...");
                        setupPeerConnection();
                        createOffer();
                        listenForSignals();
                        listenForMuteControl();
                        return;
                    }
                }
                statusText.setText("Waiting for Teacher...");
                // Add a listener to wait for teacher to join
                roomRef.child("participants").addChildEventListener(new ChildEventListener() {
                    // Implement logic to detect when teacher joins
                    @Override public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if ("teacher".equals(snapshot.child("role").getValue(String.class))) {
                            teacherParticipantId = snapshot.getKey();
                            statusText.setText("Connecting to Teacher...");
                            setupPeerConnection();
                            createOffer();
                            listenForSignals();
                            listenForMuteControl();
                            roomRef.child("participants").removeEventListener(this); // Stop listening after teacher is found
                        }
                    }
                    @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to find teacher", error.toException());
                endCall();
            }
        });
    }

    private void setupPeerConnection() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder(STUN_SERVER).createIceServer());

        PeerConnection.Observer observer = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.d(TAG, "onSignalingChange: " + signalingState);
            }
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: " + iceConnectionState);
                runOnUiThread(() -> statusText.setText(iceConnectionState.toString()));
                if (iceConnectionState == PeerConnection.IceConnectionState.CONNECTED) {
                    statusText.setVisibility(View.GONE);
                }
            }
            @Override
            public void onIceConnectionReceivingChange(boolean b) {}
            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                sendSignal(iceCandidate);
            }
            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
            @Override
            public void onAddStream(MediaStream mediaStream) {
                Log.d(TAG, "onAddStream: Received remote stream.");
                if (mediaStream.videoTracks.size() > 0) {
                    VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                    runOnUiThread(() -> remoteVideoTrack.addSink(teacherVideoView));
                }
            }
            @Override
            public void onRemoveStream(MediaStream mediaStream) {}
            @Override
            public void onDataChannel(DataChannel dataChannel) {}
            @Override
            public void onRenegotiationNeeded() {}
            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
        };

        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, observer);
        peerConnection.addTrack(localAudioTrack);
    }

    private void createOffer() {
        MediaConstraints sdpConstraints = new MediaConstraints();
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));

        peerConnection.createOffer(new SdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription);
                sendSignal(sessionDescription);
            }
            @Override
            public void onSetSuccess() {}
            @Override
            public void onCreateFailure(String s) { Log.e(TAG, "onCreateOfferFailure: " + s); }
            @Override
            public void onSetFailure(String s) { Log.e(TAG, "onSetFailure: " + s); }
        }, sdpConstraints);
    }

    private void listenForSignals() {
        DatabaseReference signalRef = roomRef.child("participants").child(myParticipantId).child("webrtc_signals");
        signalsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    String data = snapshot.getValue(String.class);
                    JSONObject json = new JSONObject(data);
                    String type = json.getString("type");
                    if ("ANSWER".equals(type)) {
                        String sdp = json.getString("sdp");
                        SessionDescription answerSdp = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                        peerConnection.setRemoteDescription(new SdpObserver() {
                            @Override public void onCreateSuccess(SessionDescription sd) {}
                            @Override public void onSetSuccess() { Log.d(TAG, "setRemoteDescription success"); }
                            @Override public void onCreateFailure(String s) {}
                            @Override public void onSetFailure(String s) { Log.e(TAG, "setRemoteDescription failed: " + s); }
                        }, answerSdp);
                    } else if ("CANDIDATE".equals(type)) {
                        String sdpMid = json.getString("sdpMid");
                        int sdpMLineIndex = json.getInt("sdpMLineIndex");
                        String sdpCandidate = json.getString("candidate");
                        IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdpCandidate);
                        peerConnection.addIceCandidate(candidate);
                    }
                    // Clean up the signal after processing
                    snapshot.getRef().removeValue();
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing signal", e);
                }
            }
            // Other ChildEventListener methods...
            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        signalRef.addChildEventListener(signalsListener);
    }

    private void listenForMuteControl() {
        DatabaseReference muteRef = roomRef.child("participants").child(myParticipantId).child("mic_muted");
        teacherMuteListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isMuted = snapshot.getValue(Boolean.class);
                if (isMuted != null) {
                    localAudioTrack.setEnabled(!isMuted);
                    micButton.setImageResource(isMuted ? R.drawable.microphone : R.drawable.microphone);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        };
        muteRef.addValueEventListener(teacherMuteListener);
    }

    private void sendSignal(Object signal) {
        try {
            JSONObject json = new JSONObject();
            if (signal instanceof SessionDescription) {
                SessionDescription sdp = (SessionDescription) signal;
                json.put("type", sdp.type.canonicalForm().toUpperCase());
                json.put("sdp", sdp.description);
            } else if (signal instanceof IceCandidate) {
                IceCandidate candidate = (IceCandidate) signal;
                json.put("type", "CANDIDATE");
                json.put("sdpMid", candidate.sdpMid);
                json.put("sdpMLineIndex", candidate.sdpMLineIndex);
                json.put("candidate", candidate.sdp);
            }
            DatabaseReference signalRef = roomRef.child("participants").child(teacherParticipantId).child("webrtc_signals");
            signalRef.push().setValue(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void toggleMic() {
        isMicOn = !isMicOn;
        localAudioTrack.setEnabled(isMicOn);
        micButton.setImageResource(isMicOn ? R.drawable.microphone : R.drawable.microphone);
        // Note: This only mutes locally. Teacher has master control.
    }

    private void endCall() {
        // Clean up Firebase entry
        if (roomRef != null && myParticipantId != null) {
            roomRef.child("participants").child(myParticipantId).removeValue();
            // Remove listeners
            if (signalsListener != null) roomRef.child("participants").child(myParticipantId).child("webrtc_signals").removeEventListener(signalsListener);
            if (roomStatusListener != null) roomRef.child("status").removeEventListener(roomStatusListener);
            if (teacherMuteListener != null) roomRef.child("participants").child(myParticipantId).child("mic_muted").removeEventListener(teacherMuteListener);
        }

        // Clean up WebRTC resources
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }
        if (teacherVideoView != null) {
            teacherVideoView.release();
        }
        if (eglBase != null) {
            eglBase.release();
        }
        PeerConnectionFactory.shutdownInternalTracer();

        finish();
    }

    @Override
    protected void onDestroy() {
        endCall();
        super.onDestroy();
    }
}