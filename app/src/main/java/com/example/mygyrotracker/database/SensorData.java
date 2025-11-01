package com.example.mygyrotracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "sensor_data")
public class SensorData {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "value")
    public float value;

    @ColumnInfo(name = "type")
    public String type;

    public SensorData(long timestamp, float value, String type) {
        this.timestamp = timestamp;
        this.value = value;
        this.type = type;
    }

    public SensorData() {}

}
