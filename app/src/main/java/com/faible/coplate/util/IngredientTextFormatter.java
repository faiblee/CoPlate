package com.faible.coplate.util;

import com.faible.coplate.model.DishIngredientResponse;

import java.util.List;
import java.util.Locale;

public final class IngredientTextFormatter {

    private IngredientTextFormatter() {}

    /**
     * Не показываем тривиальное «1 шт» / «1 pc» — только название ингредиента.
     */
    public static boolean shouldOmitQuantityAndUnit(double quantity, String unit) {
        if (Math.abs(quantity - 1.0) >= 1e-6) {
            return false;
        }
        if (unit == null || unit.trim().isEmpty()) {
            return false;
        }
        String u = unit.trim();
        return u.equalsIgnoreCase("шт")
                || u.equalsIgnoreCase("штук")
                || u.equalsIgnoreCase("шт.")
                || u.equalsIgnoreCase("pc")
                || u.equalsIgnoreCase("pcs");
    }

    public static String fromIngredients(List<DishIngredientResponse> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            DishIngredientResponse ing = ingredients.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            String name = ing.getName() != null ? ing.getName() : "";
            builder.append(name);
            if (shouldOmitQuantityAndUnit(ing.getQuantity(), ing.getUnit())) {
                continue;
            }
            if (ing.getQuantity() > 0) {
                builder.append(" ");
                if (Math.abs(ing.getQuantity() - Math.rint(ing.getQuantity())) < 1e-6) {
                    builder.append(String.format(Locale.US, "%.0f", ing.getQuantity()));
                } else {
                    builder.append(String.format(Locale.US, "%s", ing.getQuantity()));
                }
            }
            if (ing.getUnit() != null && !ing.getUnit().trim().isEmpty()) {
                builder.append(" ").append(ing.getUnit().trim());
            }
        }
        return builder.toString();
    }

    /** По одному ингредиенту на строку — для экрана подробностей блюда. */
    public static String ingredientsAsBulletLines(List<DishIngredientResponse> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (DishIngredientResponse ing : ingredients) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append("• ");
            String name = ing.getName() != null ? ing.getName() : "";
            builder.append(name);
            if (shouldOmitQuantityAndUnit(ing.getQuantity(), ing.getUnit())) {
                continue;
            }
            if (ing.getQuantity() > 0) {
                builder.append(" — ");
                if (Math.abs(ing.getQuantity() - Math.rint(ing.getQuantity())) < 1e-6) {
                    builder.append(String.format(Locale.US, "%.0f", ing.getQuantity()));
                } else {
                    builder.append(String.format(Locale.US, "%s", ing.getQuantity()));
                }
            }
            if (ing.getUnit() != null && !ing.getUnit().trim().isEmpty()) {
                builder.append(" ").append(ing.getUnit().trim());
            }
        }
        return builder.toString();
    }
}
