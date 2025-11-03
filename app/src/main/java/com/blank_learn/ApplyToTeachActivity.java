package com.blank_learn;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ApplyToTeachActivity extends AppCompatActivity {

    // UI Views
    private EditText etName, etExperience, etInstitutes, etVideoLink;
    private RadioGroup rgDevice;
    private CheckBox cbZoom, cbMeet, cbTeams;
    private Button btnSubmit, btnSelectResume;
    private TextView tvSelectedFileName;
    private ProgressBar progressBar;

    // Firebase
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    // Activity Result Launcher for file picking
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Uri pdfFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_to_teach);

        // Firebase setup
        databaseReference = FirebaseDatabase.getInstance().getReference("teacher_applications");
        storageReference = FirebaseStorage.getInstance().getReference("teacher_resumes");

        initializeUI();
        initializeListeners();
        initializeFilePicker();
    }

    private void initializeUI() {
        etName = findViewById(R.id.et_name);
        etExperience = findViewById(R.id.et_experience);
        etInstitutes = findViewById(R.id.et_institutes);
        etVideoLink = findViewById(R.id.et_video_link);
        rgDevice = findViewById(R.id.rg_device);
        cbZoom = findViewById(R.id.cb_zoom);
        cbMeet = findViewById(R.id.cb_meet);
        cbTeams = findViewById(R.id.cb_teams);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSelectResume = findViewById(R.id.btn_select_resume);
        tvSelectedFileName = findViewById(R.id.tv_selected_file_name);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initializeListeners() {
        btnSubmit.setOnClickListener(v -> validateAndSubmitForm());
        btnSelectResume.setOnClickListener(v -> openFilePicker());
    }

    private void initializeFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        pdfFileUri = result.getData().getData();
                        String fileName = getFileName(pdfFileUri);
                        tvSelectedFileName.setText(fileName);
                        tvSelectedFileName.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }


    private void validateAndSubmitForm() {
        // --- Data Collection and Validation ---
        String name = etName.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Full Name is required.");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(experience)) {
            etExperience.setError("Experience is required.");
            etExperience.requestFocus();
            return;
        }
        if (rgDevice.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a primary teaching device.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pdfFileUri == null) {
            Toast.makeText(this, "Please upload your resume.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        uploadResumeAndSubmitData();
    }

    private void uploadResumeAndSubmitData() {
        // Create a unique ID for the new teacher application
        String teacherId = databaseReference.push().getKey();
        if (teacherId == null) {
            Toast.makeText(this, "Could not create application entry. Please try again.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        StorageReference fileReference = storageReference.child(teacherId + ".pdf");

        fileReference.putFile(pdfFileUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String resumeUrl = uri.toString();
                            saveDataToDatabase(teacherId, resumeUrl);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(ApplyToTeachActivity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            setLoading(false);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(ApplyToTeachActivity.this, "Resume upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    private void saveDataToDatabase(String teacherId, String resumeUrl) {
        String name = etName.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String institutes = etInstitutes.getText().toString().trim();
        String videoLink = etVideoLink.getText().toString().trim();

        int selectedDeviceId = rgDevice.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedDeviceId);
        String device = selectedRadioButton.getText().toString();

        StringBuilder tools = new StringBuilder();
        if (cbZoom.isChecked()) tools.append("Zoom, ");
        if (cbMeet.isChecked()) tools.append("Google Meet, ");
        if (cbTeams.isChecked()) tools.append("Microsoft Teams, ");
        String familiarTools = tools.length() > 0 ? tools.substring(0, tools.length() - 2) : "None";

        TeacherData teacherData = new TeacherData(teacherId, name, experience, institutes, device, familiarTools, resumeUrl, videoLink);

        databaseReference.child(teacherId).setValue(teacherData).addOnCompleteListener(task -> {
            setLoading(false);
            if (task.isSuccessful()) {
                Toast.makeText(ApplyToTeachActivity.this, "Application submitted successfully! We will review it and get back to you.", Toast.LENGTH_LONG).show();
                clearForm();
            } else {
                Toast.makeText(ApplyToTeachActivity.this, "Database submission failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
        }
    }

    private void clearForm() {
        etName.setText("");
        etExperience.setText("");
        etInstitutes.setText("");
        etVideoLink.setText("");
        rgDevice.clearCheck();
        cbZoom.setChecked(false);
        cbMeet.setChecked(false);
        cbTeams.setChecked(false);
        pdfFileUri = null;
        tvSelectedFileName.setVisibility(View.GONE);
        tvSelectedFileName.setText("No file selected.");
        etName.requestFocus();
    }

    // Helper method to get file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}