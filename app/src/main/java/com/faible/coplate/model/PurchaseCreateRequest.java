package com.faible.coplate.model;

public class PurchaseCreateRequest {
    private final String name;
    private final int quantity;
    private final String unit;
    private final String source;
    private final String dishId;

    public PurchaseCreateRequest(String name, int quantity, String unit, String source, String dishId) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.source = source;
        this.dishId = dishId;
    }
}
