package com.blank_learn.session;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.blank_learn.dark.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class Result_both_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_both);

        // --- Find all UI elements ---
        // My views
        TextView myFocusPercentageText = findViewById(R.id.myFocusPercentageText);
        TextView myDistractionTimeText = findViewById(R.id.myDistractionTimeText);
        LineChart myFocusChart = findViewById(R.id.myFocusChart);

        // Partner views
        CardView partnerResultsCard = findViewById(R.id.partnerResultsCard);
        TextView partnerFocusPercentageText = findViewById(R.id.partnerFocusPercentageText);
        TextView partnerDistractionTimeText = findViewById(R.id.partnerDistractionTimeText);
        LineChart partnerFocusChart = findViewById(R.id.partnerFocusChart);

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> finish());

        // --- Get data from Intent ---
        ArrayList<FocusVideoCallActivity.FocusStatus> myDataList =
                (ArrayList<FocusVideoCallActivity.FocusStatus>) getIntent().getSerializableExtra("MY_FOCUS_DATA_LIST");
        ArrayList<FocusVideoCallActivity.FocusStatus> partnerDataList =
                (ArrayList<FocusVideoCallActivity.FocusStatus>) getIntent().getSerializableExtra("PARTNER_FOCUS_DATA_LIST");

        // --- Populate Views ---
        if (myDataList != null && !myDataList.isEmpty()) {
            populateResultsView(myDataList, myFocusPercentageText, myDistractionTimeText, myFocusChart);
        }

        if (partnerDataList != null && !partnerDataList.isEmpty()) {
            partnerResultsCard.setVisibility(View.VISIBLE);
            populateResultsView(partnerDataList, partnerFocusPercentageText, partnerDistractionTimeText, partnerFocusChart);
        }
    }

    private void populateResultsView(ArrayList<FocusVideoCallActivity.FocusStatus> dataList, TextView focusText, TextView distractionText, LineChart chart) {
        // --- Calculate Stats ---
        int focusedCount = 0;
        int distractedCount = 0; // Combines looking away and tired
        for (FocusVideoCallActivity.FocusStatus status : dataList) {
            if (status == FocusVideoCallActivity.FocusStatus.FOCUSED) {
                focusedCount++;
            } else {
                distractedCount++;
            }
        }

        int totalFrames = dataList.size();
        if (totalFrames == 0) totalFrames = 1; // Avoid division by zero
        int focusPercentage = (focusedCount * 100) / totalFrames;

        // This is a rough estimate of time. For better accuracy, you'd need the actual FPS.
        // Assuming ~10 FPS for this calculation.
        double assumedFps = 10.0;
        int distractionTimeInSeconds = (int) (distractedCount / assumedFps);

        // --- Update UI ---
        focusText.setText("Focus Level: " + focusPercentage + "%");
        distractionText.setText("Time Distracted: " + distractionTimeInSeconds + "s");
        setupFocusChart(dataList, chart);
    }

    private void setupFocusChart(ArrayList<FocusVideoCallActivity.FocusStatus> dataList, LineChart focusChart) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            FocusVideoCallActivity.FocusStatus status = dataList.get(i);
            float yValue;
            switch (status) {
                case FOCUSED: yValue = 2f; break;
                case LOOKING_AWAY: case EYES_CLOSED: yValue = 1f; break;
                default: yValue = 0f; break; // NO_FACE_DETECTED
            }
            entries.add(new Entry(i, yValue));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Focus Timeline");
        dataSet.setColor(Color.parseColor("#6BDD4D"));
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#6BDD4D"));
        dataSet.setFillAlpha(80);

        focusChart.setData(new LineData(dataSet));
        focusChart.getDescription().setEnabled(false);
        focusChart.getLegend().setEnabled(false);

        XAxis xAxis = focusChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = focusChart.getAxisLeft();
        yAxisLeft.setAxisMaximum(2.5f);
        yAxisLeft.setAxisMinimum(-0.5f);
        yAxisLeft.setLabelCount(3, true);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 1.5f) return "Focused";
                if (value > 0.5f) return "Distracted";
                return "Away";
            }
        });

        focusChart.getAxisRight().setEnabled(false);
        focusChart.animateX(1000);
        focusChart.invalidate();
    }
}