package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;

public class FieldError {
    @SerializedName("field")
    private String field;

    @SerializedName("message")
    private String message;

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
