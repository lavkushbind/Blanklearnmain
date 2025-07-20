package com.blank_learn.session;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Objects;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Main_Call_Activity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText roomCodeEditText;
    private Button joinButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_call);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        roomCodeEditText = findViewById(R.id.roomCodeEditText);
        joinButton = findViewById(R.id.joinButton);
        progressBar = findViewById(R.id.progressBar);

        // Authenticate with Firebase anonymously
        signInAnonymously();

        joinButton.setOnClickListener(v -> {
            String roomCode = Objects.requireNonNull(roomCodeEditText.getText()).toString().trim();
            if (roomCode.isEmpty()) {
                Toast.makeText(this, "Please enter a room code", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissionsAndJoin(roomCode);
            }
        });
    }

    private void signInAnonymously() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Log.d("MainActivity", "signInAnonymously:success");
                joinButton.setEnabled(true);
            } else {
                Log.w("MainActivity", "signInAnonymously:failure", task.getException());
                Toast.makeText(Main_Call_Activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                joinButton.setEnabled(false);
            }
        });
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_CODE)
    private void requestPermissionsAndJoin(String roomCode) {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            joinRoom(roomCode);
        } else {
            EasyPermissions.requestPermissions(this, "This app needs camera and audio permissions to function", PERMISSION_REQUEST_CODE, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void joinRoom(String roomCode) {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference roomRef = mDatabase.child("rooms").child(roomCode).child("participants");

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(Main_Call_Activity.this, "Room does not exist.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (snapshot.getChildrenCount() >= 6) {
                    Toast.makeText(Main_Call_Activity.this, "Room is full.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Intent intent = new Intent(Main_Call_Activity.this, CallActivity.class);
                    intent.putExtra("ROOM_CODE", roomCode);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Main_Call_Activity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }


 }