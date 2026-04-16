package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class FamilyCreateRequest {
    @SerializedName("name") private String name;
    @SerializedName("ownerId") private String ownerId;

    public FamilyCreateRequest(String name, String ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }
}