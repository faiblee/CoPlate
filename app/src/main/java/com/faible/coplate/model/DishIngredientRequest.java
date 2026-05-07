package com.faible.coplate.model;

public class DishIngredientRequest {
    private final String name;
    private final int quantity;
    private final String unit;

    public DishIngredientRequest(String name, int quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
}
