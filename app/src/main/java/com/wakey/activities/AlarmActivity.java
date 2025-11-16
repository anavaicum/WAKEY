package com.wakey.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Button pickTimeBtn = findViewById(R.id.pickTimeBtn);
        Button saveAlarmBtn = findViewById(R.id.saveAlarmBtn);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        pickTimeBtn.setOnClickListener(v -> openTimePicker());
        saveAlarmBtn.setOnClickListener(v -> saveAlarm());
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

        String randomObject = getRandomObject();
        if (randomObject == null) {
            Toast.makeText(this, "Nu există obiecte selectate!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlarmEntity alarm = new AlarmEntity();
        alarm.hour = selectedHour;
        alarm.minute = selectedMinute;
        alarm.selectedObjectName = randomObject;
        alarm.isActive = true;

        WakeyDatabase.getInstance(this).alarmDao().insertAlarm(alarm);

        scheduleAlarm(selectedHour, selectedMinute, randomObject);

        Toast.makeText(this, "Alarmă setată!", Toast.LENGTH_SHORT).show();
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

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }

}
