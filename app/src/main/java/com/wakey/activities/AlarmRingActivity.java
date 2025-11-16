package com.wakey.activities;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wakey.R;
import com.wakey.alarm.AlarmRingHolder;

public class AlarmRingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);

        String objectName = getIntent().getStringExtra("object");
        int imageRes = getIntent().getIntExtra("image", -1);

        TextView text = findViewById(R.id.textObjectName);
        ImageView img = findViewById(R.id.imageObject);
        Button btnStop = findViewById(R.id.btnStopAlarm);

        text.setText("Find this object: " + objectName);

        if (imageRes != -1) {
            img.setImageResource(imageRes);
        }

        // === PORNEȘTE SUNETUL ALARMEI ===
        if (AlarmRingHolder.currentRingtone == null) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
            AlarmRingHolder.currentRingtone = ringtone;

            if (ringtone != null) {
                ringtone.play();
            }
        }

        //  BUTON PENTRU OPRIREA ALARMEI
        btnStop.setOnClickListener(v -> {
            if (AlarmRingHolder.currentRingtone != null) {
                AlarmRingHolder.currentRingtone.stop();
                AlarmRingHolder.currentRingtone = null;
            }
            finish();
        });

        // Ne asiguram ca alarma apare si pe lock screen
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Siguranță suplimentară
        if (isFinishing() && AlarmRingHolder.currentRingtone != null) {
            AlarmRingHolder.currentRingtone.stop();
            AlarmRingHolder.currentRingtone = null;
        }
    }
}
