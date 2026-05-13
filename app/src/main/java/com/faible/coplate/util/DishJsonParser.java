package com.faible.coplate.util;

import com.faible.coplate.model.DishResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Разбор JSON списков блюд: семья ({@code GET /api/families/.../dishes}) и библиотека ({@code GET /api/library}).
 */
public final class DishJsonParser {

    private static final Gson GSON = new Gson();
    private static final Type DISH_LIST = new TypeToken<List<DishResponse>>() {}.getType();

    private DishJsonParser() {}

    /**
     * {@code GET /api/library}: корень — массив {@link DishResponse}, либо объект {@code { "library": [ ... ] }}.
     */
    public static List<DishResponse> parseLibraryList(JsonElement root) {
        if (root == null || root.isJsonNull()) {
            return Collections.emptyList();
        }
        if (root.isJsonArray()) {
            List<DishResponse> parsed = GSON.fromJson(root, DISH_LIST);
            return parsed != null ? parsed : Collections.emptyList();
        }
        if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();
            if (obj.has("library")) {
                JsonElement inner = obj.get("library");
                if (inner != null && inner.isJsonArray()) {
                    List<DishResponse> parsed = GSON.fromJson(inner, DISH_LIST);
                    return parsed != null ? parsed : Collections.emptyList();
                }
            }
        }
        return parseFamilyDishes(root);
    }

    public static List<DishResponse> parseFamilyDishes(JsonElement root) {
        if (root == null || root.isJsonNull()) {
            return Collections.emptyList();
        }
        if (root.isJsonArray()) {
            List<DishResponse> parsed = GSON.fromJson(root, DISH_LIST);
            return parsed != null ? parsed : Collections.emptyList();
        }
        if (!root.isJsonObject()) {
            return Collections.emptyList();
        }
        JsonObject obj = root.getAsJsonObject();
        for (String key : new String[]{"library", "dishes", "data", "items", "content", "results", "body"}) {
            if (!obj.has(key)) {
                continue;
            }
            JsonElement inner = obj.get(key);
            if (inner.isJsonArray()) {
                List<DishResponse> parsed = GSON.fromJson(inner, DISH_LIST);
                return parsed != null ? parsed : Collections.emptyList();
            }
            if (inner.isJsonObject()) {
                List<DishResponse> nested = parseFamilyDishes(inner);
                if (!nested.isEmpty()) {
                    return nested;
                }
            }
        }
        return Collections.emptyList();
    }
}
