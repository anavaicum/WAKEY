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

    // Interfață pentru a notifica activitatea despre selecții
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

        // Setăm imaginea și numele
        holder.textName.setText(item.getName());
        holder.imageObject.setImageResource(item.getImageResId());

        // Actualizăm starea vizuală (bordură, umbră, bifă)
        updateSelectionUI(holder, item.isSelected());

        // Click pe card → schimbăm starea selectată
        holder.itemView.setOnClickListener(v -> {
            item.setSelected(!item.isSelected());
            notifyItemChanged(position);  // actualizează doar elementul modificat
            notifySelectionChanged();
        });
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    // Actualizare UI în funcție de selecție
    private void updateSelectionUI(ObjectViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.checkmark.setVisibility(View.VISIBLE);
            holder.cardView.setCardElevation(12f);
            holder.cardView.setCardBackgroundColor(
                    context.getResources().getColor(R.color.selected_card_bg)
            );
        } else {
            holder.checkmark.setVisibility(View.GONE);
            holder.cardView.setCardElevation(4f);
            holder.cardView.setCardBackgroundColor(
                    context.getResources().getColor(R.color.dark_card)
            );
        }
    }

    // Returnează câte obiecte sunt selectate
    public int getSelectedCount() {
        int count = 0;
        for (ObjectItem item : objectList) {
            if (item.isSelected()) count++;
        }
        return count;
    }

    // Returnează lista obiectelor selectate
    public List<ObjectItem> getSelectedObjects() {
        List<ObjectItem> selected = new ArrayList<>();
        for (ObjectItem item : objectList) {
            if (item.isSelected()) selected.add(item);
        }
        return selected;
    }

    // Anunțăm activitatea că s-a schimbat selecția
    private void notifySelectionChanged() {
        if (listener != null) {
            listener.onSelectionChanged(getSelectedCount());
        }
    }

    // ViewHolder
    public static class ObjectViewHolder extends RecyclerView.ViewHolder {
        ImageView imageObject, checkmark;
        TextView textName;
        CardView cardView;

        public ObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imageObject = itemView.findViewById(R.id.imageObject);
            checkmark = itemView.findViewById(R.id.checkmark);
            textName = itemView.findViewById(R.id.textObjectName);
        }
    }
}
