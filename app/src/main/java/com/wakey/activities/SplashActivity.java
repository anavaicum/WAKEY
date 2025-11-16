package com.wakey.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.wakey.R;


public class SplashActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            setContentView(R.layout.activity_splash);   // <-- AI NEVOIE DE ASTA

            SharedPreferences prefs = getSharedPreferences("wakey_prefs", MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

            Intent intent = new Intent(this, OnboardingActivity.class);

            startActivity(intent);
            finish();
        }
    }


