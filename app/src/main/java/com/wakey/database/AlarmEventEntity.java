package com.wakey.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "alarm_events",
        indices = {@Index(value = {"dateKey"}, unique = true)}
)
public class AlarmEventEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // "2026-01-06" (cheie unică per zi) – ca să nu dublezi progresul într-o zi
    @NonNull
    public String dateKey;

    // minute de la miezul nopții (ex: 5:55 -> 355)
    public int wakeMinuteOfDay;

    public boolean usedSnooze;
    public boolean usedEmergency;
    public boolean dismissedProperly; // true dacă a închis alarma prin flow normal (scan etc.)

    public AlarmEventEntity(@NonNull String dateKey, int wakeMinuteOfDay,
                            boolean usedSnooze, boolean usedEmergency, boolean dismissedProperly) {
        this.dateKey = dateKey;
        this.wakeMinuteOfDay = wakeMinuteOfDay;
        this.usedSnooze = usedSnooze;
        this.usedEmergency = usedEmergency;
        this.dismissedProperly = dismissedProperly;
    }
}
