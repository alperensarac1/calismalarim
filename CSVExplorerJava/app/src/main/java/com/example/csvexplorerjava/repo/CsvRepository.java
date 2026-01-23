package com.example.csvexplorerjava.repo;


import android.content.ContentResolver;
import android.net.Uri;

import com.example.csvexplorerjava.dao.RowDao;
import com.example.csvexplorerjava.entity.RowEntity;
import com.example.csvexplorerjava.model.CsvImportResult;
import com.example.csvexplorerjava.model.UploadResult;
import com.example.csvexplorerjava.service.AppDB;
import com.example.csvexplorerjava.service.UploadService;
import com.example.csvexplorerjava.usecases.DynamicCSVImporter;

import java.util.List;

public class CsvRepository {

    private final RowDao dao;

    public CsvRepository(AppDB db) {
        this.dao = db.rowDao();
    }

    public CsvImportResult importCsv(ContentResolver contentResolver, Uri uri) throws Exception {
        CsvImportResult result = DynamicCSVImporter.importCsv(contentResolver, uri);
        dao.insertAll(result.rows);
        return result;
    }

    public List<RowEntity> getAll() {
        return dao.getAll();
    }

    public void clear() {
        dao.clear();
    }

    public List<RowEntity> filter(String selected, String q) {
        if (q == null || q.trim().isEmpty()) return dao.getAll();
        if ("ALL_COLUMNS".equals(selected)) return dao.searchAllColumns(q);
        return dao.searchInColumn(selected, q);
    }

    public UploadResult uploadCsv(ContentResolver contentResolver, Uri uri, String endpoint) {
        return UploadService.uploadCsv(contentResolver, uri, endpoint);
    }
}
