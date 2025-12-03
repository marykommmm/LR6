package com.example.mygyrotracker.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SensorDataDao {
    @Insert
    void insert(SensorData data);

    @Update
    void update(SensorData data);

    @Query("SELECT * FROM sensor_data WHERE user_id = :userId ORDER BY timestamp ASC")
    List<SensorData> getAllData(String userId);

    @Query("SELECT * FROM sensor_data WHERE user_id = :userId ORDER BY timestamp DESC LIMIT 1")
    SensorData getLastData(String userId);

    @Query("DELETE FROM sensor_data WHERE timestamp < :timeLimit AND user_id = :userId")
    void deleteOldData(long timeLimit, String userId);

    @Query("DELETE FROM sensor_data WHERE user_id = :userId")
    void clearAll(String userId);

    @Query("SELECT * FROM sensor_data WHERE timestamp BETWEEN :startTime AND :endTime AND user_id = :userId")
    List<SensorData> getDataInRange(long startTime, long endTime, String userId);

    @Query("SELECT * FROM sensor_data WHERE device_id = :deviceId AND user_id = :userId ORDER BY timestamp ASC")
    List<SensorData> getDataByDevice(String deviceId, String userId);

    @Query("SELECT DISTINCT device_id FROM sensor_data WHERE user_id = :userId")
    List<String> getAllDevices(String userId);
}

