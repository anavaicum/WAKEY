package com.wakey.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AlarmEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int hour;
    public int minute;
    public String selectedObjectName;
    public boolean isActive;

    // Bitmask pentru zile: bit0=Mon ... bit6=Sun. 0 = one-shot
    public int repeatDaysMask;

    // opțional, util pentru debug/UI
    public long nextTriggerAt;
}
