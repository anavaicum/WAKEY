package com.wakey.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.wakey.R;
import com.wakey.alarm.AlarmScheduler;
import com.wakey.database.AlarmEntity;
import com.wakey.database.WakeyDatabase;

import java.util.ArrayList;
import java.util.List;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

    private final List<AlarmEntity> alarms = new ArrayList<>();
    private final Context context;

    public interface OnEditClickListener {
        void onEditClick(AlarmEntity alarm);
    }
    private final OnEditClickListener editListener;

    public AlarmListAdapter(List<AlarmEntity> alarms, Context context, OnEditClickListener editListener) {
        if (alarms != null) this.alarms.addAll(alarms);
        this.context = context;
        this.editListener = editListener;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlarmEntity alarm = alarms.get(position);

        holder.time.setText(String.format("%02d:%02d", alarm.hour, alarm.minute));
        holder.daysText.setText(formatDays(alarm.repeatDaysMask));

        // IMPORTANT: evităm trigger-ul listener-ului când setăm programatic
        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(alarm.isActive);

        applyActiveStyle(holder, alarm.isActive);

        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            WakeyDatabase db = WakeyDatabase.getInstance(context);

            // update model local
            alarm.isActive = isChecked;
            db.alarmDao().setActive(alarm.id, isChecked);

            if (isChecked) {
                // schedule + next trigger
                AlarmScheduler.schedule(context, alarm);
                long next = AlarmScheduler.computeNextTriggerTimeMillis(alarm);
                db.alarmDao().setNextTriggerAt(alarm.id, next);
            } else {
                // cancel
                AlarmScheduler.cancel(context, alarm.id);
            }

            applyActiveStyle(holder, isChecked);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(alarm);
        });

    }

    private void applyActiveStyle(ViewHolder holder, boolean active) {
        float a = active ? 1.0f : 0.45f;
        holder.time.setAlpha(a);
        holder.daysText.setAlpha(active ? 0.75f : 0.35f);
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public AlarmEntity getItem(int position) {
        return alarms.get(position);
    }

    public void removeAt(int position) {
        alarms.remove(position);
        notifyItemRemoved(position);
    }

    public void updateData(List<AlarmEntity> newList) {
        alarms.clear();
        if (newList != null) alarms.addAll(newList);
        notifyDataSetChanged();
    }

    private String formatDays(int mask) {
        if (mask == 0) return "Once";

        // bit0=Mon ... bit6=Sun
        String[] names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if ((mask & (1 << i)) != 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(names[i]);
            }
        }
        return sb.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        TextView daysText;
        SwitchCompat switchActive;
        ImageView btnEdit;


        ViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.alarmTime);
            daysText = itemView.findViewById(R.id.daysText);
            switchActive = itemView.findViewById(R.id.switchActive);
            btnEdit = itemView.findViewById(R.id.btnEditAlarm);

        }
    }
}
