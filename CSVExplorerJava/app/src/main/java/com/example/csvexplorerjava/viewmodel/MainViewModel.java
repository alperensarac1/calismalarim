package com.example.csvexplorerjava.viewmodel;


import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.csvexplorerjava.entity.HeadersStore;
import com.example.csvexplorerjava.entity.RowEntity;
import com.example.csvexplorerjava.entity.UiState;
import com.example.csvexplorerjava.model.CsvImportResult;
import com.example.csvexplorerjava.model.UploadResult;
import com.example.csvexplorerjava.repo.CsvRepository;
import com.example.csvexplorerjava.service.AppDB;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainViewModel extends AndroidViewModel {

    private final CsvRepository repo;
    private final MutableLiveData<UiState> state = new MutableLiveData<>(new UiState());
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private Uri lastPickedUri = null;

    public MainViewModel(@NonNull Application app) {
        super(app);
        repo = new CsvRepository(AppDB.get(app));
    }

    public LiveData<UiState> getState() {
        return state;
    }

    private UiState s() {
        UiState v = state.getValue();
        return v != null ? v : new UiState();
    }

    private void post(UiState newState) {
        state.postValue(newState);
    }

    public void init() {
        UiState cur = s();
        List<String> headers = HeadersStore.load(getApplication());

        cur.setHeaders(headers);
        cur.setInfoText(cur.getRecords().size() + " records");
        cur.setCanUpload(lastPickedUri != null);
        post(cur);

        refreshAll();
    }

    public void onCsvPicked(Uri uri, ContentResolver contentResolver) {
        lastPickedUri = uri;

        UiState cur = s();
        cur.setCanUpload(true);
        post(cur);

        importCsv(uri, contentResolver);
    }

    public void setQuery(String q) {
        UiState cur = s();
        cur.setQuery(q != null ? q : "");
        post(cur);
    }

    public void setSelectedColumn(String col) {
        UiState cur = s();
        cur.setSelectedColumn(col != null ? col : "ALL_COLUMNS");
        post(cur);
    }

    public void refreshAll() {
        io.execute(() -> {
            List<RowEntity> list = repo.getAll();
            UiState cur = s();
            cur.setRecords(list);
            cur.setLoading(false);
            cur.setErrorMessage(null);
            cur.setDownloadUrl(null);
            cur.setInfoText(list.size() + " records");
            post(cur);
        });
    }

    private String jsonValue(String rowJson, String key) {
        try {
            return new JSONObject(rowJson).optString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    public void applyFilter() {
        io.execute(() -> {
            UiState cur = s();
            List<RowEntity> list = repo.filter(cur.getSelectedColumn(), cur.getQuery());

            // seçili kolona göre sıralama
            if (!"ALL_COLUMNS".equals(cur.getSelectedColumn())) {
                String sel = cur.getSelectedColumn();
                list.sort(Comparator.comparing(
                        it -> jsonValue(it.getDataJson(), sel).toLowerCase(Locale.getDefault())
                ));
            }

            UiState out = s();
            out.setRecords(list);
            out.setInfoText(list.size() + " records (filter: " + out.getSelectedColumn() + ")");
            out.setErrorMessage(null);
            post(out);
        });
    }

    private void importCsv(Uri uri, ContentResolver contentResolver) {
        UiState cur = s();
        cur.setLoading(true);
        cur.setErrorMessage(null);
        cur.setDownloadUrl(null);
        cur.setInfoText("Importing...");
        post(cur);

        io.execute(() -> {
            try {
                CsvImportResult res = repo.importCsv(contentResolver, uri);
                HeadersStore.save(getApplication(), res.headers);

                UiState ok = s();
                ok.setHeaders(res.headers);
                ok.setLoading(false);
                int count = res.rows != null ? res.rows.size() : 0;
                ok.setInfoText("Imported: " + count + " " + (count == 1 ? "row" : "rows"));
                post(ok);

                refreshAll();
            } catch (Exception e) {
                UiState err = s();
                err.setLoading(false);
                err.setErrorMessage("Import error: " + e.getMessage());
                err.setInfoText("Import failed");
                post(err);
            }
        });
    }

    public void clearDb() {
        UiState cur = s();
        cur.setLoading(true);
        cur.setErrorMessage(null);
        cur.setInfoText("Clearing database...");
        post(cur);

        io.execute(() -> {
            try {
                repo.clear();
                UiState ok = s();
                ok.setLoading(false);
                ok.setQuery("");
                ok.setSelectedColumn("ALL_COLUMNS");
                ok.setInfoText("Database cleared.");
                post(ok);
                refreshAll();
            } catch (Exception e) {
                UiState err = s();
                err.setLoading(false);
                err.setErrorMessage("Clear DB error: " + e.getMessage());
                err.setInfoText("Clear failed");
                post(err);
            }
        });
    }

    public void upload(String endpoint, ContentResolver contentResolver) {
        if (lastPickedUri == null) {
            UiState cur = s();
            cur.setErrorMessage("Please select a CSV file first.");
            post(cur);
            return;
        }

        UiState cur = s();
        cur.setLoading(true);
        cur.setErrorMessage(null);
        cur.setInfoText("Uploading...");
        post(cur);

        Uri uri = lastPickedUri;
        io.execute(() -> {
            UploadResult res = repo.uploadCsv(contentResolver, uri, endpoint);

            if (!res.ok) {
                UiState err = s();
                err.setLoading(false);
                err.setErrorMessage("Upload failed: " + res.error);
                err.setInfoText("Upload failed");
                post(err);
                return;
            }

            UiState ok = s();
            ok.setLoading(false);
            ok.setDownloadUrl(res.downloadUrl);
            ok.setInfoText("Download as .xls: " + (res.downloadUrl != null ? res.downloadUrl : "N/A"));
            post(ok);
        });
    }

    public void consumeDownloadUrl() {
        UiState cur = s();
        cur.setDownloadUrl(null);
        post(cur);
    }

    public void clearFilter() {
        UiState cur = s();
        cur.setQuery("");
        cur.setSelectedColumn("ALL_COLUMNS");
        post(cur);
        refreshAll();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        io.shutdown();
    }
}

