package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AlarmEventDao {

    // un singur eveniment pe zi (dacă se salvează de două ori, îl înlocuiește)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AlarmEventEntity event);

    @Query("SELECT COUNT(*) FROM alarm_events WHERE dateKey BETWEEN :startDate AND :endDate AND usedSnooze = 0 AND usedEmergency = 0 AND dismissedProperly = 1")
    int countSuccessfulNoSnoozeDays(String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM alarm_events WHERE dateKey BETWEEN :startDate AND :endDate AND wakeMinuteOfDay < :limitMinute AND dismissedProperly = 1")
    int countEarlyRiserDays(String startDate, String endDate, int limitMinute);
}
