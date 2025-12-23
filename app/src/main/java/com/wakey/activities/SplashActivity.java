package com.wakey.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.wakey.R;
import com.wakey.database.WakeyDatabase;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        new Thread(() -> {
            WakeyDatabase db = WakeyDatabase.getInstance(this);
            int selectedCount = db.selectedObjectsDao().getAllSelected().size();

            Intent intent = (selectedCount >= 3)
                    ? new Intent(this, AlarmListActivity.class)
                    : new Intent(this, OnboardingActivity.class);

            runOnUiThread(() -> {
                startActivity(intent);
                finish();
            });
        }).start();

    }
}

