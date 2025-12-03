package com.example.mygyrotracker;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
    private Spinner spinnerRange, spinnerDevices;
    private AppDatabase db;

    private String userId;

    private final int[] colors = {
            Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA,
            Color.CYAN, Color.YELLOW, Color.LTGRAY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        chart = findViewById(R.id.lineChart);
        spinnerRange = findViewById(R.id.spinnerRange);
        spinnerDevices = findViewById(R.id.spinnerDevices);

        db = AppDatabase.getDatabase(this);

        // ⬇️ Беремо userId
        SharedPreferences prefs = getSharedPreferences("gyro_prefs", MODE_PRIVATE);
        userId = prefs.getString("user_id", "default");

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupRangeSpinner();
        loadDeviceList();
    }

    private void setupRangeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Last Hour", "Last Day", "Last Week"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRange.setAdapter(adapter);

        spinnerRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                updateChart();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDeviceList() {
        new Thread(() -> {

            // ⬇️ Тепер вимагає userId
            List<String> devices = db.sensorDataDao().getAllDevices(userId);

            if (devices == null) devices = new ArrayList<>();

            devices.add(0, "All devices");
            List<String> finalDevices = devices;

            runOnUiThread(() -> {
                ArrayAdapter<String> adapterDevices = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        finalDevices
                );
                adapterDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDevices.setAdapter(adapterDevices);

                spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        updateChart();
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            });
        }).start();
    }

    private void updateChart() {

        if (spinnerDevices.getSelectedItem() == null) return;
        if (spinnerRange.getSelectedItem() == null) return;

        new Thread(() -> {
            long now = System.currentTimeMillis();

            int rangeIndex = spinnerRange.getSelectedItemPosition();
            long from;

            if (rangeIndex == 0) from = now - 60 * 60 * 1000;
            else if (rangeIndex == 1) from = now - 24 * 60 * 60 * 1000;
            else from = now - 7 * 24 * 60 * 60 * 1000;

            String selectedDevice = spinnerDevices.getSelectedItem().toString();
            List<String> devicesToDraw = new ArrayList<>();

            if (selectedDevice.equals("All devices")) {
                // ⬇️ теж userId
                devicesToDraw = db.sensorDataDao().getAllDevices(userId);
            } else {
                devicesToDraw.add(selectedDevice);
            }

            List<LineDataSet> sets = new ArrayList<>();
            int colorIndex = 0;

            for (String dev : devicesToDraw) {

                // ⬇️ тепер два параметри
                List<SensorData> data = db.sensorDataDao().getDataByDevice(dev, userId);
                List<Entry> entries = new ArrayList<>();

                for (SensorData d : data) {
                    if (d.timestamp >= from) {
                        entries.add(new Entry(
                                (float) ((d.timestamp - from) / 1000f),
                                d.value
                        ));
                    }
                }

                LineDataSet ds = new LineDataSet(entries, dev);
                ds.setDrawCircles(false);
                ds.setColor(colors[colorIndex % colors.length]);
                ds.setLineWidth(2f);

                sets.add(ds);
                colorIndex++;
            }

            LineData lineData = new LineData(sets.toArray(new LineDataSet[0]));

            runOnUiThread(() -> {
                chart.setData(lineData);
                chart.invalidate();
            });

        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateChart();
    }
}
