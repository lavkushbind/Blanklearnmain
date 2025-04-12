package com.example.notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.blank_learn.dark.R;

public class FcmActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "FCM_CHANNEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcm);

        Button btnNotify = findViewById(R.id.btn_schedule);
        btnNotify.setOnClickListener(v -> {
            Log.d("ButtonClick", "Button clicked!");
            Toast.makeText(this, "tttt", Toast.LENGTH_SHORT).show();
            showNotification();
        });
    }

    private void showNotification() {
        // Get the Notification Manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sample Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Create an Intent to open the activity when the notification is clicked
        Intent intent = new Intent(this, FcmActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logofix)  // Replace with a valid icon resource
                .setContentTitle("Hello Lavkush")
                .setContentText("This is a notification popup")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);  // Open the app when clicked

        // Show the notification
        notificationManager.notify(1, builder.build());
    }
}
