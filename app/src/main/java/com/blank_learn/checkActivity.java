package com.blank_learn;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.google.firebase.database.*;

public class checkActivity extends AppCompatActivity {

    EditText inputWord;
    Button uploadBtn;
    TextView displayWord;

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);  // activity_check.xml hona chahiye

        inputWord = findViewById(R.id.inputWord);
        uploadBtn = findViewById(R.id.uploadBtn);
        displayWord = findViewById(R.id.displayWord);

        // Firebase ka reference
        dbRef = FirebaseDatabase.getInstance().getReference("wordNode");

        // Upload Button Click Listener
        uploadBtn.setOnClickListener(v -> {
            String word = inputWord.getText().toString().trim();

            if (!word.isEmpty()) {
                dbRef.setValue(word)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Uploaded to Firebase", Toast.LENGTH_SHORT).show();
                            Log.d("Firebase", "Uploaded word: " + word);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                            Log.e("Firebase", "Upload Error: ", e);
                        });
            } else {
                Toast.makeText(this, "Please enter a word", Toast.LENGTH_SHORT).show();
            }
        });

        // Firebase Listener for Realtime Data
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String word = snapshot.getValue(String.class);
                if (word != null) {
                    displayWord.setText("Firebase word: " + word);
                    Log.d("Firebase", "Fetched word: " + word);
                } else {
                    displayWord.setText("No data found.");
                    Log.d("Firebase", "No word in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: ", error.toException());
            }
        });
    }
}
