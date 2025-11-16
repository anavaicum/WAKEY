package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlarmDao {

    @Insert
    void insertAlarm(AlarmEntity alarm);

    @Query("SELECT * FROM AlarmEntity")
    List<AlarmEntity> getAllAlarms();
}
