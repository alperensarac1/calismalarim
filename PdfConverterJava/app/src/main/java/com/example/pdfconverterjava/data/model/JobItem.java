package com.example.pdfconverterjava.data.model;

public class JobItem {

    private Integer job_id;
    private String job_type;
    private String status;
    private String source_file_url;
    private String result_file_url;
    private String error_message;
    private String created_at;

    public Integer getJob_id() {
        return job_id;
    }

    public String getJob_type() {
        return job_type;
    }

    public String getStatus() {
        return status;
    }

    public String getSource_file_url() {
        return source_file_url;
    }

    public String getResult_file_url() {
        return result_file_url;
    }

    public String getError_message() {
        return error_message;
    }

    public String getCreated_at() {
        return created_at;
    }
}
