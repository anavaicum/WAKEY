package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WakeHistoryDao {

    @Insert
    void insert(WakeHistoryEntity entry);

    // 🔹 Toate intrările (debug / istoric)
    @Query("SELECT * FROM wake_history ORDER BY date DESC")
    List<WakeHistoryEntity> getAll();

    // 🔹 Ultimele 7 ZILE CORECTE (pt chart & avg)
    @Query(
            "SELECT * FROM wake_history " +
                    "WHERE success = 1 AND emergencyUsed = 0 " +
                    "ORDER BY date DESC " +
                    "LIMIT 7"
    )
    List<WakeHistoryEntity> getLast7SuccessfulDays();

    // 🔹 Streak real (zile corecte)
    @Query(
            "SELECT COUNT(*) FROM wake_history " +
                    "WHERE success = 1 AND emergencyUsed = 0"
    )
    int getSuccessfulDaysCount();

    // 🔹 Ultima zi (pt logică lives)
    @Query(
            "SELECT * FROM wake_history " +
                    "ORDER BY date DESC " +
                    "LIMIT 1"
    )
    WakeHistoryEntity getLastEntry();

    @Query(
            "SELECT COUNT(DISTINCT date(wakeTime / 1000, 'unixepoch')) " +
                    "FROM wake_history " +
                    "WHERE success = 1 " +
                    "AND emergencyUsed = 0 " +
                    "AND wakeTime >= :startDate"
    )
    int countNoSnoozeDays(long startDate);

    @Query(
            "SELECT COUNT(DISTINCT date(wakeTime / 1000, 'unixepoch')) " +
                    "FROM wake_history " +
                    "WHERE success = 1 " +
                    "AND emergencyUsed = 0 " +
                    "AND wakeTime < :sixAmTodayMillis " +
                    "AND wakeTime >= :startDate"
    )
    int countEarlyRiserDays(long startDate, long sixAmTodayMillis);

}
