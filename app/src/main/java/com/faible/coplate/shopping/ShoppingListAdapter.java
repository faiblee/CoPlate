package com.faible.coplate.shopping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.faible.coplate.R;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<ShoppingItem> itemList;
    private OnItemClickListener listener;
    private boolean isEditMode = false;

    // Интерфейс для обработки кликов во фрагменте
    public interface OnItemClickListener {
        void onCheckClick(int position);
        void onDeleteClick(int position);
    }

    public ShoppingListAdapter(List<ShoppingItem> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    // Метод для включения/выключения режима редактирования
    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged(); // Перерисовываем список, чтобы показать/скрыть кнопки удаления
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = itemList.get(position);
        holder.itemText.setText(item.getName());

        // ... (код отрисовки состояния checked/unchecked) ...
        if (item.isChecked()) {
            holder.itemCheckbox.setColorFilter(0xFF4CAF50);
            holder.itemText.setPaintFlags(holder.itemText.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemText.setTextColor(0xFF9E9E9E);
        } else {
            holder.itemCheckbox.setColorFilter(0xFF1F2A24);
            holder.itemText.setPaintFlags(holder.itemText.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemText.setTextColor(0xFF1F2A24);
        }

        holder.deleteButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        holder.itemCheckbox.setEnabled(!isEditMode);
        holder.itemCheckbox.setAlpha(isEditMode ? 0.5f : 1.0f);

        // ИСПРАВЛЕНИЕ ЗДЕСЬ:
        // Используем lambda с явным получением позиции через getAdapterPosition()
        // или просто используем аргумент position, но убедившись, что он не меняется.

        holder.deleteButton.setOnClickListener(v -> {
            // Самый надежный способ получить актуальную позицию в момент клика
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClick(currentPosition);
            }
        });

        holder.itemCheckbox.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && !isEditMode && listener != null) {
                listener.onCheckClick(currentPosition);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && !isEditMode && listener != null) {
                listener.onCheckClick(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Метод для удаления элемента из списка
    public void removeItem(int position) {
        itemList.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageButton itemCheckbox;
        TextView itemText;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckbox = itemView.findViewById(R.id.itemCheckbox);
            itemText = itemView.findViewById(R.id.itemText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
