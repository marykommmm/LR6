package com.example.mygyrotracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.example.mygyrotracker.database.AppDatabase;
import com.example.mygyrotracker.database.SensorData;

public class GyroTracker implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private AppDatabase db;
    private Handler handler = new Handler();
    private boolean isRecording = false;
    private int interval = 2000; // кожні 2 секунди
    private float lastX, lastY, lastZ;

    public GyroTracker(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        db = AppDatabase.getDatabase(context);
    }

    public void startRecording() {
        if (!isRecording) {
            isRecording = true;
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
            handler.post(recordRunnable);
        }
    }

    public void stopRecording() {
        isRecording = false;
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(recordRunnable);
    }

    private Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                long timestamp = System.currentTimeMillis();

                // Симуляція спокою та активності
                float deviation;
                if ((timestamp / 5000) % 2 == 0) {
                    deviation = (float)(Math.random() * 0.3); // спокій
                } else {
                    deviation = (float)(Math.random() * 3 + 0.5); // активність
                }

                new Thread(() -> db.sensorDataDao().insert(
                        new SensorData(timestamp, deviation, "Gyroscope"))).start();

                // Очищення старих даних (24 години)
                long timeLimit = System.currentTimeMillis() - 24*60*60*1000;
                new Thread(() -> db.sensorDataDao().deleteOldData(timeLimit)).start();

                handler.postDelayed(this, interval);
            }
        }
    };


    public float getCurrentDeviation() {
        return lastX + lastY + lastZ;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
