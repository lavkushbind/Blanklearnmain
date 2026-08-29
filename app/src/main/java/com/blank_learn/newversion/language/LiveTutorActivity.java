package com.blank_learn.newversion.language;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.blank_learn.dark.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LiveTutorActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyCtEawO-6eZ6yv7vwb2ZPLZhFJxcH85gJM"; // Replace with your key

    // UI Components
    private LottieAnimationView aiAvatar;
    private TextView tvStatus, tvSubtitleText, tvCorrection, tvSessionInfo; // Added tvSessionInfo
    private ImageView btnMicToggle;
    private CardView cardSubtitle;

    // AI & Voice Components
    private GenerativeModelFutures model;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    // Conversation State
    private StringBuilder conversationHistory = new StringBuilder();
    private String nativeLang = "Hindi", targetLang = "en", userLevel = "Beginner";
    private boolean isListening = false;

    private String systemInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tutor);

        // 1. Get Settings from Setup Activity
        Intent intent = getIntent();
        // Null check is important, but if setup is mandatory, they should not be null
        nativeLang = intent.getStringExtra("NATIVE_LANG");
        targetLang = intent.getStringExtra("TARGET_LANG");
        userLevel = intent.getStringExtra("LEVEL");

        // Use defaults if intent data is missing
        if (nativeLang == null) nativeLang = "Hindi";
        if (targetLang == null) targetLang = "en";
        if (userLevel == null) userLevel = "Beginner";

        // 2. Init UI (CRITICAL: All findViewByIds must be correct)
        aiAvatar = findViewById(R.id.aiAvatar);
        tvStatus = findViewById(R.id.tvStatus);
        tvSubtitleText = findViewById(R.id.tvSubtitleText);
        tvCorrection = findViewById(R.id.tvCorrection);
        btnMicToggle = findViewById(R.id.btnMicToggle);
        cardSubtitle = findViewById(R.id.cardSubtitle);
        tvSessionInfo = findViewById(R.id.tvSessionInfo); // Initializing session info text view

        // Set session info at the top
        tvSessionInfo.setText("Level: " + userLevel + " • Target: " + targetLang.toUpperCase());

        // Initial UI state setup
        if (cardSubtitle != null) {
            cardSubtitle.setVisibility(View.GONE);
        }
        if (tvCorrection != null) {
            tvCorrection.setVisibility(View.GONE);
        }
        tvStatus.setText("Initializing Tutor..."); // This was the line 91 crash fix

        // 3. Init Services
        initGemini();
        initTTS();
        initSTT();

        // 4. Start Session (Load history or start new)
        startConversation();

        // 5. Listener
        btnMicToggle.setOnClickListener(v -> toggleListening());
        findViewById(R.id.btnEndCall).setOnClickListener(v -> finish());
    }

    // --- SETUP METHODS ---

    private void initGemini() {
        systemInstruction = "You are a friendly, adaptive language tutor specializing in " + targetLang + ". " +
                "The user is at the " + userLevel + " level. " +
                "User's Native Language is " + nativeLang + ". " +
                "RULES: " +
                "1. Always respond in " + targetLang + " (except for rule 2). " +
                "2. If the user's spoken sentence has a clear grammar or pronunciation error, output ONLY a single JSON object. The JSON MUST be on its own line and contain: { \"correction\": \"Correct sentence.\", \"feedback_hindi\": \"Explain the error in Hindi.\", \"corrected_speech\": \"The correct phrase for TTS.\", \"conversation_reply\": \"Short reply in target language to continue conversation.\" }. DO NOT include any plain text outside the JSON when correcting." +
                "3. If input is CORRECT, output ONLY the conversational reply text, no JSON. " +
                "4. Keep conversational replies extremely brief (max 10 words).";

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", API_KEY);
        model = GenerativeModelFutures.from(gm);
    }

    private void startConversation() {
        SharedPreferences prefs = getSharedPreferences("TutorSession", MODE_PRIVATE);
        String savedHistory = prefs.getString("CONVERSATION_HISTORY", null);

        if (savedHistory != null && savedHistory.length() > 0) {
            // Resume previous session
            conversationHistory.append(savedHistory);
            String resumePrompt = "Welcome back! Summarize our last topic and ask a follow-up question to resume.";
            askGemini(resumePrompt, false);
        } else {
            // Start new session
            conversationHistory.append("SYSTEM_INSTRUCTION: ").append(systemInstruction).append("\n"); // Add instruction now
            String initialPrompt = "Start a simple, easy conversation about the weather or asking names, keeping the complexity appropriate for the " + userLevel + " level.";
            askGemini(initialPrompt, false);
        }
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale locale = new Locale(targetLang);
                if (textToSpeech.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                    textToSpeech.setLanguage(locale);
                } else {
                    textToSpeech.setLanguage(Locale.US);
                }
                tvStatus.setText("Tutor Ready.");
            }
        });
    }

    private void initSTT() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { tvStatus.setText("Speak Now..."); if (aiAvatar != null) aiAvatar.setSpeed(2f); }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { tvStatus.setText("Thinking..."); if (aiAvatar != null) aiAvatar.setSpeed(1f); }
            @Override public void onError(int error) {
                toggleListening();
                tvStatus.setText("Ready.");
                Toast.makeText(LiveTutorActivity.this, "Voice error: Tap to speak.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    sendUserMessage(matches.get(0));
                }
                // speechRecognizer.stopListening(); // already done by toggleListening()
            }
            @Override public void onPartialResults(Bundle partialResults) { }
            @Override public void onEvent(int eventType, Bundle params) { }
        });
    }

    private void toggleListening() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            // Use R.drawable.ic_mic if you have it
            btnMicToggle.setImageResource(R.drawable.ic_flame_legendary);
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLang);

            // Check if SpeechRecognizer is ready before starting
            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                speechRecognizer.startListening(intent);
                isListening = true;
                btnMicToggle.setImageResource(R.drawable.ic_flame_legendary);
            } else {
                Toast.makeText(this, "Speech Recognition not available on this device.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- CONVERSATION CORE ---

    private void sendUserMessage(String spokenText) {
        conversationHistory.append("User: ").append(spokenText).append("\n");

        if (cardSubtitle != null) cardSubtitle.setVisibility(View.VISIBLE);
        tvSubtitleText.setText("You: " + spokenText);
        if (tvCorrection != null) tvCorrection.setVisibility(View.GONE);

        askGemini(conversationHistory.toString(), true);
    }

    private void askGemini(String fullPrompt, boolean isUserReply) {
        if (isUserReply) tvStatus.setText("Gemini is thinking...");

        Content content = new Content.Builder().addText(fullPrompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (isFinishing()) return;

                String aiResponse = result.getText();

                if (aiResponse != null && aiResponse.contains("{ \"correction\": ")) {
                    processCorrection(aiResponse);
                } else {
                    processNormalReply(aiResponse);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (isFinishing()) return;
                runOnUiThread(() -> {
                    tvStatus.setText("Error. Tap Mic to retry.");
                    Toast.makeText(LiveTutorActivity.this, "AI Connection Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }, executor);
    }

    private void processCorrection(String jsonResponse) {
        try {
            int start = jsonResponse.indexOf("{");
            int end = jsonResponse.lastIndexOf("}");
            if (start == -1 || end == -1) throw new Exception("Invalid JSON format");

            JSONObject json = new JSONObject(jsonResponse.substring(start, end + 1));

            String correctedSpeech = json.optString("corrected_speech", "Try again.");
            String feedbackHindi = json.optString("feedback_hindi", "Error detected.");
            String conversationReply = json.optString("conversation_reply", "");

            // 1. Update UI (Show Correction)
            if (tvCorrection != null) {
                tvCorrection.setText("💡 " + feedbackHindi);
                tvCorrection.setVisibility(View.VISIBLE);
            }
            tvSubtitleText.setText(targetLang + " Tutor: " + correctedSpeech);
            tvStatus.setText("Listen & Try Again!");

            // 2. Speak the Corrected Speech
            speakText(correctedSpeech);

            // 3. Update history
            conversationHistory.append("Gemini (Correction): ").append(conversationReply).append("\n");

        } catch (Exception e) {
            Log.e("Tutor", "Correction JSON Parsing Failed: " + e.getMessage());
            // Fallback: Treat the response text as a normal reply
            processNormalReply("I'm sorry, I couldn't process the correction. Let's try that again.");
        }
    }

    private void processNormalReply(String aiText) {
        if (aiText == null) aiText = "I did not understand. Can you rephrase?";

        conversationHistory.append("Gemini: ").append(aiText).append("\n");

        tvSubtitleText.setText(targetLang + " Tutor: " + aiText);
        if (tvCorrection != null) tvCorrection.setVisibility(View.GONE);
        tvStatus.setText("Ready.");

        speakText(aiText);
    }

    // --- CLEANUP ---
    private void saveSessionContext() {
        SharedPreferences prefs = getSharedPreferences("TutorSession", MODE_PRIVATE);
        prefs.edit().putString("CONVERSATION_HISTORY", conversationHistory.toString()).apply();
    }

    @Override
    protected void onDestroy() {
        saveSessionContext();
        if (textToSpeech != null) { textToSpeech.stop(); textToSpeech.shutdown(); }
        if (speechRecognizer != null) { speechRecognizer.destroy(); }
        super.onDestroy();
    }

    // --- TTS Helper ---
    private void speakText(String text) {
        if (textToSpeech != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}