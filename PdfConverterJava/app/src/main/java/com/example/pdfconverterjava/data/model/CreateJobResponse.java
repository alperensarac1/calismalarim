package com.example.pdfconverterjava.data.model;

public class CreateJobResponse {

    private boolean success;
    private Integer job_id;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public Integer getJob_id() {
        return job_id;
    }

    public String getMessage() {
        return message;
    }
}
