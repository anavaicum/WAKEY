package com.wakey.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wake_target")
public class WakeTargetEntity {

    @PrimaryKey
    public int id = 1; // mereu un singur rând

    // ora țintă în minute de la miezul nopții (ex: 7*60 = 420)
    public int targetMinutes;
}
