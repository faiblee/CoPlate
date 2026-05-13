package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class MealPlanRemoveRequest {
    @SerializedName("dishId")
    private final long dishId;
    private final int dayOfWeek;
    private final String mealType;

    public MealPlanRemoveRequest(long dishId, int dayOfWeek, String mealType) {
        this.dishId = dishId;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
    }
}
