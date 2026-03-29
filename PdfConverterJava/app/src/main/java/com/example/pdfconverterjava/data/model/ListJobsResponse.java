package com.example.pdfconverterjava.data.model;

import java.util.List;

public class ListJobsResponse {

    private boolean success;
    private List<JobItem> jobs;

    public boolean isSuccess() {
        return success;
    }

    public List<JobItem> getJobs() {
        return jobs;
    }
}
