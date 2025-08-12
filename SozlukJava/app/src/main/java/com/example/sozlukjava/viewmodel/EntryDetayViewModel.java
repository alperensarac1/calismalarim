package com.example.sozlukjava.viewmodel;

// EntryDetayViewModel.java
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sozlukjava.dao.SozlukDao;
import com.example.sozlukjava.model.Comment;
import com.example.sozlukjava.model.Entry;
import com.example.sozlukjava.model.SimpleResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntryDetayViewModel extends ViewModel {
    private final SozlukDao dao = new SozlukDao();

    private final MutableLiveData<Entry> entry = new MutableLiveData<>();
    private final MutableLiveData<List<Comment>> comments = new MutableLiveData<>(new ArrayList<>());

    public MutableLiveData<Entry> getEntry() { return entry; }
    public MutableLiveData<List<Comment>> getComments() { return comments; }

    public void loadComments(int entryId) {
        dao.getCommentsByEntry(entryId).enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comments.setValue(response.body());
                }
            }
            @Override public void onFailure(Call<List<Comment>> call, Throwable t) { }
        });
    }

    public void addComment(int entryId, int userId, String text) {
        dao.addComment(entryId, userId, text).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    loadComments(entryId);
                }
            }
            @Override public void onFailure(Call<SimpleResponse> call, Throwable t) { }
        });
    }

    public void voteComment(int entryId, int commentId, int userId, boolean isLike) {
        dao.voteComment(commentId, userId, isLike).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(Call<SimpleResponse> call, Response<SimpleResponse> response) {
                if (response.isSuccessful()) {
                    loadComments(entryId);
                }
            }
            @Override public void onFailure(Call<SimpleResponse> call, Throwable t) { }
        });
    }

    public void loadEntry(int entryId) {
        dao.getEntryById(entryId).enqueue(new Callback<Entry>() {
            @Override
            public void onResponse(Call<Entry> c, Response<Entry> r) {
                entry.setValue(r.body());
            }
            @Override public void onFailure(Call<Entry> c, Throwable t) { }
        });
    }
}

