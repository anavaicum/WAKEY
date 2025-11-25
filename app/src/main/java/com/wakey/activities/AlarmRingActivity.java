package com.wakey.activities;

import android.content.Intent;
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

        /** PORNEȘTE SUNETUL ALARMEI */
        if (AlarmRingHolder.currentRingtone == null) {

            Uri alarmSound = Uri.parse(
                    "android.resource://" + getPackageName() + "/" + R.raw.wakey_alarm
            );

            Ringtone ringtone = RingtoneManager.getRingtone(this, alarmSound);
            AlarmRingHolder.currentRingtone = ringtone;

            if (ringtone != null) {
                ringtone.play();
            }
        }

        /** BUTON STOP → oprește alarma și pornește ObjectScanActivity */
        btnStop.setOnClickListener(v -> {
            if (AlarmRingHolder.mediaPlayer != null) {
                AlarmRingHolder.mediaPlayer.stop();
                AlarmRingHolder.mediaPlayer = null;
            }

            Intent i = new Intent(AlarmRingActivity.this, ObjectScanActivity.class);
            i.putExtra("target_object", objectName);
            startActivity(i);

            finish();
        });

        /** Asigurăm afișarea pe lockscreen */
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing() && AlarmRingHolder.mediaPlayer != null) {
            AlarmRingHolder.mediaPlayer.stop();
            AlarmRingHolder.mediaPlayer = null;
        }
    }
}
