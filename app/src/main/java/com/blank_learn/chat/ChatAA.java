package com.blank_learn.chat;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blank_learn.dark.databinding.ActivityGroupChatBinding;
import com.blank_learn.loginandsignup.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;






public class ChatAA extends AppCompatActivity {



    private static final String TAG = "ChatAA"; // Add Log TAG

    ActivityGroupChatBinding binding;
    FirebaseAuth auth;
    ArrayList<chatmodel> list;
    FirebaseDatabase database;
    FirebaseStorage storage;

    // Variables to store chat partner info
    String chatPartnerId;
    String chatPartnerName;
    String currentUserId;
    String calculatedChatId; // Store the generated chat ID

    private chatAdapter chatAdapter;

    private static final int REQUEST_IMAGE_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        list = new ArrayList<>();

        currentUserId = auth.getUid();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in!");
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        handleIntent(getIntent());


        if (chatPartnerId != null) {
            setupChatUI();
            loadUserInfo(chatPartnerId);
            loadGroupMessages();
            updateFcmTokenIfNeeded();
        } else {
            Log.e(TAG, "chatPartnerId is null after handleIntent. Cannot proceed.");
            Toast.makeText(this, "Error opening chat.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Handle potential relaunch when activity is already open
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called");
        setIntent(intent); // Update the activity's intent
        handleIntent(intent); // Re-process intent extras
        // Optional: Reload messages if chatId changed (shouldn't in this model)
        // if (calculatedChatId != null) loadGroupMessages();
    }

    // --- Centralized method to process Intent Extras ---
    private void handleIntent(Intent intent) {
        Log.d(TAG, "Handling Intent");
        if (intent == null) {
            Log.e(TAG, "Intent is null in handleIntent");
            return;
        }

        // Check how ChatAA was opened
        // Option 1: Opened directly, passing partner's ID as "name" (original way)
        String partnerIdFromName = intent.getStringExtra("name");

        // Option 2: Opened from notification, passing "senderId" (who sent the message)
        String senderIdFromNotification = intent.getStringExtra("senderId");

        // Option 3: Opened from notification, passing "chatId" directly (if function adds it)
        String chatIdFromNotification = intent.getStringExtra("chatId"); // Less likely based on current function


        if (senderIdFromNotification != null) {
            // Opened from notification: 'senderId' is the chat partner
            Log.d(TAG, "Opened from Notification: Partner ID (senderId) = " + senderIdFromNotification);
            chatPartnerId = senderIdFromNotification;
            // You might also get senderName from intent extra directly
            chatPartnerName = intent.getStringExtra("senderName");

        } else if (partnerIdFromName != null) {
            // Opened directly: 'name' contains the partner ID
            Log.d(TAG, "Opened Directly: Partner ID (name) = " + partnerIdFromName);
            chatPartnerId = partnerIdFromName;
            // Need to fetch partner name later in loadUserInfo
        } else {
            Log.e(TAG, "Could not determine chat partner ID from intent extras.");
            // Handle error - maybe finish activity
            Toast.makeText(this, "Error: Cannot identify chat partner.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // --- Calculate chatId consistently ---
        if (chatPartnerId != null && currentUserId != null) {
            if (currentUserId.compareTo(chatPartnerId) < 0) {
                calculatedChatId = currentUserId + "_" + chatPartnerId;
            } else {
                calculatedChatId = chatPartnerId + "_" + currentUserId;
            }
            Log.d(TAG, "Calculated Chat ID: " + calculatedChatId);
        } else {
            Log.e(TAG, "Cannot calculate chatId: currentUserId or chatPartnerId is null.");
            // This should ideally not happen if checks above are correct
            finish();
        }
    }


    private void setupChatUI() {
        chatAdapter = new chatAdapter(list, getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true); // Start scrolled at the bottom
        binding.messageAdapter.setLayoutManager(layoutManager);
        binding.messageAdapter.setAdapter(chatAdapter);

        binding.backBtn.setOnClickListener(view -> {
            // Consider finishing instead of starting MainActivity if it clears stack
            // startActivity(new Intent(ChatAA.this, MainActivity.class));
            finish();
        });

        binding.sendimgBtn.setOnClickListener(view -> openImagePicker());

        binding.sendBtn.setOnClickListener(v -> sendMessage());

        // Add listener to scroll to bottom when keyboard appears/disappears or content size changes
        binding.messageAdapter.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) { // Keyboard likely shown or content added
                scrollToBottom();
            }
        });
        binding.messageAdapter.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
            }
        });
    }


    // Load user info for the chat partner
    private void loadUserInfo(String userId) {
        Log.d(TAG, "Loading user info for: " + userId);
        database.getReference().child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Users user = snapshot.getValue(Users.class);
                    if (user != null) {
                        chatPartnerName = user.getName(); // Update partner name
                        binding.receiversName.setText(chatPartnerName != null ? chatPartnerName : "Chat User"); // Set name in toolbar
                        Log.d(TAG, "Partner Name: " + chatPartnerName);
                    } else {
                        Log.w(TAG, "User data structure invalid for ID: " + userId);
                        binding.receiversName.setText("Chat User");
                    }
                } else {
                    Log.w(TAG, "User not found in DB: " + userId);
                    binding.receiversName.setText("Chat User"); // Fallback name
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user info: " + error.getMessage());
                binding.receiversName.setText("Chat User"); // Fallback name
            }
        });
    }

    private void updateFcmTokenIfNeeded() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d(TAG, "Current FCM Token: " + token);
                    // Update in DB (could be done more globally)
                    if (currentUserId != null && token != null) {
                        database.getReference("Users")
                                .child(currentUserId)
                                .child("fcmToken")
                                .setValue(token)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated in DB (if needed)."))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update token in DB", e));
                    }
                });
    }


    private void loadGroupMessages() {
        if (calculatedChatId == null) {
            Log.e(TAG, "Cannot load messages, chatId is null.");
            return;
        }
        Log.d(TAG, "Loading messages for chatId: " + calculatedChatId);

        DatabaseReference messagesRef = database.getReference().child("Personal_chat").child(calculatedChatId).child("mess");

        messagesRef.addValueEventListener(new ValueEventListener() { // Use addValueEventListener for real-time updates
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int previousSize = list.size(); // Store previous size
                list.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    chatmodel model = snapshot1.getValue(chatmodel.class);
                    if (model != null) { // Add null check
                        list.add(model);
                    } else {
                        Log.w(TAG, "Null chatmodel encountered for key: " + snapshot1.getKey());
                    }
                }
                Log.d(TAG, "Messages loaded: " + list.size());
                updateChatAdapter(); // Update adapter on UI thread

                // Scroll to bottom only if new messages were added at the end
                if (list.size() > previousSize) {
                    scrollToBottom();
                }
                // Remove local notification logic based on lastMessageCount
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
                Toast.makeText(ChatAA.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChatAdapter() {
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
    }

    private void scrollToBottom() {
        if (binding.messageAdapter.getAdapter() != null && binding.messageAdapter.getAdapter().getItemCount() > 0) {
            // Use smooth scroll for better UX
            binding.messageAdapter.smoothScrollToPosition(binding.messageAdapter.getAdapter().getItemCount() - 1);
            // Or use immediate scroll if smooth scroll causes issues:
            // binding.messageAdapter.scrollToPosition(binding.messageAdapter.getAdapter().getItemCount() - 1);
        }
    }

    private void sendMessage() {
        String message = binding.edtMessage.getText().toString().trim();
        if (calculatedChatId == null || currentUserId == null) {
            Log.e(TAG, "Cannot send message: chatId or senderId is null.");
            Toast.makeText(this, "Error sending message.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!message.isEmpty()) {
            binding.edtMessage.setText(""); // Clear input field immediately
            Date date = new Date();
            chatmodel messageModel = new chatmodel(currentUserId, message, date.getTime());

            DatabaseReference messageRef = database.getReference().child("Personal_chat").child(calculatedChatId)
                    .child("mess").push(); // Get ref before setting value

            messageRef.setValue(messageModel)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Message sent successfully: " + messageRef.getKey());
                        // Message will appear via ValueEventListener, no need to add locally
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to send message: " + e.getMessage());
                        Toast.makeText(ChatAA.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                        // Optional: Add message back to input field on failure?
                        // binding.edtMessage.setText(message);
                    });
        } else {
            Toast.makeText(ChatAA.this, "Type a message", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*"); // Only allow images if it's an image button
        // intent.setType("*/*"); // Use this for any file type
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Change to false if sending one image at a time is better UX
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//Intent intent;
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Log.d(TAG, "Image selected for upload.");
            if (data.getClipData() != null ) {
                // Multiple images selected (if EXTRA_ALLOW_MULTIPLE was true)
                int count = data.getClipData().getItemCount();
                Log.d(TAG, count + " images selected.");
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    Log.d(TAG, "Uploading image URI: " + imageUri.toString());
                    uploadImageToStorage(imageUri);
                }
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                Log.d(TAG, "Uploading single image URI: " + imageUri.toString());
                uploadImageToStorage(imageUri);
            } else {
                Log.w(TAG, "Image selection result data is malformed.");
            }
        } else {
            Log.d(TAG, "Image selection cancelled or failed. ResultCode: " + resultCode);
        }
    }

    private void uploadImageToStorage(Uri imageUri) {
        if (calculatedChatId == null || currentUserId == null) {
            Log.e(TAG, "Cannot upload image: chatId or senderId is null.");
            Toast.makeText(this, "Error uploading image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress indicator?
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

        // Create a unique file name
        String fileName = "image_" + System.currentTimeMillis() + "_" + currentUserId;
        // Store images specific to chat? e.g., "chat_images/{chatId}/..."
        StorageReference imageRef = storage.getReference().child("chat_images").child(calculatedChatId).child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image uploaded successfully to Storage.");
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        Log.d(TAG, "Image Download URL: " + imageUrl);
                        // Send the URL as a message
                        sendImageUrlMessage(imageUrl);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL: " + e.getMessage());
                        Toast.makeText(ChatAA.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload image to Storage: " + e.getMessage());
                    Toast.makeText(ChatAA.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    // Optional: Update progress UI
                    // double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    // Log.d(TAG, "Upload Progress: " + progress + "%");
                });
    }

    // Helper method to send the image URL as a message
    private void sendImageUrlMessage(String imageUrl) {
        if (calculatedChatId == null || currentUserId == null) {
            Log.e(TAG, "Cannot send image URL message: chatId or senderId is null.");
            return;
        }
        Date date = new Date();
        // Message content IS the image URL
        chatmodel messageModel = new chatmodel(currentUserId, imageUrl, date.getTime());
        DatabaseReference messageRef = database.getReference().child("Personal_chat").child(calculatedChatId)
                .child("mess").push();
        messageRef.setValue(messageModel)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image URL message sent successfully: " + messageRef.getKey()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send image URL message: " + e.getMessage()));

    }

}