package com.example.sozlukjava.viewmodel;

// ProfilViewModel.java
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sozlukjava.dao.SozlukDao;
import com.example.sozlukjava.model.Entry;
import com.example.sozlukjava.model.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilViewModel extends ViewModel {
    private final SozlukDao dao = new SozlukDao();

    private final MutableLiveData<List<Entry>> entries = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<Entry>> getEntries() { return entries; }

    public final MutableLiveData<SimpleResponse> deleteResult = new MutableLiveData<>();

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    public MutableLiveData<String> getSearchQuery() { return searchQuery; }

    private List<Entry> allEntries = new ArrayList<>();

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        filterEntries();
    }

    private void filterEntries() {
        String q = searchQuery.getValue() == null ? "" : searchQuery.getValue();
        if (q.isBlank()) {
            entries.setValue(new ArrayList<>(allEntries));
        } else {
            List<Entry> filtered = new ArrayList<>();
            for (Entry e : allEntries) {
                if (e.getTitle() != null && e.getTitle().toLowerCase().contains(q.toLowerCase())) {
                    filtered.add(e);
                }
            }
            entries.setValue(filtered);
        }
    }

    public void loadUserEntries(int userId) {
        dao.getEntriesByUser(userId).enqueue(new Callback<List<Entry>>() {
            @Override
            public void onResponse(Call<List<Entry>> call, Response<List<Entry>> response) {
                allEntries = response.body() != null ? new ArrayList<>(response.body()) : new ArrayList<>();
                filterEntries();
            }
            @Override public void onFailure(Call<List<Entry>> call, Throwable t) { }
        });
    }

    public void deleteEntry(int entryId, int userId) {
        dao.deleteEntry(entryId).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                deleteResult.setValue(response.body());
                loadUserEntries(userId); // başarılıysa tekrar yükle
            }
            @Override
            public void onFailure(Call<SimpleResponse> call, Throwable t) {
                SimpleResponse sr = new SimpleResponse();
                sr.setSuccess(false);
                sr.setMessage("Bağlantı hatası");
                deleteResult.setValue(sr);
            }
        });
    }
}

