package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("name")
    private final String name;

    @SerializedName("username")
    private final String username;

    @SerializedName("password")
    private final String password;

    public RegisterRequest(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
