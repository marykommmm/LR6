package com.example.mygyrotracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygyrotracker.database.AppDatabase;
import com.example.mygyrotracker.database.SensorData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvCurrentValue;
    private Button btnStartStop, btnChart, btnStats;

    private boolean isRecording = false;
    private Handler handler = new Handler();
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private GyroTracker gyroTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCurrentValue = findViewById(R.id.tvCurrentValue);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnChart = findViewById(R.id.btnChart);
        btnStats = findViewById(R.id.btnStats);

        db = AppDatabase.getDatabase(this);
        gyroTracker = new GyroTracker(this);

        btnStartStop.setOnClickListener(v -> {
            if (isRecording) {
                stopTracking();
                btnStartStop.setText("Start");
            } else {
                startTracking();
                btnStartStop.setText("Stop");
            }
        });

        btnChart.setOnClickListener(v -> startActivity(new Intent(this, ChartActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));

        handler.post(updateRunnable);
    }

    private void startTracking() {
        isRecording = true;
        gyroTracker.startRecording();
    }

    private void stopTracking() {
        isRecording = false;
        gyroTracker.stopRecording();
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            executor.execute(() -> {
                SensorData last = db.sensorDataDao().getLastData();
                float currentValue = (last != null) ? last.value : 0f;
                runOnUiThread(() -> tvCurrentValue.setText(
                        String.format("Current deviation: %.2f", currentValue)
                ));
            });
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTracking();
        handler.removeCallbacks(updateRunnable);
        executor.shutdownNow();
    }
}
