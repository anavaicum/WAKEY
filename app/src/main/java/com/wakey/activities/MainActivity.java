package com.wakey.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.wakey.R;
import com.wakey.database.LifeEntity;
import com.wakey.database.WakeyDatabase;

public class MainActivity extends AppCompatActivity {

    // 🔹 Stats
    private TextView textStreakValue;
    private TextView textStreakDelta;
    private TextView textAvgWakeValue;
    private TextView textAvgWakeDelta;

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
        textStreakValue   = findViewById(R.id.textStreakValue);
        textStreakDelta   = findViewById(R.id.textStreakDelta);
        textAvgWakeValue  = findViewById(R.id.textAvgWakeValue);
        textAvgWakeDelta  = findViewById(R.id.textAvgWakeDelta);

        textLivesHearts   = findViewById(R.id.textLivesHearts);
        textProgress      = findViewById(R.id.textProgress);
        progressBar       = findViewById(R.id.progressToNextLife);

        bottomNavigation  = findViewById(R.id.bottomNavigation);

        // ===== BOTTOM NAV =====
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            }

            if (id == R.id.nav_alarms) {
                startActivity(new Intent(this, AlarmListActivity.class));
                return true;
            }

            if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile coming soon 👤", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });



        // ===== DATA =====
        initLifeIfNeeded();
        loadLifeFromDb();
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
}
