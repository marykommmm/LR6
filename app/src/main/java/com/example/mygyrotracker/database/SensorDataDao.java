package com.example.mygyrotracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SensorDataDao {
    @Insert
    void insert(SensorData data);

    @Query("SELECT * FROM sensor_data ORDER BY timestamp ASC")
    List<SensorData> getAllData();

    @Query("DELETE FROM sensor_data WHERE timestamp < :timeLimit")
    void deleteOldData(long timeLimit);

    @Query("DELETE FROM sensor_data")
    void clearAll();

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC LIMIT 1")
    SensorData getLastData();
}
