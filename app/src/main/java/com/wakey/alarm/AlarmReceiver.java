package com.wakey.alarm;
import com.wakey.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.widget.Toast;

import com.wakey.activities.AlarmRingActivity;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int alarmId = intent.getIntExtra("alarm_id", -1);

        // ia alarma din DB (dacă lipsește id, fallback pe ce exista)
        String objectName = intent.getStringExtra("object");
        if (objectName == null) objectName = "Unknown object";

        if (alarmId != -1) {
            try {
                var db = com.wakey.database.WakeyDatabase.getInstance(context);
                var alarm = db.alarmDao().getById(alarmId);
                if (alarm == null || !alarm.isActive) return;

                // reschedule / deactivate
                if (alarm.repeatDaysMask != 0) {
                    AlarmScheduler.schedule(context, alarm);
                    long next = AlarmScheduler.computeNextTriggerTimeMillis(alarm);
                    db.alarmDao().setNextTriggerAt(alarm.id, next);
                } else {
                    db.alarmDao().setActive(alarm.id, false);
                }

                objectName = alarm.selectedObjectName; // prefer DB value
            } catch (Exception ignored) {}
        }


       // PLAY ALARM SOUND
        try {
            Uri alarmSound = Uri.parse("android.resource://"
                    + context.getPackageName() + "/" + R.raw.wakey_alarm);

            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSound);

            if (ringtone != null) {
                ringtone.play();
                AlarmRingHolder.currentRingtone = ringtone;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        // OPEN ALARM SCREEN
        Intent i = new Intent(context, AlarmRingActivity.class);
        i.putExtra("object", objectName);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int imageRes = context.getResources().getIdentifier(
                objectName.trim().toLowerCase(java.util.Locale.ROOT).replace(" ", "_"),
                "drawable",
                context.getPackageName()
        );
        if (imageRes == 0) imageRes = R.drawable.ic_alarm;

        i.putExtra("image", imageRes);


        context.startActivity(i);

        Toast.makeText(context, "Alarm triggered: " + objectName, Toast.LENGTH_LONG).show();

    }
}
