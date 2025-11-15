package com.wakey.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wakey.R;
import com.wakey.adapters.ObjectAdapter;
import com.wakey.models.ObjectItem;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ObjectAdapter adapter;
    private Button btnContinue;
    private TextView tvCounter;
    private List<ObjectItem> objectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        recyclerView = findViewById(R.id.recyclerViewObjects);
        btnContinue = findViewById(R.id.btnContinue);
        tvCounter = findViewById(R.id.tvSelectedCount);


        setupObjectList();
        setupRecyclerView();

        btnContinue.setEnabled(false);
        btnContinue.setOnClickListener(v -> {
            // TODO: Salvăm selecțiile în DB (Room)
            Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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
            // Actualizăm contorul vizual
            tvCounter.setText(selectedCount + " / 3 selected");

            // Activăm butonul doar dacă sunt 3+
            btnContinue.setEnabled(selectedCount >= 3);

            // Schimbăm culoarea butonului vizual în funcție de stare
            int color = getColor(selectedCount >= 3 ? R.color.accent_blue : R.color.gray_disabled);
            btnContinue.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

        });

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
}
