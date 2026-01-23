package com.example.csvexplorerjava.entity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UiState {
    private boolean isLoading = false;
    private List<RowEntity> records = new ArrayList<>();
    private List<String> headers = new ArrayList<>();
    private String selectedColumn = "ALL_COLUMNS";
    private String query = "";
    private String infoText = "0 records";
    private String downloadUrl = null;
    private String errorMessage = null;
    private boolean canUpload = false;

    public UiState() {}

    // Getters / Setters
    public boolean isLoading() { return isLoading; }
    public void setLoading(boolean loading) { isLoading = loading; }

    public List<RowEntity> getRecords() { return records == null ? Collections.emptyList() : records; }
    public void setRecords(List<RowEntity> records) { this.records = records; }

    public List<String> getHeaders() { return headers == null ? Collections.emptyList() : headers; }
    public void setHeaders(List<String> headers) { this.headers = headers; }

    public String getSelectedColumn() { return selectedColumn; }
    public void setSelectedColumn(String selectedColumn) { this.selectedColumn = selectedColumn; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getInfoText() { return infoText; }
    public void setInfoText(String infoText) { this.infoText = infoText; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean canUpload() { return canUpload; }
    public void setCanUpload(boolean canUpload) { this.canUpload = canUpload; }
}
