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
}
