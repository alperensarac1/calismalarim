package com.example.sozlukjava.service;

// SozlukApiService.java
import com.example.sozlukjava.model.Comment;
import com.example.sozlukjava.model.Entry;
import com.example.sozlukjava.model.SimpleResponse;
import com.example.sozlukjava.model.VoteRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SozlukApiService {

    @POST("sozluk_register.php")
    Call<SimpleResponse> registerUser(@Body Map<String, String> body);

    @POST("sozluk_login.php")
    Call<SimpleResponse> loginUser(@Body Map<String, String> body);

    @POST("sozluk_entry_insert.php")
    Call<SimpleResponse> addEntry(@Body Map<String, String> body);

    @GET("sozluk_entry_list.php")
    Call<List<Entry>> getAllEntries();

    @GET("sozluk_entry_by_user.php")
    Call<List<Entry>> getEntriesByUser(@Query("user_id") int userId);

    @POST("sozluk_comment_insert.php")
    Call<SimpleResponse> addComment(@Body Map<String, String> body);

    @GET("sozluk_comments_by_entry.php")
    Call<List<Comment>> getCommentsByEntry(@Query("entry_id") int entryId);

    @POST("sozluk_like_comment.php")
    Call<SimpleResponse> likeOrDislikeComment(@Body VoteRequest req);

    @POST("sozluk_entry_delete.php")
    Call<SimpleResponse> deleteEntry(@Body Map<String, Integer> body);

    @GET("sozluk_entry_get.php")
    Call<Entry> getEntryById(@Query("entry_id") int entryId);
}

