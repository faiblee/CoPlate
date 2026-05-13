package com.faible.coplate.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Ссылка на блюдо в ответе плана на неделю (id/name или dishId/dishName).
 */
public class MealPlanDishRef {
    /** ID блюда (см. DishInfoResponseWithMealPlan.id на backend). */
    @SerializedName(value = "id", alternate = {"dishId"})
    private Object id;
    /** ID строки MealPlan для DELETE .../meal-plans/{planId}. */
    @SerializedName(value = "planId", alternate = {"plan_id"})
    private Object planId;
    @SerializedName(value = "name", alternate = {"dishName", "title"})
    private String name;

    public String getId() {
        if (id == null) {
            return null;
        }
        if (id instanceof Number) {
            return String.valueOf(((Number) id).longValue());
        }
        return id.toString();
    }

    public String getName() {
        return name;
    }

    /** @return id записи плана приёма пищи или null, если в JSON нет planId */
    @Nullable
    public Long getMealPlanRowId() {
        if (planId == null) {
            return null;
        }
        if (planId instanceof Number) {
            return ((Number) planId).longValue();
        }
        try {
            return Long.parseLong(planId.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
