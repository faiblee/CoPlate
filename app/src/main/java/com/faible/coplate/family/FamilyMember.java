package com.faible.coplate.family;

public class FamilyMember {
    private String name;
    private String role;
    private boolean isCreator;

    public FamilyMember(String name, String role, boolean isCreator) {
        this.name = name;
        this.isCreator = isCreator;
    }

    public String getName() { return name; }
    public boolean isCreator() { return isCreator; }
}
