package com.example.FCM;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.dark.R;
import com.example.home.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "chat_messages_channel"; // Choose a unique channel ID

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // Handle data payload (you can use this to customize notification behavior)
            // Example: Extract title and body from data if not using 'notification' payload
            String title = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                title = remoteMessage.getData().getOrDefault("title", "New Message");
            }
            String body = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                body = remoteMessage.getData().getOrDefault("body", "");
            }
            // Optionally extract senderId or chatId if sent in data payload
            String senderId = remoteMessage.getData().get("senderId");
            String chatId = remoteMessage.getData().get("chatId");

            sendNotification(title, body, remoteMessage.getData()); // Pass data for PendingIntent
        }

        // Check if message contains a notification payload. (Handled automatically
        // by system when app is in background/killed, but you might want custom
        // handling if app is in foreground)
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // If app is in foreground, you might want to show a custom heads-up notification
            // If app is background/killed, system handles showing it.
            // We call sendNotification here for foreground cases or if you want
            // *always* custom handling. Adjust logic as needed.
            sendNotification(title, body, remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // If you need to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Get the current logged-in user's ID
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null && token != null) {
            // Store the token in your Firebase Realtime Database under the user's profile
            FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated successfully for user: " + userId))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM Token for user: " + userId, e));
        }
    }

    private void sendNotification(String messageTitle, String messageBody, Map<String, String> data) {
        // Intent to open when notification is tapped.
        // Customize this to open your specific Chat Activity
        Intent intent = new Intent(this, MainActivity.class); // Change MainActivity to your main entry point or ChatListActivity
        // Add data from the notification payload to the intent so the receiving activity knows which chat to open
        if (data != null) {
            String chatId = data.get("chatId");
            String senderId = data.get("senderId"); // Or recipientId if you send that
            if (chatId != null) {
                intent.putExtra("chatId", chatId); // Pass necessary info
            }
            // Add other relevant data if needed
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE); // Use IMMUTABLE

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID) // Use the channel ID
                        .setSmallIcon(R.drawable.logofix) // ** IMPORTANT: Create this drawable **
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // For heads-up display
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since Android Oreo (API 26), notification channel is required.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Chat Messages", // User visible channel name
                    NotificationManager.IMPORTANCE_HIGH); // Set importance (HIGH for heads-up)
            channel.setDescription("Notifications for new chat messages"); // User visible channel description
            notificationManager.createNotificationChannel(channel);
        }

        // ID 0 allows replacement of notification if another comes for the same type
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}