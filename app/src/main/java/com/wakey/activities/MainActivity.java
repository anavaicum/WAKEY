package com.wakey.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.wakey.R;
import com.wakey.database.SelectedObjectEntity;
import com.wakey.database.WakeyDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textSelectedObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textSelectedObjects = findViewById(R.id.textSelectedObjects);

        // Obținem selecțiile salvate din DB
        List<SelectedObjectEntity> savedObjects = WakeyDatabase.getInstance(this)
                .selectedObjectsDao()
                .getAllSelected();

        StringBuilder sb = new StringBuilder("Your selected objects:\n\n");
        for (SelectedObjectEntity obj : savedObjects) {
            sb.append("• ").append(obj.getName()).append("\n");
        }

        textSelectedObjects.setText(sb.toString());
    }
}
