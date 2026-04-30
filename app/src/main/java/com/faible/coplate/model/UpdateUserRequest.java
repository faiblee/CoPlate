package com.faible.coplate.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("old_password")
    private String oldPassword;

    @SerializedName("new_password")
    private String newPassword;

    @SerializedName("name")
    private String name;

    public UpdateUserRequest(String username, String oldPassword, String newPassword, String name) {
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.name = name;
    }

    // Геттеры и сеттеры (можно сгенерировать автоматически в IDE)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}