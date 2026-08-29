package com.blank_learn.newversion.FCM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.blank_learn.dark.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.blank_learn.newversion.O2.MainActivity;

public class SamActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private ProgressBar progressBar;

    // Firebase Auth object
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sam);

        // Firebase Auth instance initialize
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);
        signUpButton = findViewById(R.id.button_signup);
        progressBar = findViewById(R.id.progressbar);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Check if user is already signed in (Optional: agar user pehle se signed in hai to seedha HomeActivity par bhej do)
        if (mAuth.getCurrentUser() != null) {
            sendUserToHomeActivity();
        }
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // --- 1. Client-Side Validation ---

        if (email.isEmpty()) {
            emailEditText.setError("Email is required.");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters.");
            passwordEditText.requestFocus();
            return;
        }

        // --- 2. Start Registration Process ---
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Sign up success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SamActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                            // User ko HomeActivity par bhej do
                            sendUserToHomeActivity();

                        } else {
                            // If sign up fails, display a message to the user.
                            // Failure typically happens if email format is invalid or email is already in use.
                            Toast.makeText(SamActivity.this, "Authentication Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendUserToHomeActivity() {
        Intent intent = new Intent(SamActivity.this, MainActivity.class);
        // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK यह सुनिश्चित करता है कि
        // जब यूजर HomeActivity में हो तो Back बटन दबाने पर वापस SignUp स्क्रीन पर न जाए।
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // MainActivity को खत्म कर दें
    }
}