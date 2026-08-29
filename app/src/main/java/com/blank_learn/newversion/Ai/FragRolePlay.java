package com.blank_learn.newversion.Ai;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FragRolePlay extends Fragment {

    // UI
    private TextView tvScenario, tvAiDialog, tvHint1, tvHint2, tvFeedback;
    private ImageView btnMic, btnReplay;

    // Logic
    private GenerativeModelFutures model;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;

    // State
    private String currentAiText = "";
    private ArrayList<String> currentHints = new ArrayList<>();
    private StringBuilder conversationHistory = new StringBuilder();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_role_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Views
        tvScenario = view.findViewById(R.id.tvScenarioTitle);
        tvAiDialog = view.findViewById(R.id.tvAiDialogue);
        tvHint1 = view.findViewById(R.id.tvHint1);
        tvHint2 = view.findViewById(R.id.tvHint2);
        tvFeedback = view.findViewById(R.id.tvUserFeedback);
        btnMic = view.findViewById(R.id.btnMic);
        btnReplay = view.findViewById(R.id.btnReplayAudio);

        initGemini();
        initTTS();
        initSTT();

        // Start the Scenario
        startNewScenario();

        // Listeners
        btnMic.setOnClickListener(v -> startListening());
        btnReplay.setOnClickListener(v -> speakText(currentAiText));

        // Click on hints to auto-speak (for practice)
        tvHint1.setOnClickListener(v -> speakText(tvHint1.getText().toString()));
    }

    private void initGemini() {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "YOUR_API_KEY");
        model = GenerativeModelFutures.from(gm);
    }

    private void startNewScenario() {
        tvScenario.setText("At the Coffee Shop");
        conversationHistory.append("System: We are roleplaying. You are a barista. I am a customer. Keep replies short.\n");

        // Initial Prompt to start conversation
        getAiResponse("Start the conversation. Return JSON: { \"ai_msg\": \"...\", \"hints\": [\"Hint1\", \"Hint2\"] }");
    }

    private void getAiResponse(String userMyInput) {
        // Show loading
        tvFeedback.setText("AI is thinking...");

        // Construct Prompt
        String prompt = conversationHistory.toString() + "User: " + userMyInput +
                "\nGenerate your reply as Barista and provide 2 possible English responses for the User (Beginner Level)." +
                "Return ONLY JSON format: { \"ai_msg\": \"Your reply here\", \"hints\": [\"Option A\", \"Option B\"] }";

        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> parseAiResponse(result.getText()));
                }
            }
            @Override public void onFailure(Throwable t) {}
        }, executor);
    }

    private void parseAiResponse(String jsonString) {
        try {
            if (jsonString.contains("```json")) jsonString = jsonString.replace("```json", "").replace("```", "");
            JSONObject json = new JSONObject(jsonString);

            currentAiText = json.getString("ai_msg");
            JSONArray hints = json.getJSONArray("hints");

            currentHints.clear();
            currentHints.add(hints.getString(0));
            currentHints.add(hints.getString(1));

            // Update UI
            tvAiDialog.setText(currentAiText);
            tvHint1.setText(currentHints.get(0));
            tvHint2.setText(currentHints.get(1));
            tvFeedback.setText("Tap Mic & Speak a Hint");

            // Update History
            conversationHistory.append("AI: ").append(currentAiText).append("\n");

            // Auto Speak AI Message
            speakText(currentAiText);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- TTS & STT Logic (Same as Level 1 but adapted) ---

    private void initTTS() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.ERROR) tts.setLanguage(Locale.US);
        });
    }

    private void speakText(String text) {
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void initSTT() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    processUserSpeech(matches.get(0));
                }
            }
            // ... other overrides ...
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() { tvFeedback.setText("Listening..."); }
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() { tvFeedback.setText("Processing..."); }
            @Override public void onError(int error) { tvFeedback.setText("Try Again"); }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        speechRecognizer.startListening(intent);
    }

    private void processUserSpeech(String spokenText) {
        // Logic: Check if user spoke clearly (Match similarity with hints or just assume validity for flow)
        // For now, we send whatever user spoke to AI to continue conversation

        tvFeedback.setText("You: " + spokenText);
        conversationHistory.append("User (Spoken): ").append(spokenText).append("\n");

        // Trigger AI for next turn
        getAiResponse(spokenText);
    }

    @Override
    public void onDestroy() {
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }
}