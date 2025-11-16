package com.wakey.activities;

import com.wakey.R;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class AlarmRingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ring);

        String objectName = getIntent().getStringExtra("object");
        int imageRes = getIntent().getIntExtra("image", -1);

        TextView text = findViewById(R.id.textObjectName);
        ImageView img = findViewById(R.id.imageObject);

        text.setText("Find this object: " + objectName);

        if (imageRes != -1) {
            img.setImageResource(imageRes);
        }
    }
}
