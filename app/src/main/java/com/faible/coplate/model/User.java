package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String name;
    private String role;
    @SerializedName(value = "familyId", alternate = {"family_id"})
    private String familyId;

    public User() {}

    public User(String id, String username, String name, String role) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFamilyId() { return familyId; }
    public void setFamilyId(String familyId) { this.familyId = familyId; }
}