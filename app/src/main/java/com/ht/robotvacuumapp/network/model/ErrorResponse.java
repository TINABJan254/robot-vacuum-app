package com.ht.robotvacuumapp.network.model;

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

    /**
     * Helper to return a user-friendly error message
     */
    public String getDisplayMessage() {
        if (fieldErrors != null && !fieldErrors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (FieldError error : fieldErrors) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(error.getField()).append(": ").append(error.getMessage());
            }
            return sb.toString();
        }
        if (message != null && !message.isEmpty()) {
            return message;
        }
        return "Yêu cầu không hợp lệ";
    }
}
