package com.faible.coplate.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DishResponse {
    private Object id;
    private String name;
    @SerializedName(value = "description", alternate = {"desc"})
    private String description;
    private String source;
    @SerializedName(value = "familyId", alternate = {"family_id"})
    private Object familyId;
    @SerializedName(
            value = "ownerId",
            alternate = {"owner_id", "user_id", "userId", "creator_id", "creatorId", "author_id"}
    )
    private Object ownerId;
    @SerializedName(
            value = "ingredients",
            alternate = {"ingredientList", "ingredient_list", "ingredient"})
    private List<DishIngredientResponse> ingredients;

    /** Подмена связанных из БД ингредиентов (напр. после GET /dishes/{id}). */
    public void replaceIngredients(@Nullable List<DishIngredientResponse> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        ingredients = new ArrayList<>(list);
    }

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

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public String getFamilyId() {
        if (familyId == null) {
            return null;
        }
        if (familyId instanceof Number) {
            return String.valueOf(((Number) familyId).longValue());
        }
        return familyId.toString();
    }

    public String getOwnerId() {
        if (ownerId == null) {
            return null;
        }
        if (ownerId instanceof Number) {
            return String.valueOf(((Number) ownerId).longValue());
        }
        return ownerId.toString();
    }

    public List<DishIngredientResponse> getIngredients() {
        return ingredients != null ? ingredients : new ArrayList<>();
    }
}
