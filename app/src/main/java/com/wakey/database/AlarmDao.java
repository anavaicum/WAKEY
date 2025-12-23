package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface AlarmDao {
        @Insert
        long insertAlarm(AlarmEntity alarm);

        @Query("SELECT * FROM AlarmEntity")
        List<AlarmEntity> getAllAlarms();

        @Query("SELECT * FROM AlarmEntity WHERE id = :id LIMIT 1")
        AlarmEntity getById(int id);

        @Query("UPDATE AlarmEntity SET isActive = :active WHERE id = :id")
        void setActive(int id, boolean active);

        @Query("UPDATE AlarmEntity SET nextTriggerAt = :nextTriggerAt WHERE id = :id")
        void setNextTriggerAt(int id, long nextTriggerAt);

        @Delete
        void deleteAlarm(AlarmEntity alarm);
}