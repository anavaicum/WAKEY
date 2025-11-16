package com.wakey.alarm;

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

        String objectName = intent.getStringExtra("object");
        if (objectName == null) objectName = "Unknown object";


       // PLAY ALARM SOUND
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

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

        context.startActivity(i);

        Toast.makeText(context, "Alarm triggered: " + objectName, Toast.LENGTH_LONG).show();

        // Ne asiguram ca alarma nu este oprita automat de android dupa cateva secunde
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "wakey:alarmLock"
        );

        wl.acquire(60 * 1000); // tine CPU-ul treaz 60 secunde

    }
}
