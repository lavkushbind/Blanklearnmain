package com.blank_learn.newversion.Quiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blank_learn.dark.R;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizActivity extends AppCompatActivity {

    // --- API CONFIGURATION (Same as your working SolutionActivity) ---
    private static final String API_KEY = "AIzaSyBQ6ooxe4_Qby_7EmKOXqlRouiCirzQ8ko"; // Aapki Quiz wali key
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    // Views
    private ConstraintLayout layoutSetup, layoutGame;
    private TextView tvQuestion, tvPoints, tvProgressText;
    private Button btnOp1, btnOp2, btnOp3, btnOp4, btnNext, btnStartQuiz;
    private ProgressBar progressBar;
    private Spinner spClass, spSubject, spExam;
    private LinearProgressIndicator quizProgressIndicator;

    // --- HISTORY LIST (To prevent duplicates) ---
    private ArrayList<String> askedQuestionsHistory = new ArrayList<>();

    // Game Variables
    private int currentScore = 0;
    private int difficultyLevel = 10;
    private int questionCount = 1;
    private final int TOTAL_QUESTIONS = 10;
    private String correctAnswer = "";

    // Filters
    private String selectedClass = "Class 10";
    private String selectedSubject = "Science";
    private String selectedExam = "Board Exam";

    // Firebase
    private SharedPreferences prefs;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 1. Initialize Firebase & Analytics ---
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        fbLogger = AppEventsLogger.newLogger(this);

        // --- 2. DAILY LIMIT CHECK ---
        prefs = getSharedPreferences("DailyQuizPrefs", MODE_PRIVATE);
        String lastPlayedDate = prefs.getString("last_played", "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (lastPlayedDate.equals(todayDate)) {
            Toast.makeText(this, "⚠️ You have completed today's quiz! Come back tomorrow.", Toast.LENGTH_LONG).show();
            // finish(); return;
        }

        setContentView(R.layout.activity_quiz);

        // --- 3. Initialize Views ---
        layoutSetup = findViewById(R.id.layoutSetup);
        layoutGame = findViewById(R.id.layoutGame);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvPoints = findViewById(R.id.tvPoints);
        tvProgressText = findViewById(R.id.tvProgress);

        btnOp1 = findViewById(R.id.btnOption1);
        btnOp2 = findViewById(R.id.btnOption2);
        btnOp3 = findViewById(R.id.btnOption3);
        btnOp4 = findViewById(R.id.btnOption4);
        btnNext = findViewById(R.id.btnNext);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);

        progressBar = findViewById(R.id.progressBar);

        spClass = findViewById(R.id.spClass);
        spSubject = findViewById(R.id.spSubject);
        spExam = findViewById(R.id.spExam);

        quizProgressIndicator = findViewById(R.id.quizProgressIndicator);

        // Setup Spinners
        setupFilters();

        // --- 4. Logic: Start Button Click ---
        btnStartQuiz.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();
                return;
            }
            logEvent("quiz_started", null);
            layoutSetup.setVisibility(View.GONE);
            layoutGame.setVisibility(View.VISIBLE);
            loadQuestionFromAI();
        });

        // --- 5. Game Listeners ---
        View.OnClickListener answerListener = view -> checkAnswer((Button) view);
        btnOp1.setOnClickListener(answerListener);
        btnOp2.setOnClickListener(answerListener);
        btnOp3.setOnClickListener(answerListener);
        btnOp4.setOnClickListener(answerListener);

        btnNext.setOnClickListener(v -> {
            resetButtonColors();
            btnNext.setVisibility(View.GONE);

            if (questionCount >= TOTAL_QUESTIONS && !btnNext.getText().toString().equals("Retry")) {
                finishQuiz();
            } else {
                if(!btnNext.getText().toString().equals("Retry")) {
                    questionCount++;
                }
                btnNext.setText("Next");
                loadQuestionFromAI();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
        return false;
    }

    private void setupFilters() {
        String[] classes = {"Class Kg", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"};
        String[] subjects = {"Physics", "Chemistry", "Maths", "Biology", "English", "History", "General Knowledge"};
        String[] exams = {"School Exam", "Board Exam", "JEE", "NEET", "UPSC", "None"};

        setupSpinner(spClass, classes, item -> selectedClass = item);
        setupSpinner(spSubject, subjects, item -> selectedSubject = item);
        setupSpinner(spExam, exams, item -> selectedExam = item);
    }

    private void setupSpinner(Spinner spinner, String[] data, OnItemSelectedListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, data) {
            @NonNull @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                return view;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                listener.onItemSelected(data[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    interface OnItemSelectedListener {
        void onItemSelected(String item);
    }

    // --- REWRITTEN TO USE OKHTTP (LIKE YOUR SOLUTION ACTIVITY) ---
    private void loadQuestionFromAI() {
        if (!isNetworkAvailable()) {
            tvQuestion.setText("Internet Disconnected!");
            btnNext.setText("Retry");
            btnNext.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvQuestion.setText("AI is crafting a unique question...");
        enableButtons(false);
        btnNext.setVisibility(View.GONE);

        tvProgressText.setText("Q: " + questionCount + "/" + TOTAL_QUESTIONS);
        quizProgressIndicator.setProgress(questionCount * 10);

        StringBuilder historyPrompt = new StringBuilder();
        if (!askedQuestionsHistory.isEmpty()) {
            historyPrompt.append(" Do NOT repeat these questions: [");
            for (String q : askedQuestionsHistory) {
                historyPrompt.append("\"").append(q).append("\", ");
            }
            historyPrompt.append("]. Generate NEW.");
        }

        String prompt = "Generate 1 Unique MCQ question.\n" +
                "Target: " + selectedClass + ". Subject: " + selectedSubject + ". Exam: " + selectedExam + ".\n" +
                "Difficulty: " + difficultyLevel + " (Scale 1-100).\n" +
                historyPrompt.toString() +
                "\nReturn ONLY a JSON format exactly like this: {\"question\": \"...\", \"options\": [\"A\", \"B\", \"C\", \"D\"], \"answer\": \"Correct Option Text\"}";

        // Manual JSON Body construction for Gemini
        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject userTurn = new JSONObject();
            userTurn.put("role", "user");

            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.put(textPart);

            userTurn.put("parts", parts);
            contents.put(userTurn);
            jsonBody.put("contents", contents);

            // Force JSON Response format for accuracy
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("response_mime_type", "application/json");
            jsonBody.put("generationConfig", generationConfig);

        } catch (Exception e) { e.printStackTrace(); }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvQuestion.setText("Network error: " + e.getMessage() + "\nTap Retry.");
                    btnNext.setText("Retry");
                    btnNext.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);

                        JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                        JSONObject content = candidate.getJSONObject("content");
                        String aiText = content.getJSONArray("parts").getJSONObject(0).getString("text");

                        runOnUiThread(() -> parseAndDisplayQuestion(aiText));

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> showRetryError());
                    }
                } else {
                    runOnUiThread(() -> showRetryError());
                }
            }
        });
    }

    private void showRetryError() {
        progressBar.setVisibility(View.GONE);
        tvQuestion.setText("AI sent an invalid format. Tap Retry.");
        enableButtons(false);
        btnNext.setText("Retry");
        btnNext.setVisibility(View.VISIBLE);
    }

    private void parseAndDisplayQuestion(String jsonString) {
        progressBar.setVisibility(View.GONE);
        enableButtons(true);

        try {
            String cleanJson = jsonString.trim();
            if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
            if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
            if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            cleanJson = cleanJson.trim();

            JSONObject json = new JSONObject(cleanJson);
            String questionText = json.getString("question");

            askedQuestionsHistory.add(questionText);

            tvQuestion.setText(questionText);
            correctAnswer = json.getString("answer");
            JSONArray opts = json.getJSONArray("options");

            btnOp1.setText(opts.getString(0));
            btnOp2.setText(opts.getString(1));
            btnOp3.setText(opts.getString(2));
            btnOp4.setText(opts.getString(3));

        } catch (Exception e) {
            e.printStackTrace();
            showRetryError();
        }
    }

    private void checkAnswer(Button selectedButton) {
        String selectedText = selectedButton.getText().toString();

        if (selectedText.equals(correctAnswer)) {
            selectedButton.setBackgroundResource(R.drawable.bg_correct_option);
            currentScore += 10;
            if (difficultyLevel < 100) difficultyLevel++;
        } else {
            selectedButton.setBackgroundResource(R.drawable.bg_wrong_option);
            highlightCorrectAnswer();
            if (difficultyLevel > 5) difficultyLevel--;
        }

        tvPoints.setText("💎 " + currentScore);
        enableButtons(false);

        btnNext.setText(questionCount >= TOTAL_QUESTIONS ? "Finish Quiz" : "Next Question");
        btnNext.setVisibility(View.VISIBLE);
    }

    private void finishQuiz() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        prefs.edit().putString("last_played", todayDate).apply();

        updatePointsInFirebase(currentScore);

        Bundle bundle = new Bundle();
        bundle.putInt("score", currentScore);
        bundle.putString("subject", selectedSubject);
        logEvent("quiz_completed", bundle);

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("SCORE", currentScore);
        intent.putExtra("SUBJECT", selectedSubject);
        startActivity(intent);
        finish();
    }

    private void updatePointsInFirebase(int newPoints) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = dbRef.child("Users").child(uid).child("quiz_points");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long currentPoints = 0;
                    if (snapshot.exists()) {
                        try {
                            currentPoints = snapshot.getValue(Long.class);
                        } catch (Exception e) {
                            currentPoints = Long.parseLong(snapshot.getValue(String.class));
                        }
                    }
                    userRef.setValue(currentPoints + newPoints);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void logEvent(String eventName, Bundle bundle) {
        mFirebaseAnalytics.logEvent(eventName, bundle);
        fbLogger.logEvent(eventName, bundle);
    }

    private void highlightCorrectAnswer() {
        if (btnOp1.getText().toString().equals(correctAnswer)) btnOp1.setBackgroundResource(R.drawable.bg_correct_option);
        if (btnOp2.getText().toString().equals(correctAnswer)) btnOp2.setBackgroundResource(R.drawable.bg_correct_option);
        if (btnOp3.getText().toString().equals(correctAnswer)) btnOp3.setBackgroundResource(R.drawable.bg_correct_option);
        if (btnOp4.getText().toString().equals(correctAnswer)) btnOp4.setBackgroundResource(R.drawable.bg_correct_option);
    }

    private void enableButtons(boolean enable) {
        btnOp1.setEnabled(enable); btnOp2.setEnabled(enable);
        btnOp3.setEnabled(enable); btnOp4.setEnabled(enable);
    }

    private void resetButtonColors() {
        btnOp1.setBackgroundResource(R.drawable.bg_glass_button);
        btnOp2.setBackgroundResource(R.drawable.bg_glass_button);
        btnOp3.setBackgroundResource(R.drawable.bg_glass_button);
        btnOp4.setBackgroundResource(R.drawable.bg_glass_button);
    }
}