package com.dreamweather.backend.dto;

public class LocationResponse {
    private boolean success;
    private String message;
    private LocationDto data;

    public LocationResponse() {}

    public LocationResponse(boolean success, String message, LocationDto data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocationDto getData() { return data; }
    public void setData(LocationDto data) { this.data = data; }
}
