package com.example.loginandsignup;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieDrawable;
import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ActivitySignupBinding;
import com.example.home.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
public class signup extends AppCompatActivity {
    FirebaseAuth auth;
    ActivitySignupBinding binding;
    EditText passbtn, emailbtn,namebtn,phonebtn;
    Button signupbtn;
    FirebaseDatabase database;
    FirebaseUser currentUser;
    TextView textView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        binding= ActivitySignupBinding.inflate(getLayoutInflater()) ;
        setContentView(binding.getRoot());
        binding.profilimg.playAnimation();
        binding.profilimg.pauseAnimation();
        auth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        textView= findViewById(R.id.golog);
        passbtn = findViewById(R.id.pasbtn);
        namebtn =findViewById(R.id .namebtn);
        emailbtn = findViewById(R.id.emailbtn);
        phonebtn = findViewById(R.id.phonebtn);
        signupbtn = findViewById(R.id.signupbtn);
        currentUser=auth.getCurrentUser();
        if(currentUser!= null)
        {
            Intent intent= new Intent(signup.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        }
        binding.golog.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(signup.this, login.class);
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               startActivity(intent);
               finish();
//               startActivity(new Intent(signup.this, login.class));
           }
        });
        binding.signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
              public void onClick(View v) {
                String email,phone,pass,name;
                phone=phonebtn.getText().toString();
                email=emailbtn.getText().toString();
                pass=passbtn.getText().toString();
                name=namebtn.getText().toString();
                if (name.isEmpty()) {
                    namebtn.setError("Name is required");
                    namebtn.requestFocus();
                    return;
                }
                if (phone.isEmpty()) {
                    phonebtn.setError("Phone number is required");
                    phonebtn.requestFocus();
                    return;
                }

                if (email.isEmpty()) {
                    emailbtn.setError("Email is required");
                    emailbtn.requestFocus();
                    return;
                }

                if (pass.isEmpty()) {
                    passbtn.setError("Password is required");
                    passbtn.requestFocus();
                    return;
                }

                if (pass.length() < 6) {
                    passbtn.setError("Password must be at least 6 characters");
                    passbtn.requestFocus();
                    return;
                }


                auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {if(task.isSuccessful())  {
                    modelfast users = new modelfast(name,phone,email,pass);
                 String id = task.getResult().getUser().getUid();
                    database.getReference().child("Users").child(id).setValue(users);
                                    startActivity(new Intent(signup.this, MainActivity.class));
                    }
                        else{
                            Toast.makeText(signup.this,task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
//        animationView.playAnimation();
        binding.profilimg.setRepeatCount(LottieDrawable.INFINITE); // Loop indefinitely

        binding.profilimg.playAnimation(); // Ensure it's playing when the activity resumes

    }

    @Override
    protected void onPause() {
//        animationView.pauseAnimation(); // Pause when the activity is paused
        super.onPause();
    }
}