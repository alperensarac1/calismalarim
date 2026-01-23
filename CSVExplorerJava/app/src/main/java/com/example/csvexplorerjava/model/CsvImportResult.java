package com.example.csvexplorerjava.model;




import com.example.csvexplorerjava.entity.RowEntity;

import java.util.List;

public class CsvImportResult {
    public final List<String> headers;
    public final List<RowEntity> rows;

    public CsvImportResult(List<String> headers, List<RowEntity> rows) {
        this.headers = headers;
        this.rows = rows;
    }
}

