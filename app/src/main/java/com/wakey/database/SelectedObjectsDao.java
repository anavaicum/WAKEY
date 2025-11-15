package com.wakey.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SelectedObjectsDao {

    @Insert
    void insertAll(List<SelectedObjectEntity> objects);

    @Query("SELECT * FROM selected_objects")
    List<SelectedObjectEntity> getAllSelected();

    @Query("DELETE FROM selected_objects")
    void clearAll();
}
