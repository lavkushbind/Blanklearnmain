package com.blank_learn.session;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;

public class JoinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        EditText etMeetingCode1 = findViewById(R.id.mame);

        EditText etMeetingCode = findViewById(R.id.et_meeting_code);
        Button btnJoin = findViewById(R.id.btn_join);

        btnJoin.setOnClickListener(v -> {
            String meetingCode1 = etMeetingCode.getText().toString().trim();
            String meetingCode = etMeetingCode.getText().toString().trim();

            if (!meetingCode.isEmpty()) {
                Intent intent = new Intent(JoinActivity.this, StudentSessionActivity.class);
                intent.putExtra("MEETING_CODE", meetingCode);
                intent.putExtra("USER_NAME", meetingCode1);

                startActivity(intent);
            }
        });
    }
}