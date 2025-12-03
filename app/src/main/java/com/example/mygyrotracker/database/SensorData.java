package com.example.mygyrotracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "sensor_data")
public class SensorData {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "value")
    public float value;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "device_id")
    public String deviceId;

    @ColumnInfo(name = "device_model")
    public String deviceModel;

    @ColumnInfo(name = "os_version")
    public String osVersion;

    @ColumnInfo(name = "is_duplicate")
    public boolean isDuplicate;

    @ColumnInfo(name = "user_id")
    public String userId;

    @Ignore
    public SensorData(long timestamp, float value, String type,
                      String deviceId, String deviceModel, String osVersion,
                      boolean isDuplicate, String userId) {
        this.timestamp = timestamp;
        this.value = value;
        this.type = type;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.isDuplicate = isDuplicate;
        this.userId = userId;
    }

    public SensorData() {}
}
