package com.faible.coplate.model;

public class PurchaseResponse {
    private String id;
    private String name;
    private int quantity;
    private String unit;
    private boolean isBought;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isBought() {
        return isBought;
    }
}
