package com.wakey.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wakey.R;
import com.wakey.database.AlarmEntity;
import com.wakey.database.WakeyDatabase;
import com.wakey.adapters.AlarmListAdapter;

import java.util.List;

public class AlarmListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        RecyclerView recycler = findViewById(R.id.recyclerAlarms);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        List<AlarmEntity> alarms = WakeyDatabase
                .getInstance(this)
                .alarmDao()
                .getAllAlarms();

        AlarmListAdapter adapter = new AlarmListAdapter(alarms, this);
        recycler.setAdapter(adapter);
    }
}
