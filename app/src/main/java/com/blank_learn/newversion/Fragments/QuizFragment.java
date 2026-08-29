package com.blank_learn.newversion.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;
import com.blank_learn.newversion.Quiz.QuizReviewModel;
import com.blank_learn.newversion.Quiz.ResultActivity;

// AI Imports
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

// Firebase & Analytics Imports
import com.google.firebase.analytics.FirebaseAnalytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class QuizFragment extends Fragment {

    // Views
    private ConstraintLayout layoutSetup, layoutGame;
    private TextView tvQuestion, tvPoints, tvProgressText;
    private Button btnOp1, btnOp2, btnOp3, btnOp4, btnNext, btnStartQuiz;
    private ProgressBar progressBar;
    private Spinner spClass, spSubject, spExam;
    private LinearProgressIndicator quizProgressIndicator;

    // Analytics & Firebase
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppEventsLogger fbLogger;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    // Data Lists
    private ArrayList<String> askedQuestionsHistory = new ArrayList<>();
    private ArrayList<QuizReviewModel> quizReviewList = new ArrayList<>();

    // Game Variables
    private int currentScore = 0;
    private int difficultyLevel = 10;
    private int questionCount = 1;
    private final int TOTAL_QUESTIONS = 10;

    // Question Data
    private String correctAnswer = "";
    private String currentQuestionText = "";
    private String currentExplanation = "";

    // Filters
    private String selectedClass = "Class 10";
    private String selectedSubject = "Science";
    private String selectedExam = "Board Exam";

    private GenerativeModelFutures model;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Init Firebase & Analytics
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        fbLogger = AppEventsLogger.newLogger(requireContext());

        // 2. Init Views
        layoutSetup = view.findViewById(R.id.layoutSetup);
        layoutGame = view.findViewById(R.id.layoutGame);
        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvPoints = view.findViewById(R.id.tvPoints);
        tvProgressText = view.findViewById(R.id.tvProgress);
        btnOp1 = view.findViewById(R.id.btnOption1);
        btnOp2 = view.findViewById(R.id.btnOption2);
        btnOp3 = view.findViewById(R.id.btnOption3);
        btnOp4 = view.findViewById(R.id.btnOption4);
        btnNext = view.findViewById(R.id.btnNext);
        btnStartQuiz = view.findViewById(R.id.btnStartQuiz);
        progressBar = view.findViewById(R.id.progressBar);
        spClass = view.findViewById(R.id.spClass);
        spSubject = view.findViewById(R.id.spSubject);
        spExam = view.findViewById(R.id.spExam);
        quizProgressIndicator = view.findViewById(R.id.quizProgressIndicator);

        setupFilters();

        // 3. Gemini AI Setup
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", "AIzaSyCtEawO-6eZ6yv7vwb2ZPLZhFJxcH85gJM");
        model = GenerativeModelFutures.from(gm);

        // 4. Start Quiz
        btnStartQuiz.setOnClickListener(v -> {
            // Analytics: Quiz Started
            Bundle params = new Bundle();
            params.putString("subject", selectedSubject);
            params.putString("class", selectedClass);
            logEvent("quiz_started", params);

            // Reset Data
            currentScore = 0;
            questionCount = 1;
            difficultyLevel = 10;
            askedQuestionsHistory.clear();
            quizReviewList.clear();
            tvPoints.setText("💎 0");

            layoutSetup.setVisibility(View.GONE);
            layoutGame.setVisibility(View.VISIBLE);
            loadQuestionFromAI();
        });

        // 5. Game Listeners
        View.OnClickListener answerListener = v -> checkAnswer((Button) v);
        btnOp1.setOnClickListener(answerListener);
        btnOp2.setOnClickListener(answerListener);
        btnOp3.setOnClickListener(answerListener);
        btnOp4.setOnClickListener(answerListener);

        btnNext.setOnClickListener(v -> {
            resetButtonColors();
            loadQuestionFromAI();
            btnNext.setVisibility(View.GONE);
        });
    }

    private void setupFilters() {
        String[] classes = {"Class Kg", "Class 1", "Class 2", "Class 3", "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10", "Class 11", "Class 12"};
        String[] subjects = {"Physics", "Chemistry", "Maths", "Biology", "English", "History", "Computer science", "General Knowledge"};
        String[] exams = {"School Exam", "Board Exam", "JEE", "NEET", "UPSC", "None"};

        setupSpinner(spClass, classes, item -> selectedClass = item);
        setupSpinner(spSubject, subjects, item -> selectedSubject = item);
        setupSpinner(spExam, exams, item -> selectedExam = item);
    }

    private void setupSpinner(Spinner spinner, String[] data, OnItemSelectedListener listener) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, data) {
            @NonNull
            @Override
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
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    interface OnItemSelectedListener {
        void onItemSelected(String item);
    }

    private void loadQuestionFromAI() {
        progressBar.setVisibility(View.VISIBLE);
        tvQuestion.setText("AI is crafting a unique question...");
        enableButtons(false);
        tvProgressText.setText("Q: " + questionCount + "/" + TOTAL_QUESTIONS);
        quizProgressIndicator.setProgress(questionCount * 10);

        StringBuilder historyPrompt = new StringBuilder();
        if (!askedQuestionsHistory.isEmpty()) {
            historyPrompt.append(" CRITICAL: Do NOT repeat: [");
            for (String q : askedQuestionsHistory) {
                historyPrompt.append("\"").append(q).append("\", ");
            }
            historyPrompt.append("]. Generate NEW question.");
        }

        String prompt = "Generate 1 Unique MCQ question." +
                " Target: " + selectedClass + "." +
                " Subject: " + selectedSubject + "." +
                " Exam Prep: " + selectedExam + "." +
                " Difficulty: " + difficultyLevel + " (Scale 1-100). " +
                historyPrompt.toString() +
                " Return ONLY raw JSON. No markdown. Format: { \"question\": \"...\", \"options\": [\"A\", \"B\", \"C\", \"D\"], \"answer\": \"Option Text\", \"explanation\": \"Short 1-line explanation\" }";

        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> parseAndDisplayQuestion(result.getText()));
                }
            }
            @Override
            public void onFailure(Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvQuestion.setText("Connection Failed. Retrying...");
                    });
                }
            }
        }, executor);
    }

    private void parseAndDisplayQuestion(String jsonString) {
        progressBar.setVisibility(View.GONE);
        enableButtons(true);
        try {
            if (jsonString.contains("```json")) jsonString = jsonString.replace("```json", "").replace("```", "");
            if (jsonString.contains("```")) jsonString = jsonString.replace("```", "");

            JSONObject json = new JSONObject(jsonString);
            currentQuestionText = json.getString("question");
            correctAnswer = json.getString("answer");
            currentExplanation = json.has("explanation") ? json.getString("explanation") : "Not available.";

            askedQuestionsHistory.add(currentQuestionText);
            tvQuestion.setText(currentQuestionText);
            JSONArray opts = json.getJSONArray("options");

            btnOp1.setText(opts.getString(0));
            btnOp2.setText(opts.getString(1));
            btnOp3.setText(opts.getString(2));
            btnOp4.setText(opts.getString(3));
        } catch (Exception e) {
            loadQuestionFromAI();
        }
    }

    private void checkAnswer(Button selectedButton) {
        String selectedText = selectedButton.getText().toString();
        boolean isCorrect = selectedText.equals(correctAnswer);

        if (isCorrect) {
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

        quizReviewList.add(new QuizReviewModel(currentQuestionText, selectedText, correctAnswer, currentExplanation, isCorrect));

        if (questionCount >= TOTAL_QUESTIONS) {
            finishQuiz();
        } else {
            questionCount++;
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void finishQuiz() {
        // 1. Update Firebase Points
        updatePointsInFirebase(currentScore);

        // 2. Log Analytics (Quiz Completed)
        Bundle params = new Bundle();
        params.putInt("score", currentScore);
        params.putString("subject", selectedSubject);
        logEvent("quiz_completed", params);

        // 3. Move to Result
        Intent intent = new Intent(requireContext(), ResultActivity.class);
        int percentage = (currentScore / (TOTAL_QUESTIONS * 10)) * 100;
        if (percentage > 100) percentage = 100;
        if (percentage == 0 && currentScore > 0) percentage = currentScore;

        intent.putExtra("SCORE", percentage);
        intent.putExtra("SUBJECT", selectedSubject);
        intent.putExtra("REVIEW_DATA", quizReviewList);
        startActivity(intent);

        // Reset UI
        layoutSetup.setVisibility(View.VISIBLE);
        layoutGame.setVisibility(View.GONE);
        questionCount = 1;
        currentScore = 0;
        tvPoints.setText("💎 0");
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

    private void logEvent(String eventName, Bundle params) {
        if (params == null) params = new Bundle();
        mFirebaseAnalytics.logEvent(eventName, params);
        fbLogger.logEvent(eventName, params);
    }
}