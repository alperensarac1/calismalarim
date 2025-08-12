package com.example.sozlukjava.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sozlukjava.dao.SozlukDao;
import com.example.sozlukjava.model.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BugunViewModel extends ViewModel {
    private final SozlukDao dao = new SozlukDao();

    private final MutableLiveData<List<Entry>> entries = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<Entry>> getEntries() { return entries; }

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

    public void loadTodayEntries() {
        dao.getAllEntries().enqueue(new Callback<List<Entry>>() {
            @Override
            public void onResponse(Call<List<Entry>> call, Response<List<Entry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEntries = new ArrayList<>(response.body());
                    // created_at "YYYY-MM-DD HH:mm:ss" ise string karşılaştırması çalışır
                    Collections.sort(allEntries, (a, b) -> {
                        String ca = a.getCreated_at() == null ? "" : a.getCreated_at();
                        String cb = b.getCreated_at() == null ? "" : b.getCreated_at();
                        return cb.compareTo(ca); // desc
                    });
                    filterEntries();
                }
            }
            @Override public void onFailure(Call<List<Entry>> call, Throwable t) { }
        });
    }
}

