package com.blank_learn.newversion.Quiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.R;
import com.blank_learn.newversion.O2.MainActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TextView tvScore, tvSubject, tvDate, tvStudentName, tvPresentedTo;
    private View certificateCard;
    private RecyclerView rvReview;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Get Data
        int score = getIntent().getIntExtra("SCORE", 0);
        String subject = getIntent().getStringExtra("SUBJECT");

        // GET THE LIST OF QUESTIONS (***CHANGED TO USE Parcelable***)
        ArrayList<QuizReviewModel> reviewList = getIntent().getParcelableArrayListExtra("REVIEW_DATA");

        // Init Views
        tvScore = findViewById(R.id.tvScoreResult);
        tvSubject = findViewById(R.id.tvSubjectResult);
        tvDate = findViewById(R.id.tvDate);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvPresentedTo = findViewById(R.id.tvPresentedTo);
        certificateCard = findViewById(R.id.certificateContainer);
        Button btnShare = findViewById(R.id.btnShareCertificate);
        TextView btnHome = findViewById(R.id.btnHome);

        // Setup RecyclerView
        rvReview = findViewById(R.id.rvReview);
        rvReview.setLayoutManager(new LinearLayoutManager(this));
        if (reviewList != null) {
            // Check if reviewList is populated before setting adapter
            if (!reviewList.isEmpty()) {
                rvReview.setAdapter(new QuizReviewAdapter(reviewList));
            } else {
                // Optionally hide review section if list is empty
                rvReview.setVisibility(View.GONE);
            }
        }

        // Set Certificate Data
        tvScore.setText(score + "%");
        tvSubject.setText(subject != null ? subject : "General Knowledge");
        tvDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()));

        fetchUserName();

        // Share
        btnShare.setOnClickListener(v -> {
            Bitmap screenshot = takeScreenshot(certificateCard);
            String filename = "Certificate_" + System.currentTimeMillis();
            File savedFile = saveScreenshot(this, screenshot, filename);
            if (savedFile != null) {
                String appLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
                shareScreenshot(this, savedFile, appLink, score, subject);
            } else {
                Toast.makeText(this, "Failed to capture certificate.", Toast.LENGTH_SHORT).show();
            }
        });

        // Home
        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            dbRef.child("Users").child(user.getUid()).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.getValue(String.class);
                                if (name != null && !name.isEmpty()) {
                                    tvStudentName.setText(name);
                                    tvStudentName.setVisibility(View.VISIBLE);
                                    tvPresentedTo.setText("This certificate is proudly presented to");
                                } else {
                                    showNoNameLayout();
                                }
                            } else {
                                showNoNameLayout();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showNoNameLayout();
                        }
                    });
        } else {
            showNoNameLayout();
        }
    }

    private void showNoNameLayout() {
        tvStudentName.setVisibility(View.GONE);
        tvPresentedTo.setText("This officially certifies that you have mastered");
    }

    // --- SCREENSHOT & SHARE methods ---
    private Bitmap takeScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (view.getBackground() != null) {
            view.getBackground().draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE); // Default background if needed
        }
        view.draw(canvas);
        return bitmap;
    }

    private File saveScreenshot(Context context, Bitmap screenshot, String filename) {
        // Use external cache directory if external files dir fails
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            dir = context.getCacheDir();
        }

        File imagePath = new File(dir, filename + ".png");
        try (FileOutputStream fos = new FileOutputStream(imagePath)) {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return imagePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void shareScreenshot(Context context, File file, String link, int score, String subject) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        // Ensure you have configured the FileProvider in your AndroidManifest.xml
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String text = "🎓 I scored " + score + "% in " + subject + "!\nDownload App: " + link;
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(shareIntent, "Share Achievement"));
    }
}