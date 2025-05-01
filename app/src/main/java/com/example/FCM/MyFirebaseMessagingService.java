package com.example.FCM;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.chat.ChatAA;
import com.example.dark.R;
import com.example.home.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import io.reactivex.rxjava3.annotations.Nullable;

public class MyFirebaseMessagingService extends FirebaseMessagingService { // Renamed example

    private static final String TAG = "MyFCMService"; // Updated TAG

    // --- Channel IDs must match Cloud Functions ---
    public static final String DEMO_CHANNEL_ID = "TeacherNewDemoChannel";
    public static final String DEMO_CHANNEL_NAME = "New Demo Bookings";
    public static final String CHAT_CHANNEL_ID = "ChatMessagesChannel"; // New channel ID
    public static final String CHAT_CHANNEL_NAME = "Chat Messages"; // New channel name

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM Message Received!");
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // --- Get Data Payload ---
        Map<String, String> dataPayload = remoteMessage.getData();
        Log.d(TAG, "Message data payload: " + dataPayload);

        if (dataPayload.isEmpty()) {
            Log.w(TAG, "Received FCM message without data payload. Cannot determine type.");
            // Optionally handle messages that *only* have a 'notification' part, but it's less reliable
            // If remoteMessage.getNotification() != null -> display generic notification?
            return;
        }

        // --- Determine Notification Type based on 'notificationAction' in data ---
        String action = dataPayload.get("notificationAction");
        if (action == null) {
            Log.e(TAG, "Missing 'notificationAction' in data payload. Cannot process message.");
            return;
        }

        Log.d(TAG, "Notification Action: " + action);

        // --- Get Title and Body (Prefer data payload, fallback to notification part) ---
        String title = dataPayload.get("title"); // Might be sender name for chat
        String body = dataPayload.get("body");

        if (remoteMessage.getNotification() != null) {
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (body == null) body = remoteMessage.getNotification().getBody();
        }

        // Provide defaults if still null (shouldn't happen if functions are correct)
        if (title == null) title = getString(R.string.app_name); // Use app name as default title
        if (body == null) body = "You have a new notification.";


        // --- Route to appropriate handler ---
        switch (action) {
            case "OPEN_TEACHER_DEMO_DETAILS":
                handleDemoNotification(title, body, dataPayload);
                break;
            case "OPEN_PERSONAL_CHAT":
                handleChatNotification(title, body, dataPayload);
                break;
            default:
                Log.w(TAG, "Unknown notificationAction received: " + action);
                // Handle as a generic notification or ignore
                handleGenericNotification(title, body, dataPayload);
                break;
        }
    }

    // --- Handler for Demo Notifications ---
    private void handleDemoNotification(String title, String body, Map<String, String> data) {
        Log.d(TAG, "Handling Demo Notification");

        // Intent to launch TeacherMainActivity (or specific demo details activity)
        Intent intent = new Intent(this, MainActivity.class); // <<< Adjust target if needed
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Add extras for demo
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        int requestCode = generateRequestCode(data, "demoId"); // Use demoId for request code

        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification using the DEMO channel
        NotificationCompat.Builder notificationBuilder =
                createNotificationBuilder(title, body, DEMO_CHANNEL_ID, pendingIntent)
                        // --- Use your specific demo icon ---
                        .setSmallIcon(R.drawable.logofix); // <<< CHANGE Demo Icon

        // Use demoId hash as notification ID for potential updates
        int notificationId = requestCode;
        showNotification(notificationId, null, notificationBuilder); // No tag for demos? Or use teacherId?
    }

    // --- Handler for Chat Notifications ---
    private void handleChatNotification(String title, String body, Map<String, String> data) {
        Log.d(TAG, "Handling Chat Notification");

        String chatId = data.get("chatId");
        String senderId = data.get("senderId"); // ID of the person who sent the message
        String senderName = data.get("senderName"); // Name of the person who sent the message

        if (chatId == null || senderId == null) {
            Log.e(TAG, "Chat notification missing chatId or senderId in data.");
            return;
        }

        // Intent to launch ChatAA activity
        Intent intent = new Intent(this, ChatAA.class); // <<< Target ChatAA
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // NEW_TASK might be needed if launching from service background

        // --- IMPORTANT: Add extras needed by ChatAA ---
        // ChatAA seems to need 'name' which corresponds to the OTHER user's ID (recipient in this context)
        // The Cloud function currently sends senderId and recipientId. Let's pass senderId.
        // ChatAA needs modification to handle being opened from notification.
        intent.putExtra("Postid", chatId); // Assuming Postid is the chatId
        intent.putExtra("name", senderId); // Pass the SENDER's ID. ChatAA needs to load based on this + current user.
        // Add other data if needed by ChatAA
        intent.putExtra("senderName", senderName); // Pass senderName too


        int requestCode = generateRequestCode(data, "chatId"); // Use chatId for request code

        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification using the CHAT channel
        NotificationCompat.Builder notificationBuilder =
                createNotificationBuilder(title, body, CHAT_CHANNEL_ID, pendingIntent)
                        // --- Use your specific chat icon ---
                        .setSmallIcon(R.drawable.logofix); // <<< CHANGE Chat Icon

        // Use chatId as the TAG for grouping/stacking notifications
        String notificationTag = chatId;
        // Use a consistent ID for notifications within the same chat (e.g., 0 or senderId hash?)
        int notificationId = 0; // Use 0 to allow Android's stacking with tag, or use senderId hash

        // Add MessagingStyle for richer chat notifications (Optional but nice)
        // Person currentUser = new Person.Builder().setName("You").setKey(FirebaseAuth.getInstance().getCurrentUser().getUid()).build(); // Recipient (the current user)
        // Person senderPerson = new Person.Builder().setName(senderName).setKey(senderId).build(); // Sender
        // NotificationCompat.MessagingStyle messagingStyle = new NotificationCompat.MessagingStyle(currentUser)
        //         .setConversationTitle(senderName) // Or group chat title
        //         .addMessage(body, System.currentTimeMillis(), senderPerson); // Add the new message
        // notificationBuilder.setStyle(messagingStyle);


        showNotification(notificationId, notificationTag, notificationBuilder);
    }

    // --- Handler for Generic/Unknown Notifications ---
    private void handleGenericNotification(String title, String body, Map<String, String> data){
        Log.d(TAG, "Handling Generic Notification");
        // Intent to launch main activity
        Intent intent = new Intent(this, MainActivity.class); // <<< Adjust target if needed
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        int requestCode = generateRequestCode(data, "generic");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Use a default channel or the demo channel? Decide based on your needs.
        NotificationCompat.Builder notificationBuilder =
                createNotificationBuilder(title, body, DEMO_CHANNEL_ID, pendingIntent)
                        .setSmallIcon(R.drawable.logofix); // <<< Default icon

        showNotification(requestCode, null, notificationBuilder); // Use request code as ID, no tag
    }


    // --- Helper to create Notification Builder ---
    private NotificationCompat.Builder createNotificationBuilder(String title, String body, String channelId, PendingIntent pendingIntent) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
    }

    // --- Helper to show Notification (Handles Channel Creation) ---
    private void showNotification(int notificationId, @Nullable String tag, NotificationCompat.Builder builder) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager is null. Cannot show notification.");
            return;
        }

        // --- Create Notification Channels (Android O+) ---
        // It's safe to call createNotificationChannel repeatedly.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Demo Channel
            NotificationChannel demoChannel = new NotificationChannel(DEMO_CHANNEL_ID,
                    DEMO_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            // demoChannel.setDescription("Notifications for new demo class bookings");
            notificationManager.createNotificationChannel(demoChannel);

            // Chat Channel
            NotificationChannel chatChannel = new NotificationChannel(CHAT_CHANNEL_ID,
                    CHAT_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH); // Or IMPORTANCE_DEFAULT?
            // chatChannel.setDescription("Notifications for new chat messages");
            notificationManager.createNotificationChannel(chatChannel);

            Log.d(TAG, "Notification Channels created/ensured.");
        }

        // --- Show the notification ---
        if (tag != null) {
            // Use tag for grouping (e.g., chat notifications)
            notificationManager.notify(tag, notificationId, builder.build());
            Log.d(TAG, "Notification sent with TAG: " + tag + " and ID: " + notificationId);
        } else {
            // Use only ID (e.g., demo notifications)
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification sent with ID: " + notificationId);
        }
    }


    // --- Helper to generate a unique request code for PendingIntent ---
    private int generateRequestCode(Map<String, String> data, String keyHint) {
        String uniqueKey = data.get(keyHint); // Try using hint first (chatId, demoId)
        if (uniqueKey == null) {
            // Fallback if hint key not present
            uniqueKey = data.toString(); // Use string representation of all data
        }
        // Use hashcode for potentially stable code, fallback to timestamp if needed
        return !TextUtils.isEmpty(uniqueKey) ? uniqueKey.hashCode() : (int) System.currentTimeMillis();
    }


    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Your existing logic to update token in /Users/{userId}/fcmToken
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && token != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "Updating FCM token for user: " + userId);
            DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users") // <<< Ensure this path is correct
                    .child(userId)
                    .child("fcmToken");
            tokenRef.setValue(token)
                    .addOnSuccessListener(aVoid -> Log.i(TAG, "FCM Token successfully updated in DB."))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM token in DB", e));
        } else {
            Log.w(TAG, "Cannot update FCM token: User not logged in or token is null.");
        }
    }
}