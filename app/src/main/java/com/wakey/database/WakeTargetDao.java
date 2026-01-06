package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface WakeTargetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(WakeTargetEntity target);

    @Query("SELECT * FROM wake_target WHERE id = 1")
    WakeTargetEntity get();

    @Query("SELECT * FROM wake_target LIMIT 1")
    WakeTargetEntity getTarget();

}