package com.example.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dark.databinding.ActivityGroupChatBinding;
import com.example.home.MainActivity;
import com.example.loginandsignup.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatAA extends AppCompatActivity {
    private final String CHANNEL_ID = "message_channel";
    private final int NOTIFICATION_ID = 1;
    private int lastMessageCount = 0;
    ActivityGroupChatBinding
     binding;
    FirebaseAuth auth;
    ArrayList<chatmodel> list;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String name;
    Intent intent;

    String Postid;
    private chatAdapter chatAdapter;

    private static final int REQUEST_IMAGE_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();

        name = intent.getStringExtra("name");


        list = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        Postid = intent.getStringExtra("Postid");
        chatAdapter = new chatAdapter(list, getApplicationContext());
        database.getReference().child("Users").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    binding.receiversName.setText(user.getName());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.messageAdapter.setLayoutManager(layoutManager);
        binding.messageAdapter.setAdapter(chatAdapter);

        // Load group messages from Firebase
        loadGroupMessages();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatAA.this, MainActivity.class));
            }
        });

        binding.sendimgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void scrollToBottom() {
        if (binding.messageAdapter.getAdapter() != null) {
            int itemCount = binding.messageAdapter.getAdapter().getItemCount();
            if (itemCount > 0) {
                binding.messageAdapter.smoothScrollToPosition(itemCount - 1);
            }
        }
    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // Set MIME type to all file types
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple file selection
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }


    private void loadGroupMessages() {
        String senderId = auth.getUid();
        String chatId;
        String userid1 = auth.getUid();
        intent = getIntent();
        String userid2;
        auth = FirebaseAuth.getInstance();
        userid2 = intent.getStringExtra("name");
        if (userid1.compareTo(userid2) < 0) {
            chatId = userid1 + "_" + userid2;
        } else {
            chatId = userid2 + "_" + userid1;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult();
                    String userId = FirebaseAuth.getInstance().getUid();

                    if (userId != null) {
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(userId)
                                .child("fcmToken")
                                .setValue(token);
                    }
                });

        database.getReference().child("Personal_chat").child(chatId)
                .child("mess")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            chatmodel model = snapshot1.getValue(chatmodel.class);
                            list.add(model);
                        }
                        updateChatAdapter();
                        scrollToBottom();

                        if (list.size() > lastMessageCount) {
                            lastMessageCount = list.size();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatAA.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateChatAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatAdapter.notifyDataSetChanged();
            }
        });
    }

    private void sendMessage() {
        String message = binding.edtMessage.getText().toString().trim();
        Date date = new Date();
        String senderId = auth.getUid();
        String chatId;
        String userid1=  auth.getUid();
        intent = getIntent();
        String userid2;
        auth= FirebaseAuth.getInstance();
        userid2 = intent.getStringExtra("name");
        if (userid1.compareTo(userid2) < 0) {
            chatId = userid1 + "_" + userid2;
        } else {
            chatId = userid2 + "_" + userid1;
        }

        if (!message.isEmpty()) {
            chatmodel messages = new chatmodel(senderId, message, date.getTime());
            database.getReference().child("Personal_chat").child(chatId)
                    .child("mess").push()
                    .setValue(messages)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            sendNotificationToUser(userid2, "New Message", message);

                        }

                      
                    });
        } else {
            Toast.makeText(ChatAA.this, "Type a message", Toast.LENGTH_SHORT).show();
        }

        // Clear the message input field
        binding.edtMessage.setText("");
    }

    private void sendNotificationToUser(String recipientUserId, String title, String message) {
        // Retrieve the recipient's OneSignal Player ID from Firebase
        FirebaseDatabase.getInstance().getReference("Users")
                .child(recipientUserId)
                .child("oneSignalPlayerId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String recipientPlayerId = dataSnapshot.getValue(String.class);

                        if (recipientPlayerId != null) {
                            // OneSignal API URL
                            String oneSignalApiUrl = "https://onesignal.com/api/v1/notifications";

                            // OneSignal App ID
                            String oneSignalAppId = "YOUR_ONESIGNAL_APP_ID";

                            // Create the JSON payload for the OneSignal API
                            JSONObject notificationContent = new JSONObject();
                            try {
                                notificationContent.put("app_id", oneSignalAppId);
                                notificationContent.put("include_player_ids", new JSONArray().put(recipientPlayerId));
                                notificationContent.put("contents", new JSONObject().put("en", message));
                                notificationContent.put("headings", new JSONObject().put("en", title));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e("ChatAA", "Error creating JSON payload: " + e.getMessage());
                                return;
                            }

                            // Create a Volley request queue
                            RequestQueue requestQueue = Volley.newRequestQueue(ChatAA.this);

                            // Create a JSON object request
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, oneSignalApiUrl, notificationContent,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Log.d("ChatAA", "Notification sent successfully: " + response.toString());
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("ChatAA", "Error sending notification: " + error.getMessage());
                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    // Add headers for OneSignal API
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Authorization", "Basic YOUR_ONESIGNAL_REST_API_KEY");
                                    headers.put("Content-Type", "application/json; charset=utf-8");
                                    return headers;
                                }
                            };

                            // Add the request to the queue
                            requestQueue.add(jsonObjectRequest);
                        } else {
                            Log.e("ChatAA", "Recipient has no OneSignal Player ID");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("ChatAA", "Error retrieving OneSignal Player ID: " + databaseError.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    uploadImageToStorage(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                uploadImageToStorage(imageUri);
            }
        }

    }

    private void uploadImageToStorage(Uri imageUri) {

        StorageReference storageRef = storage.getReference().child("images");
        StorageReference imageRef = storageRef.child("image_" + System.currentTimeMillis());
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {



                                Date date = new Date();
                                String senderId = auth.getUid();
                                String chatId;
                                String userid1=  auth.getUid();
                                intent = getIntent();
                                String userid2;
                                auth= FirebaseAuth.getInstance();
                                userid2 = intent.getStringExtra("name");
                                if (userid1.compareTo(userid2) < 0) {
                                    chatId = userid1 + "_" + userid2;
                                } else {
                                    chatId = userid2 + "_" + userid1;
                                }



                                String imageUrl = uri.toString();
//                                final String senderId = auth.getUid();
//                                Date date = new Date();
                                chatmodel message = new chatmodel(senderId, imageUrl, date.getTime());
                                database.getReference().child("Personal_chat").child(chatId)
                                        .child("mess").push()
                                        .setValue(message)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatAA.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}