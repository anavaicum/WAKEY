package com.wakey.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.wakey.R;
import com.wakey.database.LifeEntity;
import com.wakey.database.WakeHistoryEntity;
import com.wakey.database.WakeyDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.wakey.views.WeeklyWakeChartView;

import com.wakey.database.WakeTargetEntity;



public class MainActivity extends AppCompatActivity {

    // 🔹 Stats
    private TextView textStreakValue;
    private TextView textStreakDelta;
    private TextView textAvgWakeValue;
    private TextView textAvgWakeDelta;

    private TextView textWakeTarget;

    private WeeklyWakeChartView weeklyChart;


    // 🔹 Lives & progress
    private TextView textLivesHearts;
    private TextView textProgress;
    private ProgressBar progressBar;


    // 🔹 Bottom nav
    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ===== FIND VIEWS =====
        textStreakValue = findViewById(R.id.textStreakValue);
        textStreakDelta = findViewById(R.id.textStreakDelta);
        textAvgWakeValue = findViewById(R.id.textAvgWakeValue);
        textAvgWakeDelta = findViewById(R.id.textAvgWakeDelta);

        textLivesHearts = findViewById(R.id.textLivesHearts);
        textProgress = findViewById(R.id.textProgress);
        progressBar = findViewById(R.id.progressToNextLife);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        weeklyChart = findViewById(R.id.weeklyChart);

        textWakeTarget = findViewById(R.id.textWakeTarget);
        textWakeTarget.setOnClickListener(v -> openTimePicker());


        // ===== BOTTOM NAV =====
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                // Suntem DEJA în MainActivity (Dashboard)
                // NU facem nimic
                return true;
            }

            if (id == R.id.nav_alarms) {
                startActivity(new Intent(this, AlarmListActivity.class));
                return true;
            }

            if (id == R.id.nav_profile) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .commit();
                return true;
            }
            if (id == R.id.nav_dashboard) {
                // Eliminăm orice fragment (ex: ProfileFragment)
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new Fragment())
                        .commit();

                return true;
            }

            return false;
        });



        // ===== DATA =====
        initLifeIfNeeded();
        loadLifeFromDb();
        loadAvgWakeTime();
        loadWeeklyChart();

    }

    // =========================
    // DB INIT
    // =========================
    private void initLifeIfNeeded() {
        new Thread(() -> {
            WakeyDatabase db = WakeyDatabase.getInstance(this);
            if (db.lifeDao().getLife() == null) {
                LifeEntity life = new LifeEntity();
                life.id = 1;
                life.lives = 3;
                life.correctDaysInRow = 0;
                db.lifeDao().insert(life);
            }
        }).start();
    }

    private void loadLifeFromDb() {
        new Thread(() -> {
            LifeEntity life = WakeyDatabase.getInstance(this)
                    .lifeDao()
                    .getLife();

            runOnUiThread(() -> updateUI(life));
        }).start();
    }

    // =========================
    // UI UPDATE
    // =========================
    private void updateUI(LifeEntity life) {
        if (life == null) return;

        // ❤️ LIVES
        int maxLives = 3;
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < maxLives; i++) {
            hearts.append(i < life.lives ? "❤️ " : "🤍 ");
        }
        textLivesHearts.setText(hearts.toString().trim());

        // 🔥 STREAK
        textStreakValue.setText(life.correctDaysInRow + " days");
        textStreakDelta.setText("+0%");

        // 📊 PROGRESS
        int progress = Math.min((life.correctDaysInRow * 100) / 2, 100);
        progressBar.setProgress(progress);
        textProgress.setText(life.correctDaysInRow + "/2 days");

        // ⏰ AVG WAKE (placeholder real, nu minciună)
        textAvgWakeValue.setText("--:--");
        textAvgWakeDelta.setText("");
    }

    // =========================
    // 🚨 EMERGENCY
    // =========================
    private void useEmergency() {
        new Thread(() -> {
            WakeyDatabase db = WakeyDatabase.getInstance(this);
            LifeEntity life = db.lifeDao().getLife();

            if (life.lives > 0) {
                db.lifeDao().loseLife();
                db.lifeDao().resetStreak();

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            "Emergency used! You lost 1 life.",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadLifeFromDb();
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(this, "No lives left!", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private String calculateAvgWakeTime(List<WakeHistoryEntity> entries) {
        if (entries == null || entries.isEmpty()) {
            return "--:--";
        }

        long totalMinutes = 0;

        for (WakeHistoryEntity e : entries) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.wakeTime);

            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            totalMinutes += hour * 60 + minute;
        }

        long avgMinutes = totalMinutes / entries.size();

        long avgHour = avgMinutes / 60;
        long avgMinute = avgMinutes % 60;

        // format HH:mm
        return String.format(Locale.getDefault(), "%02d:%02d", avgHour, avgMinute);
    }


    private void loadAvgWakeTime() {
        new Thread(() -> {
            List<WakeHistoryEntity> entries =
                    WakeyDatabase.getInstance(this)
                            .wakeHistoryDao()
                            .getLast7SuccessfulDays();

            String avgWake = calculateAvgWakeTime(entries);

            runOnUiThread(() ->
                    textAvgWakeValue.setText(avgWake)
            );
        }).start();

    }

    private Map<Integer, Integer> buildWeeklyWakeMap(List<WakeHistoryEntity> entries) {
        Map<Integer, Integer> map = new HashMap<>();

        for (WakeHistoryEntity e : entries) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.date);

            int day = c.get(Calendar.DAY_OF_WEEK); // 1=Sun ... 7=Sat

            Calendar w = Calendar.getInstance();
            w.setTimeInMillis(e.wakeTime);

            int minutes = w.get(Calendar.HOUR_OF_DAY) * 60 + w.get(Calendar.MINUTE);

            map.put(day, minutes);
        }

        return map;
    }


    private void loadWeeklyChart() {
        new Thread(() -> {
            List<WakeHistoryEntity> entries =
                    WakeyDatabase.getInstance(this)
                            .wakeHistoryDao()
                            .getLast7SuccessfulDays();

            Map<Integer, Integer> data = buildWeeklyWakeMap(entries);

            runOnUiThread(() -> weeklyChart.setData(data));
        }).start();
    }


    private void openTimePicker() {
        Calendar now = Calendar.getInstance();

        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    saveWakeTarget(selectedHour, selectedMinute);
                },
                hour,
                minute,
                true
        );

        dialog.show();
    }


    private void saveWakeTarget(int hour, int minute) {
        int totalMinutes = hour * 60 + minute;

        new Thread(() -> {
            WakeTargetEntity target = new WakeTargetEntity();
            target.targetMinutes = totalMinutes;

            WakeyDatabase.getInstance(this)
                    .wakeTargetDao()
                    .save(target);

            runOnUiThread(() ->
                    textWakeTarget.setText(
                            String.format(Locale.getDefault(),
                                    "Target: %02d:%02d", hour, minute)
                    )
            );
        }).start();
    }



}
