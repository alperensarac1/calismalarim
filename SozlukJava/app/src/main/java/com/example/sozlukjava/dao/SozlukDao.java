package com.example.sozlukjava.dao;

// SozlukDao.java
import com.example.sozlukjava.model.Comment;
import com.example.sozlukjava.model.Entry;
import com.example.sozlukjava.model.SimpleResponse;
import com.example.sozlukjava.model.VoteRequest;
import com.example.sozlukjava.service.ApiUtils;
import com.example.sozlukjava.service.SozlukApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class SozlukDao {

    private final SozlukApiService api;

    public SozlukDao() {
        this.api = ApiUtils.getService();
    }

    // Kullanıcı Kaydı
    public Call<SimpleResponse> register(String username, String password, String email) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("email", email);
        return api.registerUser(body);
    }

    // Giriş
    public Call<SimpleResponse> login(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        return api.loginUser(body);
    }

    // Entry Ekle
    public Call<SimpleResponse> addEntry(int userId, String title, String content) {
        Map<String, String> body = new HashMap<>();
        body.put("user_id", String.valueOf(userId));
        body.put("title", title);
        body.put("content", content);
        return api.addEntry(body);
    }

    // Entry Listele
    public Call<List<Entry>> getAllEntries() {
        return api.getAllEntries();
    }

    // Kullanıcının Entry’leri
    public Call<List<Entry>> getEntriesByUser(int userId) {
        return api.getEntriesByUser(userId);
    }

    // Yorum Ekle
    public Call<SimpleResponse> addComment(int entryId, int userId, String commentText) {
        Map<String, String> body = new HashMap<>();
        body.put("entry_id", String.valueOf(entryId));
        body.put("user_id", String.valueOf(userId));
        body.put("comment_text", commentText);
        return api.addComment(body);
    }

    // Entry’ye Yorumları Getir
    public Call<List<Comment>> getCommentsByEntry(int entryId) {
        return api.getCommentsByEntry(entryId);
    }

    // Yorum Like/Dislike
    public Call<SimpleResponse> voteComment(int commentId, int userId, boolean isLike) {
        VoteRequest req = new VoteRequest(
                commentId,
                userId,
                isLike ? 1 : 0
        );
        return api.likeOrDislikeComment(req);
    }

    // Entry Sil
    public Call<SimpleResponse> deleteEntry(int entryId) {
        Map<String, Integer> body = new HashMap<>();
        body.put("entry_id", entryId);
        return api.deleteEntry(body);
    }

    // ID'ye Göre Entry Getir
    public Call<Entry> getEntryById(int entryId) {
        return api.getEntryById(entryId);
    }
}

