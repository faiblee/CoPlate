package com.faible.coplate.dishes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.faible.coplate.R;
import com.faible.coplate.model.DishResponse;
import com.faible.coplate.util.IngredientTextFormatter;

import java.util.ArrayList;
import java.util.List;

public class MyDishesAdapter extends RecyclerView.Adapter<MyDishesAdapter.ViewHolder> {

    public interface OnMyDishClickListener {
        void onMyDishClick(@NonNull DishResponse dish);
    }

    private final List<DishResponse> dishes = new ArrayList<>();
    @Nullable
    private final OnMyDishClickListener clickListener;

    public MyDishesAdapter(@Nullable OnMyDishClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setDishes(List<DishResponse> newDishes) {
        dishes.clear();
        if (newDishes != null) {
            dishes.addAll(newDishes);
        }
        notifyDataSetChanged();
    }

    public void clearDishes() {
        dishes.clear();
        notifyDataSetChanged();
    }

    public void appendDish(@NonNull DishResponse dish) {
        dishes.add(dish);
        notifyItemInserted(dishes.size() - 1);
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
        holder.ingredients.setText(formatIngredients(dish));
        holder.itemView.setOnClickListener(v -> {
            if (clickListener == null) {
                return;
            }
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || pos >= dishes.size()) {
                return;
            }
            clickListener.onMyDishClick(dishes.get(pos));
        });
    }

    @Override
    public int getItemCount() {
        return dishes.size();
    }

    private String formatIngredients(DishResponse dish) {
        return IngredientTextFormatter.fromIngredients(dish.getIngredients()).trim();
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
