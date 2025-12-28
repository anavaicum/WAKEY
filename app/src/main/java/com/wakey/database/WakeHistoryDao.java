package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WakeHistoryDao {

    @Insert
    void insert(WakeHistoryEntity entry);

    @Query("SELECT * FROM wake_history ORDER BY date DESC")
    List<WakeHistoryEntity> getAll();

    @Query("SELECT * FROM wake_history ORDER BY date DESC LIMIT 7")
    List<WakeHistoryEntity> getLast7Days();

    @Query("SELECT COUNT(*) FROM wake_history WHERE success = 1")
    int getSuccessfulDaysCount();
}
