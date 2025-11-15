package com.wakey.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wakey.R;
import com.wakey.adapters.ObjectAdapter;
import com.wakey.database.SelectedObjectEntity;
import com.wakey.database.WakeyDatabase;
import com.wakey.models.ObjectItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnContinue;
    private ObjectAdapter adapter;
    private List<ObjectItem> objectList;
    private WakeyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        recyclerView = findViewById(R.id.recyclerViewObjects);
        btnContinue = findViewById(R.id.btnContinue);

        // Inițializăm baza de date
        db = WakeyDatabase.getInstance(this);

        // Inițializare listă obiecte
        objectList = new ArrayList<>();
        objectList.add(new ObjectItem("Toothbrush", R.drawable.toothbrush));
        objectList.add(new ObjectItem("Toilet", R.drawable.toilet));
        objectList.add(new ObjectItem("Cup", R.drawable.cup));
        objectList.add(new ObjectItem("Fork", R.drawable.fork));
        objectList.add(new ObjectItem("Washing Machine", R.drawable.washing_machine));
        objectList.add(new ObjectItem("Book", R.drawable.book));
        objectList.add(new ObjectItem("Sneaker", R.drawable.sneaker));
        objectList.add(new ObjectItem("Chair", R.drawable.chair));
        objectList.add(new ObjectItem("Apple", R.drawable.apple));

        adapter = new ObjectAdapter(this, objectList, selectedCount -> {
            Button continueButton = findViewById(R.id.btnContinue);
            continueButton.setEnabled(selectedCount >= 3);
        });


        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        btnContinue.setOnClickListener(v -> saveSelections());
    }

    private void saveSelections() {
        List<ObjectItem> selected = adapter.getSelectedObjects();
        if (selected.size() < 3) {
            Toast.makeText(this, "Select at least 3 objects!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Curățăm baza de date înainte de salvare
        db.selectedObjectsDao().clearAll();

        // Convertim obiectele selectate în entități DB
        List<SelectedObjectEntity> entities = new ArrayList<>();
        for (ObjectItem item : selected) {
            entities.add(new SelectedObjectEntity(item.getName(), String.valueOf(item.getImageResId())));
        }

        db.selectedObjectsDao().insertAll(entities);

        // Marcăm onboarding-ul ca terminat
        SharedPreferences prefs = getSharedPreferences("wakey_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstRun", false);
        editor.apply();

        Toast.makeText(this, "Selection saved successfully!", Toast.LENGTH_SHORT).show();

        // TODO: Launch MainActivity (în etapa următoare)
        // startActivity(new Intent(this, MainActivity.class));
        // finish();
    }
}
