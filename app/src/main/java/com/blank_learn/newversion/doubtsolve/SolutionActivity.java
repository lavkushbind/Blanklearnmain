package com.blank_learn.newversion.doubtsolve;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.blank_learn.dark.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SolutionActivity extends AppCompatActivity {

    // --- API CONFIGURATION ---
    private static final String API_KEY = "AQ.Ab8RN6JPP70orzCGhf0wESVtXkMNwJ9EBUB9k7lsohHbkmBsXA";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
     // --- UI VARIABLES ---
    private WebView webAnswer;
    private EditText etFollowUp;
    private ImageView imgThumb;
    private Bitmap croppedBitmap;

    // Correct Type (NestedScrollView)
    private NestedScrollView mainContentLayout;
    private LinearLayout loadingLayout;

    // Chat History Store
    private JSONArray conversationHistory = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution);

        // --- INIT VIEWS ---
        imgThumb = findViewById(R.id.imgQuestionThumb);
        webAnswer = findViewById(R.id.webAnswer);
        etFollowUp = findViewById(R.id.etFollowUp);

        mainContentLayout = findViewById(R.id.mainContentLayout);
        loadingLayout = findViewById(R.id.loadingLayout);

        setupAnswerWebView(webAnswer);

        // --- HANDLE IMAGE FROM INTENT ---
        String uriString = getIntent().getStringExtra("imageUri");
        if (uriString != null) {
            try {
                Uri imageUri = Uri.parse(uriString);
                croppedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgThumb.setImageBitmap(croppedBitmap);

                // --- START AUTOMATIC SOLVING ---
                // Clean Prompt: Only Text & Math, No Video instructions
                String initialPrompt = "You are an expert tutor. Solve this problem step-by-step. " +
                        "Use LaTeX for math formulas (wrap in single $ signs). " +
                        "Explain clearly like a premium textbook, keeping the tone helpful and academic.";

                askGemini(initialPrompt, true);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }

        // --- BUTTON CLICKS ---

        // Explain Concept Button
        findViewById(R.id.btnConcept).setOnClickListener(v -> {
            String prompt = "Explain the core concept, formulas, and theory used in this problem in detail.";
            askGemini(prompt, false);
        });

        // Step-by-Step Button
        findViewById(R.id.btnSolve).setOnClickListener(v -> {
            String prompt = "Provide a very detailed step-by-step calculation/derivation for this.";
            askGemini(prompt, false);
        });

        // Send Custom Chat Button
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            String query = etFollowUp.getText().toString().trim();
            if (!query.isEmpty()) {
                etFollowUp.setText(""); // Clear box immediately
                askGemini(query, false);
            }
        });
    }

    // --- LOADING LOGIC ---
    private void toggleLoading(boolean isLoading) {
        if (isLoading) {
            loadingLayout.setVisibility(View.VISIBLE);
            // Agar pehli baar hai to content chupao
            if (conversationHistory.length() == 0) {
                mainContentLayout.setVisibility(View.GONE);
            }
        } else {
            loadingLayout.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.VISIBLE);
        }
    }

    // --- MAIN AI FUNCTION ---
    private void askGemini(String prompt, boolean isInitialImage) {
        // Show Loader IMMEDIATELY
        toggleLoading(true);

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject currentUserTurn = new JSONObject();
            currentUserTurn.put("role", "user");
            JSONArray parts = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.put(textPart);

            // Send Image ONLY on first request
            if (isInitialImage && croppedBitmap != null) {
                JSONObject imagePart = new JSONObject();
                JSONObject inlineData = new JSONObject();
                inlineData.put("mime_type", "image/jpeg");
                inlineData.put("data", bitmapToBase64(croppedBitmap));
                imagePart.put("inline_data", inlineData);
                parts.put(imagePart);
            }

            currentUserTurn.put("parts", parts);

            // Add to history BEFORE sending, so context is maintained
            conversationHistory.put(currentUserTurn);

            // Send full history
            jsonBody.put("contents", conversationHistory);

        } catch (Exception e) { e.printStackTrace(); }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder().url(GEMINI_URL).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    toggleLoading(false);
                    Toast.makeText(SolutionActivity.this, "Connection Failed. Check Internet.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);

                        if (!jsonResponse.has("candidates")) {
                            throw new Exception("No candidates");
                        }

                        JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                        JSONObject content = candidate.getJSONObject("content");
                        String aiText = content.getJSONArray("parts").getJSONObject(0).getString("text");

                        // Save AI response to history
                        conversationHistory.put(content);

                        final String finalAnswer = aiText;

                        runOnUiThread(() -> {
                            // STOP Loader Only when answer is ready
                            toggleLoading(false);
                            renderMathAnswer(finalAnswer);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            toggleLoading(false);
                            Toast.makeText(SolutionActivity.this, "Error parsing AI response", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        toggleLoading(false);
                        Toast.makeText(SolutionActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // --- RENDER TEXT AS PREMIUM BOOK STYLE ---
    private void setupAnswerWebView(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(0x00000000); // Transparent
        webView.setWebViewClient(new WebViewClient());
    }

    private void renderMathAnswer(String rawMarkdown) {
        String cleanText = rawMarkdown.replace("```html", "").replace("```", "");

        String htmlContent = "<!DOCTYPE html><html><head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<script src=\"https://polyfill.io/v3/polyfill.min.js?features=es6\"></script>" +
                "<script id=\"MathJax-script\" async src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>" +
                "<script src=\"https://cdn.jsdelivr.net/npm/marked/marked.min.js\"></script>" +
                "<style>" +
                "body { font-family: 'Georgia', serif; color: #E0E0E0; font-size: 16px; line-height: 1.6; padding: 10px; }" +
                "h1, h2, h3 { color: #81D4FA; font-family: sans-serif; margin-top: 20px; border-bottom: 1px solid #444; }" +
                "strong { color: #FFD54F; }" +
                "p { margin-bottom: 12px; }" +
                ".math-display { overflow-x: auto; background: rgba(255,255,255,0.05); padding: 8px; border-radius: 6px; }" +
                "</style>" +
                "</head><body>" +
                "<div id=\"content\"></div>" +
                "<script>" +
                "  var rawText = `" + cleanText.replace("`", "\\`").replace("$", "$") + "`;" +
                "  document.getElementById('content').innerHTML = marked.parse(rawText);" +
                "  MathJax.typesetPromise();" +
                "</script>" +
                "</body></html>";

        webAnswer.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null);
    }

    // --- HELPER: IMAGE TO BASE64 ---
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    public void onBackPressed(View view) {
        super.onBackPressed();
    }
}