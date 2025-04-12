package com.example.One_Signal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve data from WorkManager input
        String teacherPlayerId = getInputData().getString("teacherPlayerId");
        String studentPlayerId = getInputData().getString("studentPlayerId");
        String selectedDate = getInputData().getString("selectedDate");
        String selectedTimeSlot = getInputData().getString("selectedTimeSlot");
        String selectedClass = getInputData().getString("selectedClass");

        // Send notification to teacher and student
        sendNotification(teacherPlayerId, "Reminder: Your demo class for " + selectedClass + " is in 3 hours.");
        sendNotification(studentPlayerId, "Reminder: Your demo class for " + selectedClass + " is in 3 hours.");

        return Result.success();
    }

    private void sendNotification(String playerId, String message) {
        // OneSignal API URL
        String oneSignalApiUrl = "https://onesignal.com/api/v1/notifications";

        // OneSignal App ID
        String oneSignalAppId = "102b9dc4-8938-43bf-88b1-4df4b52d136d";

        // Create the JSON payload for the OneSignal API
        JSONObject notificationContent = new JSONObject();
        try {
            notificationContent.put("app_id", oneSignalAppId);
            notificationContent.put("include_player_ids", new JSONArray().put(playerId));
            notificationContent.put("contents", new JSONObject().put("en", message));
            notificationContent.put("headings", new JSONObject().put("en", "Demo Class Reminder"));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Send the notification using Volley or Retrofit (similar to previous implementation)
        // ...
    }



}