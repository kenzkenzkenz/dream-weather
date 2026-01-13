package com.dreamweather.backend.dto;

public class LivestreamResponse {
    private boolean success;
    private String message;
    private LiveStreamDto data;

    public LivestreamResponse() {}

    public LivestreamResponse(boolean success, String message, LiveStreamDto data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LiveStreamDto getData() { return data; }
    public void setData(LiveStreamDto data) { this.data = data; }
}
