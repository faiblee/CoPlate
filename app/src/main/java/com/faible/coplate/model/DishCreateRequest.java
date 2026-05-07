package com.faible.coplate.model;

import java.util.List;

public class DishCreateRequest {
    private final String name;
    private final String description;
    private final String source;
    private final String familyId;
    private final String ownerId;
    private final List<DishIngredientRequest> ingredients;

    public DishCreateRequest(
            String name,
            String description,
            String source,
            String familyId,
            String ownerId,
            List<DishIngredientRequest> ingredients
    ) {
        this.name = name;
        this.description = description;
        this.source = source;
        this.familyId = familyId;
        this.ownerId = ownerId;
        this.ingredients = ingredients;
    }
}
