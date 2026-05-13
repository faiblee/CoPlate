package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class MealPlanAddRequest {
    @SerializedName("dishId")
    private final Object dishId;
    private final int dayOfWeek;
    private final String mealType;

    public MealPlanAddRequest(String dishId, int dayOfWeek, String mealType) {
        Long asLong = tryParseLong(dishId);
        this.dishId = asLong != null ? asLong : dishId;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
    }

    private static Long tryParseLong(String raw) {
        if (raw == null) return null;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
