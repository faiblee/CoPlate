package com.faible.coplate.family;

public class Family {
    private String id;
    private String name;
    private String ownerId;
    private String inviteCode;

    // Пустой конструктор нужен для библиотек сериализации (например, Gson/Retrofit)
    public Family() {}

    public Family(String id, String name, String ownerId, String inviteCode) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.inviteCode = inviteCode;
    }

    // Геттеры и Сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}