package com.example.mygyrotracker;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mygyrotracker.database.AppDatabase;
import com.example.mygyrotracker.database.SensorData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    private LineChart chart;
    private Spinner spinnerRange;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        chart = findViewById(R.id.lineChart);
        spinnerRange = findViewById(R.id.spinnerRange);
        db = AppDatabase.getDatabase(this);

        // Кнопка "Назад"
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Спінер для вибору діапазону
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Last Hour", "Last Day", "Last Week"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRange.setAdapter(adapter);

        spinnerRange.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateChart(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Стрілочка "назад" у верхньому барі
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Додаємо обробку натискання системної "Назад"
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateChart(int rangeIndex) {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            long from;
            switch (rangeIndex) {
                case 0:
                    from = now - 60 * 60 * 1000; // остання година
                    break;
                case 1:
                    from = now - 24 * 60 * 60 * 1000; // останній день
                    break;
                case 2:
                default:
                    from = now - 7 * 24 * 60 * 60 * 1000; // останній тиждень
                    break;
            }

            // Отримуємо всі дані з БД
            List<SensorData> allData = db.sensorDataDao().getAllData();

            // Фільтруємо вручну без Stream API
            List<Entry> entries = new ArrayList<>();
            for (SensorData data : allData) {
                if (data.timestamp >= from) {
                    entries.add(new Entry((float) (data.timestamp - from) / 1000f, data.value));
                }
            }

            // Якщо даних немає — очищуємо графік
            if (entries.isEmpty()) {
                runOnUiThread(() -> {
                    chart.clear();
                    chart.invalidate();
                });
                return;
            }

            // Готуємо набір даних
            LineDataSet dataSet = new LineDataSet(entries, "Gyroscope deviation");
            dataSet.setDrawCircles(false);
            dataSet.setColor(Color.BLUE);
            dataSet.setLineWidth(2f);

            LineData lineData = new LineData(dataSet);

            // Оновлення графіка у UI-потоці
            runOnUiThread(() -> {
                chart.setData(lineData);
                chart.invalidate();
            });
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateChart(0);
    }
}
