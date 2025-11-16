package com.wakey.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wakey.R;
import com.wakey.database.SelectedObjectEntity;
import com.wakey.database.WakeyDatabase;

import java.util.List;

import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    private TextView textSelectedObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permisiune notificări (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        textSelectedObjects = findViewById(R.id.textSelectedObjects);
        Button btnAlarm = findViewById(R.id.openAlarmBtn);

        // Obținem selecțiile salvate din DB
        List<SelectedObjectEntity> savedObjects = WakeyDatabase.getInstance(this)
                .selectedObjectsDao()
                .getAllSelected();

        StringBuilder sb = new StringBuilder("Your selected objects:\n\n");
        for (SelectedObjectEntity obj : savedObjects) {
            sb.append("• ").append(obj.getName()).append("\n");
        }

        textSelectedObjects.setText(sb.toString());

        // Navigare către AlarmActivity
        btnAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
            startActivity(intent);
        });
    }
}
