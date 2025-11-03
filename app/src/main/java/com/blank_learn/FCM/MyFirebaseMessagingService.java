package com.blank_learn.FCM;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.blank_learn.chat.ChatAA;
import com.blank_learn.dark.R; 
import com.blank_learn.home.demoActivity;
 import com.blank_learn.home.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    private static final String CHANNEL_ID_ALERTS = "ALERTS_CHANNEL"; // For chat & demo bookings
    private static final String CHANNEL_ID_PROMOTIONS = "PROMOTIONS_CHANNEL"; // For offers & reminders

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification == null) {
            Log.d(TAG, "Received message without notification payload. Skipping.");
            return;
        }

        String title = notification.getTitle();
        String body = notification.getBody();
        String imageUrl = notification.getImageUrl() != null ? notification.getImageUrl().toString() : null;
        Map<String, String> data = remoteMessage.getData();

        handleNotification(title, body, imageUrl, data);
    }

    private void handleNotification(String title, String body, String imageUrl, Map<String, String> data) {
        Intent intent;
        String channelId;
        String notificationType = data.get("type");
        if (notificationType == null) {
            notificationType = "default";
        }

        Log.d(TAG, "Handling notification of type: " + notificationType);

        switch (notificationType) {
            case "chat_message":
            case "demo_booking": // For teachers
                channelId = CHANNEL_ID_ALERTS;
                if (notificationType.equals("chat_message")) {
                    intent = new Intent(this, ChatAA.class);
                    intent.putExtra("userId", data.get("senderId"));
                } else {
                    intent = new Intent(this, MainActivity.class); // Or a DemoDetailsActivity
                    intent.putExtra("bookingId", data.get("bookingId"));
                }
                break;

            case "signup_offer":
            case "demo_reminder":
                channelId = CHANNEL_ID_PROMOTIONS;
                intent = new Intent(this, demoActivity.class); // <--- REPLACE with your demo booking activity
                break;

            case "purchase_offer":
            case "purchase_offer_reminder":
                channelId = CHANNEL_ID_PROMOTIONS;
                intent = new Intent(this, MainActivity.class); // <--- REPLACE with your purchase/pricing activity
                intent.putExtra("PROMO_APPLIED", "500_OFF");
                break;

            default:
                channelId = CHANNEL_ID_PROMOTIONS;
                intent = new Intent(this, MainActivity.class);
                break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannels();

        Bitmap imageBitmap = getBitmapFromUrl(imageUrl);
        showVisualNotification(title, body, channelId, pendingIntent, imageBitmap);
    }

    private void showVisualNotification(String title, String body, String channelId, PendingIntent pendingIntent, Bitmap image) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.background_logo) // IMPORTANT: Must be a simple, white & transparent icon
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        if (image != null) {
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .setBigContentTitle(title)
                    .setSummaryText(body));
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(body));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Notification permission not granted.");
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        // VERY IMPORTANT: In a real app, use a library like Glide or Picasso for this.
        // This is a simplified version and not recommended for production as it runs on the main thread.
        // But for FCM Service, this synchronous call is sometimes acceptable if quick.
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading image for notification", e);
            return null;
        }
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // High-priority channel for important alerts
            NotificationChannel alertsChannel = new NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "Important Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertsChannel.setDescription("For chat messages and new demo bookings.");
            alertsChannel.enableVibration(true);

            // Default-priority channel for offers
            NotificationChannel promotionsChannel = new NotificationChannel(
                    CHANNEL_ID_PROMOTIONS,
                    "Offers & Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            promotionsChannel.setDescription("For special offers, discounts, and helpful reminders.");
            promotionsChannel.enableVibration(false); // Less intrusive

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(alertsChannel);
                manager.createNotificationChannel(promotionsChannel);
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