package com.wakey.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
                SelectedObjectEntity.class,
                AlarmEntity.class,
                LifeEntity.class,
                WakeHistoryEntity.class,
                WakeTargetEntity.class,
                UserProfileEntity.class
        },
        version = 7,
        exportSchema = false
)

public abstract class WakeyDatabase extends RoomDatabase {

    private static WakeyDatabase instance;

    // MIGRATION 2 -> 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE AlarmEntity ADD COLUMN repeatDaysMask INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE AlarmEntity ADD COLUMN nextTriggerAt INTEGER NOT NULL DEFAULT 0");
        }
    };

    public abstract SelectedObjectsDao selectedObjectsDao();
    public abstract AlarmDao alarmDao();

    public abstract LifeDao lifeDao();
    public abstract WakeHistoryDao wakeHistoryDao();

    public abstract WakeTargetDao wakeTargetDao();

    public abstract UserProfileDao userProfileDao();



    public static synchronized WakeyDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            WakeyDatabase.class, "wakey_database")
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // rămâne fallback dacă apar alte mismatch-uri
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}

