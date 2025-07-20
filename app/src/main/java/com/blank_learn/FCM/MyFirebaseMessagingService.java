package com.blank_learn.FCM;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color; // NEW: Import Color
import android.media.RingtoneManager; // NEW: Import RingtoneManager
import android.net.Uri; // NEW: Import Uri
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blank_learn.chat.ChatAA;
import com.blank_learn.dark.R;
import com.blank_learn.home.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHAT_CHANNEL_ID = "CHAT_MESSAGES";
    private static final String DEMO_CHANNEL_ID = "DEMO_BOOKINGS";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "New FCM Message From: " + remoteMessage.getFrom());

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();

        if (notification != null) {
            String title = notification.getTitle();
            String body = notification.getBody();
            showNotification(title, body, data);
        }
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        Intent intent;
        String channelId;

        String notificationType = data.get("type");
        if ("chat_message".equals(notificationType)) {
            channelId = CHAT_CHANNEL_ID;
            intent = new Intent(this, ChatAA.class);
            intent.putExtra("senderId", data.get("senderId"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else {
            channelId = DEMO_CHANNEL_ID;
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannels();

        // --- NEW: Customizations for the notification builder ---
        // Get the default notification sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.background_logo) // IMPORTANT: This icon MUST be simple, white, and transparent.
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)) // Allows for longer text
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensures it appears as a heads-up notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // Dismiss the notification when tapped
                .setSound(defaultSoundUri) // NEW: Add the default notification sound
                .setLights(Color.BLUE, 500, 500) // NEW: Pulse a blue light if the device supports it
                .setVibrate(new long[]{0, 500, 250, 500}); // NEW: Add a vibration pattern (vibrate-pause-vibrate)


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "POST_NOTIFICATIONS permission not granted. Cannot show notification.");
            return;
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Demo Channel - High importance to make a sound
            NotificationChannel demoChannel = new NotificationChannel(DEMO_CHANNEL_ID, "Demo Bookings", NotificationManager.IMPORTANCE_HIGH);
            demoChannel.setDescription("Notifications for new demo class bookings");
            demoChannel.enableLights(true); // NEW: Enable notification light
            demoChannel.setLightColor(Color.BLUE); // NEW: Set light color
            demoChannel.enableVibration(true); // NEW: Enable vibration

            // Chat Channel - High importance to make a sound
            NotificationChannel chatChannel = new NotificationChannel(CHAT_CHANNEL_ID, "Chat Messages", NotificationManager.IMPORTANCE_HIGH);
            chatChannel.setDescription("Notifications for new private chat messages");
            chatChannel.enableLights(true);
            chatChannel.setLightColor(Color.BLUE);
            chatChannel.enableVibration(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(demoChannel);
                manager.createNotificationChannel(chatChannel);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed: " + token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            DatabaseReference userTokenRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentUserId)
                    .child("fcmToken");
            userTokenRef.setValue(token);
        }
    }
}