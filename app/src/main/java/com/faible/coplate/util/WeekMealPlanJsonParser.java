package com.faible.coplate.util;

import com.faible.coplate.model.WeekMealPlanResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Парсит ответ GET week; при несовпадении схемы пробует «обёртки».
 */
public final class WeekMealPlanJsonParser {

    private static final Gson GSON = new Gson();

    private WeekMealPlanJsonParser() {}

    public static WeekMealPlanResponse parse(JsonElement root) {
        if (root == null || root.isJsonNull()) {
            return new WeekMealPlanResponse();
        }
        JsonElement effective = unwrap(root);
        WeekMealPlanResponse plan = GSON.fromJson(effective, WeekMealPlanResponse.class);
        if (plan != null && !plan.getDays().isEmpty()) {
            return plan;
        }
        return plan != null ? plan : new WeekMealPlanResponse();
    }

    private static JsonElement unwrap(JsonElement root) {
        if (!root.isJsonObject()) {
            return root;
        }
        JsonObject obj = root.getAsJsonObject();
        for (String key : new String[]{"data", "payload", "result", "week", "weekPlan", "body"}) {
            if (obj.has(key)) {
                JsonElement inner = obj.get(key);
                if (inner != null && !inner.isJsonNull()) {
                    return inner;
                }
            }
        }
        return root;
    }
}
