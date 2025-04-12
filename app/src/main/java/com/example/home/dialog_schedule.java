package com.example.home;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blank_learn.dark.R;

public class dialog_schedule extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCustomDialog();
    }

    private void showCustomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_schedule);
        CalendarView calendarView = dialog.findViewById(R.id.calendar_view);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            Toast.makeText(this, "Selected Date: " + date, Toast.LENGTH_SHORT).show();
        });
        LinearLayout timeSlotContainer = dialog.findViewById(R.id.time_slot_container);
        String[] timeSlots = generateTimeSlots();
        for (String slot : timeSlots) {
            Button timeButton = new Button(this);
            timeButton.setText(slot);
            timeButton.setBackgroundResource(R.drawable.card_background);
            timeButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Match parent width
                    LinearLayout.LayoutParams.WRAP_CONTENT  // Wrap content height
            );
            params.setMargins(16, 16, 16, 16); // Left, Top, Right, Bottom margins in pixels
            timeButton.setLayoutParams(params);
            timeButton.setOnClickListener(v -> Toast.makeText(this, "Selected: " + slot, Toast.LENGTH_SHORT).show());
            timeSlotContainer.addView(timeButton);
        }


        LinearLayout classContainer = dialog.findViewById(R.id.time_slot_container1);
        String[] classess = generateClass();
        for (String className : classess) {
            Button classButton = new Button(this);
            classButton.setText(className);
            classButton.setBackgroundResource(R.drawable.card_background);
            classButton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Match parent width
                    LinearLayout.LayoutParams.WRAP_CONTENT  // Wrap content height
            );
            params.setMargins(16, 16, 16, 16); // Left, Top, Right, Bottom margins in pixels
            classButton.setLayoutParams(params);
            classButton.setOnClickListener(v ->
                    Toast.makeText(this, "Selected Class: " + className, Toast.LENGTH_SHORT).show()
            );
            classContainer.addView(classButton);
        }




        // Set up the Next button
        Button nextButton = dialog.findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> {
            String selectedClass = classess.toString();
            Toast.makeText(this, "Next clicked! Selected Class: " + selectedClass, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
    private String[] generateClass() {
        int totalClasses = 12;
        String[] classes = new String[totalClasses];
        for (int i = 0; i < totalClasses; i++) {
            classes[i] = "Class " + (i + 1);
        }
        return classes;
    }

    private String[] generateTimeSlots() {
        String[] slots = new String[15];
        int hour = 6;
        for (int i = 0; i < slots.length; i++) {
            slots[i] = String.format("%02d-%02d %s", hour, hour + 1, (hour < 12) ? "AM" : "PM");
            hour++;
        }
        return slots;
    }
}
