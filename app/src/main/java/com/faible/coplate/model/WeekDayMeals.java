package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ответ недельного меню из CoPlate-backend: у каждого дня завтрак/обед/ужин —
 * объект с полем {@code dishes}, а не плоский список блюд (см. DayMenu и MealSlot в репозитории backend).
 */
public class WeekDayMeals {

    private Integer dayOfWeek;
    @SerializedName(value = "breakfast", alternate = {"breakfastDishes"})
    private MealSlotDto breakfast;
    @SerializedName(value = "lunch", alternate = {"lunchDishes"})
    private MealSlotDto lunch;
    @SerializedName(value = "dinner", alternate = {"dinnerDishes"})
    private MealSlotDto dinner;

    /** Как MealSlot на backend: mealType + список кратких блюд (DishInfoResponse). */
    public static class MealSlotDto {
        private String mealType;
        private List<MealPlanDishRef> dishes;

        public String getMealType() {
            return mealType;
        }

        public List<MealPlanDishRef> getDishes() {
            return dishes != null ? dishes : new ArrayList<>();
        }
    }

    public int getDayOfWeek() {
        return dayOfWeek != null ? dayOfWeek : 0;
    }

    public List<MealPlanDishRef> getBreakfast() {
        return dishesFromSlot(breakfast);
    }

    public List<MealPlanDishRef> getLunch() {
        return dishesFromSlot(lunch);
    }

    public List<MealPlanDishRef> getDinner() {
        return dishesFromSlot(dinner);
    }

    private static List<MealPlanDishRef> dishesFromSlot(MealSlotDto slot) {
        if (slot == null) {
            return Collections.emptyList();
        }
        return slot.getDishes();
    }
}
