package com.example.home;



import android.os.Bundle;
        import android.text.TextUtils;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;

import com.example.dark.R;
import com.google.firebase.FirebaseException;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.PhoneAuthCredential;
        import com.google.firebase.auth.PhoneAuthProvider;

        import java.util.concurrent.TimeUnit;

public class phone_number_signup extends AppCompatActivity {

    private EditText phoneNumberField, otpField;
    private Button sendCodeButton, verifyCodeButton;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_signup);

        phoneNumberField = findViewById(R.id.phone_number);
        otpField = findViewById(R.id.otp);
        sendCodeButton = findViewById(R.id.send_code);
        verifyCodeButton = findViewById(R.id.verify_code);

        mAuth = FirebaseAuth.getInstance();

        sendCodeButton.setOnClickListener(v -> sendVerificationCode());
        verifyCodeButton.setOnClickListener(v -> verifyCode());
    }

    private void sendVerificationCode() {
        String phoneNumber = phoneNumberField.getText().toString().trim();

        // Validate the phone number for India
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phoneNumber.startsWith("+91")) {
            phoneNumber = "+91" + phoneNumber; // Automatically add country code if missing
        }

        if (phoneNumber.length() != 13) { // +91 + 10-digit number
            Toast.makeText(this, "Enter a valid 10-digit phone number (e.g., 9876543210)", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(phone_number_signup.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationId = s;
                        Toast.makeText(phone_number_signup.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyCode() {
        String code = otpField.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithCredential(credential);
        } else {
            Toast.makeText(this, "Verification ID not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(phone_number_signup.this, "Signup Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(phone_number_signup.this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
