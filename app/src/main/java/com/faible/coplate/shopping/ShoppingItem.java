package com.faible.coplate.shopping;

public class ShoppingItem {
    private String id;
    private String name;
    private boolean isChecked;
    private int quantity;
    private String unit;

    public ShoppingItem(String name) {
        this(null, name, false, 1, "шт");
    }

    public ShoppingItem(String id, String name, boolean isChecked, int quantity, String unit) {
        this.id = id;
        this.name = name;
        this.isChecked = isChecked;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
