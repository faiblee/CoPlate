package com.faible.coplate.model;

import java.util.ArrayList;
import java.util.List;

public class DishResponse {
    private String id;
    private String name;
    private String description;
    private String source;
    private String familyId;
    private String ownerId;
    private List<DishIngredientResponse> ingredients;

    public String getId() {
        return id;
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
        return familyId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public List<DishIngredientResponse> getIngredients() {
        return ingredients != null ? ingredients : new ArrayList<>();
    }
}
