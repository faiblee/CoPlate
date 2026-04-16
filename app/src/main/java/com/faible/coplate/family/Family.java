package com.faible.coplate.family;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Family {
    @SerializedName("id") private String id;
    @SerializedName("name") private String name;
    @SerializedName("ownerId") private String ownerId;
    @SerializedName("inviteCode") private String inviteCode;

    // Конструктор по умолчанию нужен для Retrofit/Gson
    public Family() {}

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}