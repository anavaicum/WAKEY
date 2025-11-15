package com.wakey.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fortam tematica sa fie mereu darck
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        // Citim flagul de prima rulare
        SharedPreferences prefs = getSharedPreferences("wakey_prefs", MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);

//        Intent intent;
//        if (isFirstRun) {
//            // Prima deschidere → mergem în Onboarding
//            intent = new Intent(this, OnboardingActivity.class);
//        } else {
//            // A doua oară → mergem direct în Main
//            intent = new Intent(this, MainActivity.class);
//        }

        // Temporar: mergem mereu la Onboarding pentru testare
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();

        startActivity(intent);
        finish();
    }
}
