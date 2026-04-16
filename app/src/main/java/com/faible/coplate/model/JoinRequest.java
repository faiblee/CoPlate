package com.faible.coplate.model;
import com.google.gson.annotations.SerializedName;

public class JoinRequest {
    @SerializedName("inviteCode") private String inviteCode;
    @SerializedName("userId") private String userId;

    public JoinRequest(String inviteCode, String userId) {
        this.inviteCode = inviteCode;
        this.userId = userId;
    }
}