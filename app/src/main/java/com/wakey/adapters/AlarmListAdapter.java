package com.wakey.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wakey.R;
import com.wakey.database.AlarmEntity;
import com.wakey.database.WakeyDatabase;

import java.util.List;
public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {
    private final List<AlarmEntity> alarms;
    private final Context context;

    public AlarmListAdapter(List<AlarmEntity> alarms, Context context) {
        this.alarms = alarms;
        this.context = context;
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
        holder.time.setText(String.format("Time: %02d:%02d", alarm.hour, alarm.minute));

        holder.deleteBtn.setOnClickListener(v -> {
            com.wakey.alarm.AlarmScheduler.cancel(context, alarm.id);
            WakeyDatabase.getInstance(context).alarmDao().deleteAlarm(alarm);
            alarms.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.alarmTime);
            deleteBtn = itemView.findViewById(R.id.deleteAlarmBtn);
        }
    }

    public void updateData(List<AlarmEntity> newList) {
        alarms.clear();
        alarms.addAll(newList);
        notifyDataSetChanged();
    }

}
