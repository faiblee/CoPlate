package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Соответствует backend WeekPlanResponse: familyId (Long в JSON), days — список DayMenu. */
public class WeekMealPlanResponse {

    @SerializedName(value = "familyId", alternate = {"family_id"})
    private Object familyId;

    private List<WeekDayMeals> days;

    public String getFamilyIdString() {
        if (familyId == null) {
            return null;
        }
        if (familyId instanceof Number) {
            return String.valueOf(((Number) familyId).longValue());
        }
        return familyId.toString();
    }

    public List<WeekDayMeals> getDays() {
        return days != null ? days : new ArrayList<>();
    }
}
