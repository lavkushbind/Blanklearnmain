package com.example.Feed;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.blank_learn.dark.R;
import com.blank_learn.dark.databinding.ActivityFeedPostBinding;
import com.example.home.Story_model;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Feed_Post_Activity extends AppCompatActivity {
    ActivityFeedPostBinding binding;
    FirebaseAuth auth;
    Uri uri;

    FirebaseDatabase database;
    ProgressDialog dialog;
    FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth= FirebaseAuth.getInstance();
        //postid=auth.getTenantId();
        database= FirebaseDatabase.getInstance();
        storage= FirebaseStorage.getInstance();
        binding.feedPText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String language = binding.feedPText.getText().toString();
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.feedImgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,10);
            }
        });



        binding.textView112.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (uri == null) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setMessage("Select image")
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                }
//                            });
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                    return;
//                }


            }
        });



    }

    @Override
    public void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);


         if (requestCode == 52) {
            if (data.getData() != null) {
                Uri uri = data.getData();

                final StorageReference storageReference = storage.getReference().child("Story_videos").child(FirebaseAuth.getInstance().getUid());



                storageReference.putFile(uri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageReference.getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri downloadUri) {
                                                // Generate a unique storyid
                                                String storyId = database.getReference().push().getKey();

                                                // Create a Story_model object
                                                Story_model storyModel = new Story_model(FirebaseAuth.getInstance().getUid(), "", downloadUri.toString(), storyId);

                                                DatabaseReference databaseReference = database.getReference().child("stories").child(storyId);
                                                databaseReference.setValue(storyModel);


                                            }
                                        });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                // Update the ProgressDialog with the upload progress
                                float percent = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
//                                progressDialog.setProgress((int) percent);
                            }
                        });
            }
        }

    }
}