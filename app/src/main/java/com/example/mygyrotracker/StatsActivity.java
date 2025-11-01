package com.example.mygyrotracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygyrotracker.database.AppDatabase;
import com.example.mygyrotracker.database.SensorData;

import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private TextView tvMin, tvMax, tvAvg, tvCount;
    private Button btnClear;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvMin = findViewById(R.id.tvMin);
        tvMax = findViewById(R.id.tvMax);
        tvAvg = findViewById(R.id.tvAvg);
        tvCount = findViewById(R.id.tvCount);
        btnClear = findViewById(R.id.btnClear);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        db = AppDatabase.getDatabase(this);

        // Кнопка очищення історії
        btnClear.setOnClickListener(v -> {
            new Thread(() -> {
                db.sensorDataDao().clearAll();
                runOnUiThread(() -> {
                    tvMin.setText("Min: -");
                    tvMax.setText("Max: -");
                    tvAvg.setText("Avg: -");
                    tvCount.setText("Count: 0");
                });
            }).start();
        });

        // Додаємо стрілку "назад" у Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Statistics");
        }


        // Показуємо початкову статистику
        updateStats();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateStats() {
        new Thread(() -> {
            try {
                List<SensorData> dataList = db.sensorDataDao().getAllData();

                if (dataList == null || dataList.isEmpty()) {
                    runOnUiThread(() -> {
                        tvMin.setText("Min: -");
                        tvMax.setText("Max: -");
                        tvAvg.setText("Avg: -");
                        tvCount.setText("Count: 0");
                    });
                    return;
                }

                float min = Float.MAX_VALUE;
                float max = -Float.MAX_VALUE;
                float sum = 0f;

                for (SensorData d : dataList) {
                    float v = d.value;
                    if (v < min) min = v;
                    if (v > max) max = v;
                    sum += v;
                }

                final float avg = sum / dataList.size();
                final float finalMin = min;
                final float finalMax = max;
                final int count = dataList.size();

                runOnUiThread(() -> {
                    tvMin.setText(String.format("Min: %.2f", finalMin));
                    tvMax.setText(String.format("Max: %.2f", finalMax));
                    tvAvg.setText(String.format("Avg: %.2f", avg));
                    tvCount.setText("Count: " + count);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> tvAvg.setText("Error loading stats"));
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }
}
