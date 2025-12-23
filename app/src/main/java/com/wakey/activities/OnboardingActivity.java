package com.wakey.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
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
    private ObjectAdapter adapter;
    private Button btnContinue;
    private TextView tvCounter;
    private List<ObjectItem> objectList;

    private volatile boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        recyclerView = findViewById(R.id.recyclerViewObjects);
        btnContinue = findViewById(R.id.btnContinue);
        tvCounter = findViewById(R.id.tvSelectedCount);

        setupObjectList();
        setupRecyclerView();

        // inițial dezactivat până la 3 selecții
        btnContinue.setEnabled(false);

        btnContinue.setOnClickListener(v -> {
            if (isSaving) return;

            List<ObjectItem> selected = adapter.getSelectedObjects();
            if (selected == null || selected.size() < 3) {
                Toast.makeText(this, "Selectează cel puțin 3 obiecte.", Toast.LENGTH_SHORT).show();
                return;
            }

            isSaving = true;
            btnContinue.setEnabled(false);
            btnContinue.setText("Saving...");

            new Thread(() -> {
                try {
                    WakeyDatabase db = WakeyDatabase.getInstance(getApplicationContext());

                    // 1) clear
                    db.selectedObjectsDao().clearAll();

                    // 2) insert
                    List<SelectedObjectEntity> entities = new ArrayList<>();
                    for (ObjectItem item : selected) {
                        entities.add(new SelectedObjectEntity(
                                item.getName(),
                                String.valueOf(item.getImageResId())
                        ));
                    }
                    db.selectedObjectsDao().insertAll(entities);

                    // 3) go to alarms list
                    runOnUiThread(() -> {
                        Intent intent = new Intent(OnboardingActivity.this, AlarmListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        isSaving = false;
                        btnContinue.setText("Continue");
                        // recalculăm enable după câte sunt selectate acum
                        int countNow = adapter.getSelectedObjects() != null ? adapter.getSelectedObjects().size() : 0;
                        btnContinue.setEnabled(countNow >= 3);

                        Toast.makeText(this, "Eroare la salvare. Încearcă din nou.", Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }

    private void setupObjectList() {
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
    }

    private void setupRecyclerView() {
        adapter = new ObjectAdapter(this, objectList, selectedCount -> {
            tvCounter.setText(selectedCount + " / 3 selected");

            // dacă suntem în saving, nu lăsăm enable/disable să se schimbe
            if (isSaving) return;

            boolean ok = selectedCount >= 3;
            btnContinue.setEnabled(ok);

            int color = getColor(ok ? R.color.accent_blue : R.color.gray_disabled);
            btnContinue.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}
