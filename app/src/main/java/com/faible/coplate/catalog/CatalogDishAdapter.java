package com.faible.coplate.catalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.util.IngredientTextFormatter;

import java.util.ArrayList;
import java.util.List;

public class CatalogDishAdapter extends RecyclerView.Adapter<CatalogDishAdapter.ViewHolder> {

    public interface Listener {
        void onDishTitleClick(@NonNull DishResponse dish);

        void onAddToPlanClick(@NonNull DishResponse dish);
    }

    private final List<DishResponse> items = new ArrayList<>();
    private final Listener listener;

    public CatalogDishAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    public void setItems(@NonNull List<DishResponse> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalog_dish, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishResponse dish = items.get(position);
        String name = dish.getName();
        holder.nameView.setText(name != null && !name.trim().isEmpty() ? name : "?");

        String ingLine = IngredientTextFormatter.fromIngredients(dish.getIngredients()).trim();
        if (ingLine.isEmpty()) {
            holder.ingredientsView.setVisibility(View.GONE);
        } else {
            holder.ingredientsView.setVisibility(View.VISIBLE);
            holder.ingredientsView.setText(ingLine);
        }

        holder.nameView.setOnClickListener(v -> listener.onDishTitleClick(dish));
        holder.addButton.setOnClickListener(v -> listener.onAddToPlanClick(dish));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView ingredientsView;
        final Button addButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.catalogDishName);
            ingredientsView = itemView.findViewById(R.id.catalogDishIngredients);
            addButton = itemView.findViewById(R.id.catalogAddToPlanButton);
        }
    }
}
