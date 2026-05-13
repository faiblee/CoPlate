package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class DishIngredientResponse {
    @SerializedName(value = "name", alternate = {"ingredient_name", "ingredientName", "title"})
    private String name;
    @SerializedName(value = "quantity", alternate = {"qty", "amount"})
    private double quantity;
    @SerializedName(value = "unit", alternate = {"measure", "measurement"})
    private String unit;

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
