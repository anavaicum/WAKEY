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
        // Preluăm obiectul random trimis de AlarmActivity
        String objectName = intent.getStringExtra("object");
        if (objectName == null) objectName = "Unknown object";

        // Permisiune Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(context, "Notification permission needed!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create notification channel (Android 8+)
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

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "wakey_channel")
                .setSmallIcon(R.drawable.ic_alarm)  // asigură-te că există în drawable
                .setContentTitle("Wakey Alarm")
                .setContentText("Wake up! Today's object: " + objectName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1, builder.build());

        // Feedback vizual rapid
        Toast.makeText(context,
                "Alarm triggered! Object of the day: " + objectName,
                Toast.LENGTH_LONG).show();
    }
    }

