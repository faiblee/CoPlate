package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

/**
 * Строка ингредиента в {@link DishResponse#getIngredients()} (сервер: Integer quantity, Long id).
 */
public class DishIngredientResponse {
    private Long id;
    @SerializedName(value = "name", alternate = {"ingredient_name", "ingredientName", "title"})
    private String name;
    /** На сервере {@code Integer}; Gson приводит к Double. */
    @SerializedName(value = "quantity", alternate = {"qty", "amount"})
    private Double quantity;
    @SerializedName(value = "unit", alternate = {"measure", "measurement"})
    private String unit;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity != null ? quantity : 0.0;
    }

    public String getUnit() {
        return unit;
    }
}
