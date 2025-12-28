package com.wakey.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "life")
public class LifeEntity {

    @PrimaryKey
    public int id;

    public int lives;
    public int correctDaysInRow;
}
