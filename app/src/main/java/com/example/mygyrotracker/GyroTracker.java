package com.example.mygyrotracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.provider.Settings;
import android.os.Build;

import com.example.mygyrotracker.database.AppDatabase;
import com.example.mygyrotracker.database.SensorData;
import com.example.mygyrotracker.cloud.CloudSyncManager;
import com.google.firebase.auth.FirebaseAuth;

public class GyroTracker implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private AppDatabase db;
    private Handler handler = new Handler();
    private boolean isRecording = false;
    private int interval = 2000;
    private float lastX, lastY, lastZ;
    private Context context;
    private CloudSyncManager cloudSync;

    private String userId; // ✔ зберігаємо uid

    public GyroTracker(Context context) {
        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        db = AppDatabase.getDatabase(context);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cloudSync = new CloudSyncManager(userId, db, context);
            cloudSync.startListeningRemoteUpdates();
        } else {
            userId = "guest";
            cloudSync = null;
        }
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

                float deviation;
                if ((timestamp / 5000) % 2 == 0) {
                    deviation = (float) (Math.random() * 0.3);
                } else {
                    deviation = (float) (Math.random() * 3 + 0.5);
                }

                String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                String deviceModel = Build.MODEL;
                String osVersion = Build.VERSION.RELEASE;

                new Thread(() -> {

                    // === 1. Перевірка дублікатів ===
                    long startTime = timestamp - 50;
                    long endTime = timestamp + 50;

                    boolean isDuplicate =
                            !db.sensorDataDao().getDataInRange(startTime, endTime, userId).isEmpty();

                    if (isDuplicate) {
                        android.util.Log.w("GYRO_DUP", "⚠ DUPLICATE for " + userId + " at " + timestamp);
                    }

                    // === 2. Створення запису ===
                    SensorData data = new SensorData(
                            timestamp,
                            deviation,
                            "Gyroscope",
                            deviceId,
                            deviceModel,
                            osVersion,
                            isDuplicate,
                            userId
                    );

                    // === 3. Запис у БД ===
                    db.sensorDataDao().insert(data);

                    // === 4. Заливка у хмару ===
                    if (cloudSync != null) {
                        cloudSync.uploadRecord(data);
                    }

                }).start();

                String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "guest";
                long timeLimit = System.currentTimeMillis() - 24L * 60 * 60 * 1000;

                new Thread(() -> db.sensorDataDao().deleteOldData(timeLimit, currentUserId)).start();


                handler.postDelayed(this, interval);
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        lastX = event.values[0];
        lastY = event.values[1];
        lastZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

