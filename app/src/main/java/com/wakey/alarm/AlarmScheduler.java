package com.wakey.alarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.wakey.database.AlarmEntity;

import java.util.Calendar;

public class AlarmScheduler {

    // bit0=Mon ... bit6=Sun
    public static int dayOfWeekToBitIndex(int calendarDayOfWeek) {
        // Calendar: SUNDAY=1 ... SATURDAY=7
        switch (calendarDayOfWeek) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            case Calendar.SUNDAY: return 6;
        }
        return 0;
    }

    public static long computeNextTriggerTimeMillis(AlarmEntity alarm) {
        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int mask = alarm.repeatDaysMask;

        // ONE-SHOT
        if (mask == 0) {
            c.set(Calendar.HOUR_OF_DAY, alarm.hour);
            c.set(Calendar.MINUTE, alarm.minute);

            if (c.getTimeInMillis() <= nowMs) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            return c.getTimeInMillis();
        }

        // REPEATING (următoarea zi selectată)
        for (int addDays = 0; addDays < 7; addDays++) {
            Calendar candidate = Calendar.getInstance();
            candidate.set(Calendar.SECOND, 0);
            candidate.set(Calendar.MILLISECOND, 0);
            candidate.add(Calendar.DAY_OF_YEAR, addDays);
            candidate.set(Calendar.HOUR_OF_DAY, alarm.hour);
            candidate.set(Calendar.MINUTE, alarm.minute);

            int bitIndex = dayOfWeekToBitIndex(candidate.get(Calendar.DAY_OF_WEEK));
            boolean selected = (mask & (1 << bitIndex)) != 0;

            if (!selected) continue;
            if (candidate.getTimeInMillis() <= nowMs) continue;

            return candidate.getTimeInMillis();
        }

        // fallback (teoretic nu ajunge aici)
        c.add(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.HOUR_OF_DAY, alarm.hour);
        c.set(Calendar.MINUTE, alarm.minute);
        return c.getTimeInMillis();
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void schedule(Context context, AlarmEntity alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.id);
        intent.putExtra("object", alarm.selectedObjectName);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarm.id, // IMPORTANT: unic per alarmă
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = computeNextTriggerTimeMillis(alarm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // dacă userul nu a dat voie pentru exact alarms, app-ul tău deja gestionează asta în AlarmActivity
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pi
        );
    }

    public static void cancel(Context context, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pi);
    }
}
