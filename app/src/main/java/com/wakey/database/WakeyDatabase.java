package com.wakey.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SelectedObjectEntity.class}, version = 1)
public abstract class WakeyDatabase extends RoomDatabase {

    private static WakeyDatabase instance;

    public abstract SelectedObjectsDao selectedObjectsDao();

    public static synchronized WakeyDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            WakeyDatabase.class, "wakey_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // simplu pentru prototip (în versiunea finală facem async)
                    .build();
        }
        return instance;
    }
}
