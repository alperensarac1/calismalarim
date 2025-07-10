package com.example.chatjava.service.response;

public class SimpleResponse {
    private boolean success;
    private String message;
    private Integer id;
    private String error;

    public SimpleResponse(boolean success, String message, Integer id, String error) {
        this.success = success;
        this.message = message;
        this.id = id;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getId() {
        return id;
    }

    public String getError() {
        return error;
    }
}

