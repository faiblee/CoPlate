package com.faible.coplate.dishes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.model.DishIngredientResponse;
import com.faible.coplate.model.DishResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyDishesAdapter extends RecyclerView.Adapter<MyDishesAdapter.ViewHolder> {

    private final List<DishResponse> dishes = new ArrayList<>();

    public void setDishes(List<DishResponse> newDishes) {
        dishes.clear();
        if (newDishes != null) {
            dishes.addAll(newDishes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_dish, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishResponse dish = dishes.get(position);
        holder.dishName.setText(dish.getName() != null ? dish.getName() : "Без названия");
        holder.ingredients.setText(buildIngredientsText(dish.getIngredients()));
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    private String buildIngredientsText(List<DishIngredientResponse> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "Ингредиенты не указаны";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            DishIngredientResponse ingredient = ingredients.get(i);
            if (i > 0) builder.append(", ");
            String name = ingredient.getName() != null ? ingredient.getName() : "ингредиент";
            builder.append(name);
            if (ingredient.getQuantity() > 0) {
                builder.append(" ");
                builder.append(String.format(Locale.US, "%.0f", ingredient.getQuantity()));
            }
            if (ingredient.getUnit() != null && !ingredient.getUnit().trim().isEmpty()) {
                builder.append(" ");
                builder.append(ingredient.getUnit().trim());
            }
        }
        return builder.toString();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dishName;
        final TextView ingredients;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dishName = itemView.findViewById(R.id.myDishName);
            ingredients = itemView.findViewById(R.id.myDishIngredients);
        }
    }
}
