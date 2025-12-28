package com.wakey.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wake_history")
public class WakeHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // ziua (ex: 2025-01-15)
    public long date;

    // ora reală de trezire (timestamp)
    public long wakeTime;

    // true = a scanat obiectul
    public boolean success;

    // true = a folosit emergency
    public boolean emergencyUsed;
}
