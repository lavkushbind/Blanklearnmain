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

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FragWordPractice extends Fragment {

    // UI
    private TextView tvTarget, tvNative, tvFeedback;
    private ImageView btnMic, btnSpeak;
    private View viewPulse;

    // Logic
    private GenerativeModelFutures model;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private String currentWord = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_word_practice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Views
        tvTarget = view.findViewById(R.id.tvTargetWord);
        tvNative = view.findViewById(R.id.tvNativeMeaning);
        tvFeedback = view.findViewById(R.id.tvFeedback);
        btnMic = view.findViewById(R.id.btnMic);
        btnSpeak = view.findViewById(R.id.btnAiSpeak);
        viewPulse = view.findViewById(R.id.viewPulse);

        // Init AI & Voice
        initGemini();
        initTTS();
        initSTT();

        // Load First Word
        loadNewWord();

        // Listeners
        btnSpeak.setOnClickListener(v -> speakWord());
        btnMic.setOnClickListener(v -> startListening());
    }

    private void initGemini() {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "YOUR_API_KEY");
        model = GenerativeModelFutures.from(gm);
    }

    private void loadNewWord() {
        tvTarget.setText("Loading...");
        tvNative.setText("");

        // Prompt for AI
        String prompt = "Give me 1 simple English word for a beginner and its Hindi meaning. Return ONLY JSON: { \"word\": \"Apple\", \"meaning\": \"सेब\" }";

        Content content = new Content.Builder().addText(prompt).build();
        Executor executor = Executors.newSingleThreadExecutor();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> parseWord(result.getText()));
                }
            }
            @Override public void onFailure(Throwable t) {}
        }, executor);
    }

    private void parseWord(String jsonString) {
        try {
            if (jsonString.contains("```json")) jsonString = jsonString.replace("```json", "").replace("```", "");
            JSONObject json = new JSONObject(jsonString);

            currentWord = json.getString("word");
            String meaning = json.getString("meaning");

            tvTarget.setText(currentWord);
            tvNative.setText("(" + meaning + ")");

            // Auto speak once loaded
            speakWord();

        } catch (Exception e) { loadNewWord(); } // Retry if error
    }

    // --- TTS (AI Speaking) ---
    private void initTTS() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.ERROR) tts.setLanguage(Locale.US);
        });
    }

    private void speakWord() {
        if (tts != null && !currentWord.isEmpty()) {
            tts.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // --- STT (Listening) ---
    private void initSTT() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    checkPronunciation(matches.get(0));
                }
                viewPulse.setVisibility(View.INVISIBLE);
            }
            // Other required overrides...
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) { viewPulse.setVisibility(View.INVISIBLE); }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        viewPulse.setVisibility(View.VISIBLE);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        speechRecognizer.startListening(intent);
    }

    private void checkPronunciation(String spokenText) {
        if (spokenText.equalsIgnoreCase(currentWord)) {
            // SUCCESS
            tvFeedback.setVisibility(View.VISIBLE);
            tvFeedback.setText("Correct! 🎉 Great Job.");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_light));

            // Load next word after delay
            btnMic.postDelayed(this::loadNewWord, 2000);
        } else {
            // FAIL
            tvFeedback.setVisibility(View.VISIBLE);
            tvFeedback.setText("You said: " + spokenText + ". Try again!");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }
}