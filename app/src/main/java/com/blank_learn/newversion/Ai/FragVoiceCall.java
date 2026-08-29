package com.blank_learn.newversion.Ai;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.blank_learn.dark.R;
import com.airbnb.lottie.LottieAnimationView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FragVoiceCall extends Fragment {

    // UI
    private TextView tvStatus, tvCorrection;
    private CardView cardCorrection;
    private LottieAnimationView aiOrb;
    private ImageView btnEndCall;

    // Logic
    private GenerativeModelFutures model;
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private StringBuilder conversationHistory = new StringBuilder();
    private boolean isCallActive = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_voice_call, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatus = view.findViewById(R.id.tvStatus);
        tvCorrection = view.findViewById(R.id.tvCorrectionText);
        cardCorrection = view.findViewById(R.id.cardCorrection);
        aiOrb = view.findViewById(R.id.aiOrb);
        btnEndCall = view.findViewById(R.id.btnEndCall);

        initGemini();
        initTTS();
        initSTT();

        // Start the call loop
        startCallConversation();

        btnEndCall.setOnClickListener(v -> endCall());
    }

    private void initGemini() {
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", "YOUR_API_KEY");
        model = GenerativeModelFutures.from(gm);
    }

    private void startCallConversation() {
        // System Prompt for Professional Mode
        conversationHistory.append("System: Act as an advanced English tutor. We are having a casual conversation. " +
                "Keep your responses conversational and short (1-2 sentences max). " +
                "If I make a grammar mistake, correct me immediately using JSON. " +
                "Format: { \"reply\": \"...\", \"correction\": \"null (if correct) OR the correction\" } \n");

        // AI initiates conversation
        getAiResponse("Start the conversation by asking me about my day.");
    }

    private void getAiResponse(String input) {
        if (!isCallActive) return;

        requireActivity().runOnUiThread(() -> {
            tvStatus.setText("AI is thinking...");
            aiOrb.setSpeed(2.0f); // Fast thinking animation
        });

        String prompt = conversationHistory.toString() + "User: " + input + "\nReturn JSON only.";

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

            String reply = json.getString("reply");
            String correction = json.optString("correction", "null");

            conversationHistory.append("AI: ").append(reply).append("\n");

            // Show correction if needed
            if (!correction.equals("null") && !correction.isEmpty()) {
                cardCorrection.setVisibility(View.VISIBLE);
                tvCorrection.setText(correction);
                // Hide correction after 5 seconds
                cardCorrection.postDelayed(() -> cardCorrection.setVisibility(View.INVISIBLE), 5000);
            }

            // Speak the reply (This triggers the loop)
            speakText(reply);

        } catch (Exception e) {
            // Fallback if JSON fails
            speakText("Could you repeat that? I didn't catch it.");
        }
    }

    // --- TTS with Auto-Listen ---
    private void initTTS() {
        tts = new TextToSpeech(getContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
                // Set Listener to know when speaking finishes
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        requireActivity().runOnUiThread(() -> {
                            tvStatus.setText("Speaking...");
                            aiOrb.playAnimation();
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        // AI finished speaking -> Start Listening
                        if (isCallActive) {
                            requireActivity().runOnUiThread(() -> startListening());
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {}
                });
            }
        });
    }

    private void speakText(String text) {
        if (tts != null) {
//            HashMap<String, String> params = new HashMap<>();
//            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "AI_REPLY");
//            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "AI_REPLY");
        }
    }

    // --- STT (Hands Free) ---
    private void initSTT() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String userSpeech = matches.get(0);
                    conversationHistory.append("User: ").append(userSpeech).append("\n");
                    getAiResponse(userSpeech); // Send to AI
                }
            }

            @Override
            public void onReadyForSpeech(Bundle params) {
                tvStatus.setText("Listening... (Speak now)");
                aiOrb.pauseAnimation(); // Static or slow pulse when listening
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() { tvStatus.setText("Processing..."); }
            @Override public void onError(int error) {
                // If error (silence), retry listening after 1 sec
                if(isCallActive) {
                    tvStatus.setText("I didn't hear you...");
                    aiOrb.postDelayed(() -> startListening(), 1000);
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startListening() {
        if (!isCallActive) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        // Important for hands-free
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);

        requireActivity().runOnUiThread(() -> speechRecognizer.startListening(intent));
    }

    private void endCall() {
        isCallActive = false;
        if (tts != null) tts.stop();
        if (speechRecognizer != null) speechRecognizer.stopListening();
        // Navigate back or close fragment
        if(getActivity() != null) getActivity().onBackPressed();
    }

    @Override
    public void onDestroy() {
        endCall();
        if (tts != null) tts.shutdown();
        if (speechRecognizer != null) speechRecognizer.destroy();
        super.onDestroy();
    }
}