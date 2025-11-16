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

            // Setăm tema dark
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            // Verificăm dacă e prima rulare
            SharedPreferences prefs = getSharedPreferences("wakey_prefs", MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

            Intent intent;

            if (isFirstRun) {
                // Prima deschidere → Onboarding
                intent = new Intent(this, OnboardingActivity.class);

                // Salvăm că onboarding a fost făcut
                prefs.edit().putBoolean("isFirstRun", false).apply();

            } else {
                // Nu este prima deschidere → MainActivity
                intent = new Intent(this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }
    }


