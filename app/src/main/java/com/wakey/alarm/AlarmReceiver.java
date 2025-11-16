package com.wakey.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.wakey.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(context, "Permisiune notificări necesară!", Toast.LENGTH_SHORT).show();
                return; // NU trimite notificarea dacă permisiunea nu e acordată
            }
        }

        // Creare canal de notificări
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "wakey_channel",
                    "Wakey Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Notificare
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "wakey_channel")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Wakey Alarm")
                .setContentText("Trezirea! A sunat alarma!!!!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1, builder.build());

        Toast.makeText(context, "Alarmă declanșată!", Toast.LENGTH_SHORT).show();
    }

}
