package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class DishIngredientResponse {
    @SerializedName(value = "name", alternate = {"ingredient_name", "ingredientName", "title"})
    private String name;
    @SerializedName(value = "quantity", alternate = {"qty", "amount"})
    private Double quantity;
    @SerializedName(value = "unit", alternate = {"measure", "measurement"})
    private String unit;

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
