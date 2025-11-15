package com.wakey.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.wakey.R;
import com.wakey.models.ObjectItem;

import java.util.ArrayList;
import java.util.List;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ObjectViewHolder> {

    private final Context context;
    private final List<ObjectItem> objectList;
    private final OnSelectionChangedListener listener;

    // 👇 Interfață pentru comunicare cu activitatea
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    public ObjectAdapter(Context context, List<ObjectItem> objectList, OnSelectionChangedListener listener) {
        this.context = context;
        this.objectList = objectList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_object, parent, false);
        return new ObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectViewHolder holder, int position) {
        ObjectItem item = objectList.get(position);

        holder.textName.setText(item.getName());
        holder.imageView.setImageResource(item.getImageResId());

        // 🎨 Aplicăm vizual starea selectată
        holder.itemView.setSelected(item.isSelected());
        float elevation = item.isSelected() ? 12f : 4f;
        holder.itemView.setElevation(elevation);

        // 🔄 Gestionăm click-ul și selecția
        holder.itemView.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            notifyItemChanged(position);
            notifySelectionChanged();
        });
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    // ✅ Metodă care anunță activitatea câte obiecte sunt selectate
    private void notifySelectionChanged() {
        if (listener != null) {
            int selectedCount = getSelectedCount();
            listener.onSelectionChanged(selectedCount);
        }
    }

    // ✅ Numărăm câte obiecte sunt selectate
    public int getSelectedCount() {
        int count = 0;
        for (ObjectItem item : objectList) {
            if (item.isSelected()) count++;
        }
        return count;
    }

    // ✅ Returnăm lista obiectelor selectate
    public List<ObjectItem> getSelectedObjects() {
        List<ObjectItem> selected = new ArrayList<>();
        for (ObjectItem item : objectList) {
            if (item.isSelected()) selected.add(item);
        }
        return selected;
    }

    // 🧱 ViewHolder
    public static class ObjectViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textName;

        public ObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageObject);
            textName = itemView.findViewById(R.id.textObjectName);
        }
    }
}
