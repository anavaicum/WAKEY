package com.wakey.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wakey.R;
import com.wakey.alarm.AlarmReceiver;
import com.wakey.database.AlarmEntity;
import com.wakey.database.SelectedObjectEntity;
import com.wakey.database.WakeyDatabase;
import com.wakey.models.ObjectItem;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class AlarmActivity extends AppCompatActivity  {
    private TextView selectedTimeText;
    private int selectedHour = -1;
    private int selectedMinute = -1;
    private com.google.android.material.chip.Chip chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Button pickTimeBtn = findViewById(R.id.pickTimeBtn);
        Button saveAlarmBtn = findViewById(R.id.saveAlarmBtn);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        chipMon = findViewById(R.id.chipMon);
        chipTue = findViewById(R.id.chipTue);
        chipWed = findViewById(R.id.chipWed);
        chipThu = findViewById(R.id.chipThu);
        chipFri = findViewById(R.id.chipFri);
        chipSat = findViewById(R.id.chipSat);
        chipSun = findViewById(R.id.chipSun);

        pickTimeBtn.setOnClickListener(v -> openTimePicker());
        saveAlarmBtn.setOnClickListener(v -> saveAlarm());
    }

    private int buildRepeatMask() {
        int mask = 0;
        if (chipMon.isChecked()) mask |= (1 << 0);
        if (chipTue.isChecked()) mask |= (1 << 1);
        if (chipWed.isChecked()) mask |= (1 << 2);
        if (chipThu.isChecked()) mask |= (1 << 3);
        if (chipFri.isChecked()) mask |= (1 << 4);
        if (chipSat.isChecked()) mask |= (1 << 5);
        if (chipSun.isChecked()) mask |= (1 << 6);
        return mask;
    }
    private void openTimePicker() {
        Calendar now = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    selectedTimeText.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    private void saveAlarm() {

        if (selectedHour == -1 || selectedMinute == -1) {
            Toast.makeText(this, "Alege o oră!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (!am.canScheduleExactAlarms()) {

                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                Toast.makeText(this, "Enable exact alarms and come back.", Toast.LENGTH_LONG).show();
                return; // STOP — NU salvăm încă
            }
        }

        // 🔥 Acum putem salva alarma o singură dată
        String randomObject = getRandomObject();
        if (randomObject == null) {
            Toast.makeText(this, "Nu există obiecte selectate!", Toast.LENGTH_SHORT).show();
            return;
        }

        int repeatMask = buildRepeatMask();

        AlarmEntity alarm = new AlarmEntity();
        alarm.hour = selectedHour;
        alarm.minute = selectedMinute;
        alarm.selectedObjectName = randomObject;
        alarm.isActive = true;
        alarm.repeatDaysMask = repeatMask;

        long newId = WakeyDatabase.getInstance(this).alarmDao().insertAlarm(alarm);
        alarm.id = (int) newId;

        long next = com.wakey.alarm.AlarmScheduler.computeNextTriggerTimeMillis(alarm);
        alarm.nextTriggerAt = next;
        WakeyDatabase.getInstance(this).alarmDao().setNextTriggerAt(alarm.id, next);

        com.wakey.alarm.AlarmScheduler.schedule(this, alarm);

        Toast.makeText(this, "Alarmă setată!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, AlarmListActivity.class));
        finish();
    }


    private String getRandomObject() {
        List<SelectedObjectEntity> items = WakeyDatabase
                .getInstance(this)
                .selectedObjectsDao()
                .getAllSelected();

        if (items == null || items.isEmpty()) return null;

        return items.get(new Random().nextInt(items.size())).getName();
    }

    private void scheduleAlarm(int hour, int minute, String selectedObject) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("object", selectedObject);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Permission required to set exact alarms!", Toast.LENGTH_LONG).show();
        }
    }


}
