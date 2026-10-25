package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ErrorResponse {
    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("status")
    private Integer status;

    @SerializedName("message")
    private String message;

    @SerializedName("path")
    private String path;

    @SerializedName("fieldErrors")
    private List<FieldError> fieldErrors;

    public String getTimestamp() {
        return timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

}
