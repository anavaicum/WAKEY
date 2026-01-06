package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(UserProfileEntity profile);

    @Query("SELECT * FROM user_profile WHERE id = 1")
    UserProfileEntity get();
}
