package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WakeHistoryDao {

    @Insert
    void insert(WakeHistoryEntity entry);

  
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
            "SELECT COUNT(DISTINCT date(wakeTime / 1000, 'unixepoch')) " +
                    "FROM wake_history " +
                    "WHERE success = 1 " +
                    "AND emergencyUsed = 0 " +
                    "AND wakeTime >= (" +
                    "SELECT IFNULL(MAX(wakeTime), 0) FROM wake_history " +
                    "WHERE success = 0 OR emergencyUsed = 1" +
                    ")"
    )
    int countNoSnoozeStreak();


    @Query(
            "SELECT COUNT(DISTINCT date(wakeTime / 1000, 'unixepoch')) " +
                    "FROM wake_history " +
                    "WHERE success = 1 " +
                    "AND emergencyUsed = 0 " +
                    "AND wakeTime < :sixAmMillis " +
                    "AND wakeTime >= (" +
                    "SELECT IFNULL(MAX(wakeTime), 0) FROM wake_history " +
                    "WHERE success = 0 OR emergencyUsed = 1" +
                    ")"
    )
    int countEarlyRiserStreak(long sixAmMillis);


    @Query("SELECT COUNT(*) FROM wake_history WHERE success = 1 AND emergencyUsed = 0 AND wakeTime >= :dayStart AND wakeTime < :dayEnd")
    int hasSuccessToday(long dayStart, long dayEnd);

    @Query("SELECT COUNT(DISTINCT date / 86400000) FROM wake_history WHERE success = 1 AND emergencyUsed = 0 AND date >= :start")
    int countDistinctSuccessfulDays(long start);

    @Query(
            "SELECT COUNT(*) FROM wake_history " +
                    "WHERE success = 1 " +
                    "AND emergencyUsed = 0 " +
                    "AND wakeTime >= :dayStart " +
                    "AND wakeTime < :dayEnd"
    )
    int hasWakeToday(long dayStart, long dayEnd);





}
