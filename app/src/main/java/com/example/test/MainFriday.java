package com.example.test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dark.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainFriday extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String OPENAI_API_KEY = "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";

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    private EditText etQuestion;
    private TextView tvAnswer;
    private Button btnSubmit;

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_friday);

        etQuestion = findViewById(R.id.etQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);

        client = new OkHttpClient();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = etQuestion.getText().toString().trim();
                if (!question.isEmpty()) {
                    fetchAnswerFromOpenAI(question);
                } else {
                    tvAnswer.setText("Please enter a question.");
                }
            }
        });
    }

    private void fetchAnswerFromOpenAI(String question) {
        tvAnswer.setText("Fetching answer...");

        // Build the JSON request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.put(userMessage);
            jsonBody.put("messages", messages);
        } catch (JSONException e) {
            tvAnswer.setText("Error creating JSON request: " + e.getMessage());
            return;
        }

        // Create the request body
        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        // Build the request
        Request request = new Request.Builder()
                .url(OPENAI_URL)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(body)
                .build();

        // Make the API call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
                runOnUiThread(() -> tvAnswer.setText("Error: " + e.getMessage()));
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("API_ERROR", "HTTP Error: " + response.message());
                    runOnUiThread(() -> tvAnswer.setText("Error: HTTP " + response.code() + " - " + response.message()));
                    return;
                }

                // Parse the response
                String responseData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    JSONArray choices = jsonObject.getJSONArray("choices");
                    String answer = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    runOnUiThread(() -> tvAnswer.setText(answer.trim()));
                } catch (JSONException e) {
                    runOnUiThread(() -> tvAnswer.setText("Error parsing response: " + e.getMessage()));
                }
            }
        });
    }
}
