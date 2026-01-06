package com.wakey.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wakey.R;
import com.wakey.adapters.AlarmListAdapter;
import com.wakey.alarm.AlarmScheduler;
import com.wakey.database.AlarmEntity;
import com.wakey.database.SelectedObjectEntity;
import com.wakey.database.WakeyDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import androidx.recyclerview.widget.ItemTouchHelper;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;



public class AlarmListActivity extends AppCompatActivity {

    private AlarmListAdapter adapter;

    private View emptyState;
    private RecyclerView recycler;
    private Button btnEmptyAdd;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAlarms);

        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(AlarmListActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        final int initialTopPadding = toolbar.getPaddingTop();
        toolbar.setOnApplyWindowInsetsListener((v, insets) -> {
            int topInset = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    topInset = insets.getInsets(WindowInsets.Type.statusBars()).top;
                }
            }
            v.setPadding(v.getPaddingLeft(), initialTopPadding + topInset, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        toolbar.requestApplyInsets();

        recycler = findViewById(R.id.recyclerAlarms);
        emptyState = findViewById(R.id.emptyState);
        btnEmptyAdd = findViewById(R.id.btnEmptyAdd);

        btnEmptyAdd.setOnClickListener(v -> openAddAlarmDialog());


        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(AlarmListActivity.this, OnboardingActivity.class));
        });



        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlarmListAdapter(loadAlarms(), this, alarm -> {
            openEditAlarmDialog(alarm);
        });

        recycler.setAdapter(adapter);

        updateEmptyState();

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {

            private final Paint paint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                AlarmEntity alarm = adapter.getItem(pos);

                com.wakey.alarm.AlarmScheduler.cancel(AlarmListActivity.this, alarm.id);
                WakeyDatabase.getInstance(getApplicationContext()).alarmDao().deleteAlarm(alarm);
                adapter.removeAt(pos);

                Toast.makeText(AlarmListActivity.this, "Alarm deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                int itemTop = itemView.getTop();
                int itemBottom = itemView.getBottom();
                int itemLeft = itemView.getLeft();
                int itemRight = itemView.getRight();

                // roșu (folosește o culoare existentă dacă ai; altfel alege un hex)
                paint.setColor(0xFFD32F2F);

                Drawable trash = ContextCompat.getDrawable(AlarmListActivity.this, R.drawable.ic_trash);
                if (trash != null) {
                    int iconSize = (int) (24 * recyclerView.getResources().getDisplayMetrics().density);
                    int margin = (int) (24 * recyclerView.getResources().getDisplayMetrics().density);

                    int iconTop = itemTop + (itemBottom - itemTop - iconSize) / 2;
                    int iconBottom = iconTop + iconSize;

                    if (dX > 0) {
                        // swipe RIGHT: background pe stânga
                        c.drawRect((float) itemLeft, (float) itemTop, itemLeft + dX, (float) itemBottom, paint);

                        int iconLeft = itemLeft + margin;
                        int iconRight = iconLeft + iconSize;
                        trash.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        trash.draw(c);

                    } else if (dX < 0) {
                        // swipe LEFT: background pe dreapta
                        c.drawRect(itemRight + dX, (float) itemTop, (float) itemRight, (float) itemBottom, paint);

                        int iconRight = itemRight - margin;
                        int iconLeft = iconRight - iconSize;
                        trash.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        trash.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

        helper.attachToRecyclerView(recycler);


        FloatingActionButton fab = findViewById(R.id.fabAddAlarm);
        fab.setOnClickListener(v -> openAddAlarmDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.updateData(loadAlarms());
        updateEmptyState();
    }

    private List<AlarmEntity> loadAlarms() {
        return WakeyDatabase.getInstance(this).alarmDao().getAllAlarms();
    }

    private void openAddAlarmDialog() {
        String randomObject = getRandomObject();
        if (randomObject == null) {
            Toast.makeText(this, "Selectează măcar un obiect înainte.", Toast.LENGTH_LONG).show();
            // trimite userul direct la onboarding
            startActivity(new Intent(this, OnboardingActivity.class));
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_alarm, null);

        // IMPORTANT: TimePicker din dialog_add_alarm.xml
        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        Button btnSave = view.findViewById(R.id.btnSaveAlarm);

        Chip chipMon = view.findViewById(R.id.chipMon);
        Chip chipTue = view.findViewById(R.id.chipTue);
        Chip chipWed = view.findViewById(R.id.chipWed);
        Chip chipThu = view.findViewById(R.id.chipThu);
        Chip chipFri = view.findViewById(R.id.chipFri);
        Chip chipSat = view.findViewById(R.id.chipSat);
        Chip chipSun = view.findViewById(R.id.chipSun);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        btnSave.setOnClickListener(v -> {

            // 1) ora/minut direct din TimePicker
            int pickedHour;
            int pickedMinute;

            if (android.os.Build.VERSION.SDK_INT >= 23) {
                pickedHour = timePicker.getHour();
                pickedMinute = timePicker.getMinute();
            } else {
                pickedHour = timePicker.getCurrentHour();
                pickedMinute = timePicker.getCurrentMinute();
            }

            // 2) mask zile
            int mask = 0;
            if (chipMon.isChecked()) mask |= (1 << 0);
            if (chipTue.isChecked()) mask |= (1 << 1);
            if (chipWed.isChecked()) mask |= (1 << 2);
            if (chipThu.isChecked()) mask |= (1 << 3);
            if (chipFri.isChecked()) mask |= (1 << 4);
            if (chipSat.isChecked()) mask |= (1 << 5);
            if (chipSun.isChecked()) mask |= (1 << 6);

            // dacă nu selectează zile: tratăm ca “One time” (azi sau mâine)
            // (dacă tu vrei obligatoriu repeat, schimbăm cu Toast + return)
            AlarmEntity alarm = new AlarmEntity();
            alarm.hour = pickedHour;
            alarm.minute = pickedMinute;
            alarm.selectedObjectName = randomObject;
            alarm.isActive = true;
            alarm.repeatDaysMask = mask;

            long id = WakeyDatabase.getInstance(this).alarmDao().insertAlarm(alarm);
            alarm.id = (int) id;

            long next = AlarmScheduler.computeNextTriggerTimeMillis(alarm);
            WakeyDatabase.getInstance(this).alarmDao().setNextTriggerAt(alarm.id, next);

            if (!ensureExactAlarmPermission()) {
                return; // nu închidem dialogul
            }

            AlarmScheduler.schedule(this, alarm);

            adapter.updateData(loadAlarms());
            dlg.dismiss();
            updateEmptyState();

        });

        dlg.show();

        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }


    private boolean ensureExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
            if (am != null && !am.canScheduleExactAlarms()) {
                android.content.Intent intent =
                        new android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                android.widget.Toast.makeText(this,
                        "Te rog activează Exact alarms și încearcă din nou.",
                        android.widget.Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private void openEditAlarmDialog(AlarmEntity alarm) {

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_alarm, null);

        TimePicker timePicker = view.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        // prefill time
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(alarm.hour);
            timePicker.setMinute(alarm.minute);
        } else {
            timePicker.setCurrentHour(alarm.hour);
            timePicker.setCurrentMinute(alarm.minute);
        }

        Button btnSave = view.findViewById(R.id.btnSaveAlarm);
        btnSave.setText("Save"); // sau "Update"

        Chip chipMon = view.findViewById(R.id.chipMon);
        Chip chipTue = view.findViewById(R.id.chipTue);
        Chip chipWed = view.findViewById(R.id.chipWed);
        Chip chipThu = view.findViewById(R.id.chipThu);
        Chip chipFri = view.findViewById(R.id.chipFri);
        Chip chipSat = view.findViewById(R.id.chipSat);
        Chip chipSun = view.findViewById(R.id.chipSun);

        // prefill days from mask
        int mask = alarm.repeatDaysMask;
        chipMon.setChecked((mask & (1 << 0)) != 0);
        chipTue.setChecked((mask & (1 << 1)) != 0);
        chipWed.setChecked((mask & (1 << 2)) != 0);
        chipThu.setChecked((mask & (1 << 3)) != 0);
        chipFri.setChecked((mask & (1 << 4)) != 0);
        chipSat.setChecked((mask & (1 << 5)) != 0);
        chipSun.setChecked((mask & (1 << 6)) != 0);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        btnSave.setOnClickListener(v -> {

            int pickedHour, pickedMinute;
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                pickedHour = timePicker.getHour();
                pickedMinute = timePicker.getMinute();
            } else {
                pickedHour = timePicker.getCurrentHour();
                pickedMinute = timePicker.getCurrentMinute();
            }

            int newMask = 0;
            if (chipMon.isChecked()) newMask |= (1 << 0);
            if (chipTue.isChecked()) newMask |= (1 << 1);
            if (chipWed.isChecked()) newMask |= (1 << 2);
            if (chipThu.isChecked()) newMask |= (1 << 3);
            if (chipFri.isChecked()) newMask |= (1 << 4);
            if (chipSat.isChecked()) newMask |= (1 << 5);
            if (chipSun.isChecked()) newMask |= (1 << 6);

            // update entity
            alarm.hour = pickedHour;
            alarm.minute = pickedMinute;
            alarm.repeatDaysMask = newMask;

            // cancel old schedule (dacă era activă)
            if (alarm.isActive) {
                AlarmScheduler.cancel(this, alarm.id);
            }

            // recompute next
            long next = AlarmScheduler.computeNextTriggerTimeMillis(alarm);
            WakeyDatabase.getInstance(this).alarmDao().setNextTriggerAt(alarm.id, next);

            // update DB
            WakeyDatabase.getInstance(this).alarmDao().updateAlarm(alarm);

            // ensure permission + reschedule
            if (alarm.isActive) {
                if (!ensureExactAlarmPermission()) return;
                AlarmScheduler.schedule(this, alarm);
            }

            adapter.updateData(loadAlarms());
            dlg.dismiss();
            updateEmptyState();

        });

        dlg.show();
        if (dlg.getWindow() != null) {
            dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private String getRandomObject() {
        List<SelectedObjectEntity> items = WakeyDatabase
                .getInstance(this)
                .selectedObjectsDao()
                .getAllSelected();

        if (items == null || items.isEmpty()) return null;
        return items.get(new Random().nextInt(items.size())).getName();
    }

    private void updateEmptyState() {
        boolean isEmpty = (adapter == null || adapter.getItemCount() == 0);

        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

}
