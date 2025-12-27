package com.wakey.activities;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.wakey.R;
import com.wakey.alarm.AlarmRingHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmRingActivity extends AppCompatActivity {


    private TextView timeText;
    private TextView subtitleText;
    private TextView objectNameText;
    private ImageView objectImage;

    private MaterialButton btnScan;
    private MaterialButton btnEmergency;

    private String getFormattedTime() {
        // ca în mock: 07:00 AM
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);

        View root = findViewById(R.id.rootRing);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getInsets(android.view.WindowInsets.Type.statusBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        timeText = findViewById(R.id.textAlarmTime);
        subtitleText = findViewById(R.id.textSubtitle);
        objectNameText = findViewById(R.id.textObjectName);
        objectImage = findViewById(R.id.imageObject);

        btnScan = findViewById(R.id.btnScanObject);
        btnEmergency = findViewById(R.id.btnEmergencyStop);

// UI
        // Data from receiver
        String objectName = getIntent().getStringExtra("object");
        if (objectName == null) objectName = "Unknown";

        int imageRes = getIntent().getIntExtra("image", R.drawable.ic_alarm);

        // UI set
        timeText.setText(getFormattedTime());
        subtitleText.setText("Time to wake up");
        objectNameText.setText(objectName);

        objectImage.setImageResource(imageRes);

        // PLAY ALARM SOUND (păstrăm logica ta)
        if (AlarmRingHolder.currentRingtone == null) {
            Uri alarmSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wakey_alarm);
            Ringtone ringtone = RingtoneManager.getRingtone(this, alarmSound);
            AlarmRingHolder.currentRingtone = ringtone;
            if (ringtone != null) ringtone.play();
        }

        // Scan Object -> deschide camera (NU oprim alarma aici; se oprește când detectezi în ObjectScanActivity)
        String finalObjectName = objectName;
        btnScan.setOnClickListener(v -> {
            Intent i = new Intent(AlarmRingActivity.this, ObjectScanActivity.class);
            i.putExtra("target_object", finalObjectName);
            startActivity(i);
            finish();
        });

        // Emergency Stop -> deocamdată doar există
        btnEmergency.setOnClickListener(v -> {
            // TODO implement later
            android.widget.Toast.makeText(this, "Emergency Stop (TODO)", android.widget.Toast.LENGTH_SHORT).show();
        });

        // lockscreen flags
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // dacă se închide complet ecranul, oprește sunetul
        if (isFinishing() && AlarmRingHolder.currentRingtone != null) {
            AlarmRingHolder.currentRingtone.stop();
            AlarmRingHolder.currentRingtone = null;
        }
    }


}
