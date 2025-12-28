package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface LifeDao {

    @Query("SELECT * FROM life WHERE id = 1 LIMIT 1")
    LifeEntity getLife();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LifeEntity life);

    @Query("UPDATE life SET lives = lives - 1 WHERE id = 1 AND lives > 0")
    void loseLife();

    @Query("UPDATE life SET lives = lives + 1 WHERE id = 1 AND lives < 3")
    void gainLife();

    @Query("UPDATE life SET correctDaysInRow = correctDaysInRow + 1 WHERE id = 1")
    void incrementStreak();

    @Query("UPDATE life SET correctDaysInRow = 0 WHERE id = 1")
    void resetStreak();
}
