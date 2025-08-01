package com.example.haberuygulamajava.servis;

import java.io.Serializable;

public class ApiResponse implements Serializable {
    private boolean success;
    private Integer id;
    private String error;

    public boolean isSuccess() {
        return success;
    }

    public Integer getId() {
        return id;
    }

    public String getError() {
        return error;
    }
}

