package com.example.sozlukjetpack.servis


import com.example.sozlukjetpack.model.Comment
import com.example.sozlukjetpack.model.Entry
import com.example.sozlukjetpack.model.SimpleResponse
import com.example.sozlukjetpack.model.VoteRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SozlukApiService {

    @POST("sozluk_register.php")
    fun registerUser(@Body body: Map<String, String>): Call<SimpleResponse>

    @POST("sozluk_login.php")
    fun loginUser(@Body body: Map<String, String>): Call<SimpleResponse>

    @POST("sozluk_entry_insert.php")
    fun addEntry(@Body body: Map<String, String>): Call<SimpleResponse>

    @GET("sozluk_entry_list.php")
    fun getAllEntries(): Call<List<Entry>>

    @GET("sozluk_entry_by_user.php")
    fun getEntriesByUser(@Query("user_id") userId: Int): Call<List<Entry>>

    @POST("sozluk_comment_insert.php")
    fun addComment(@Body body: Map<String, String>): Call<SimpleResponse>

    @GET("sozluk_comments_by_entry.php")
    fun getCommentsByEntry(@Query("entry_id") entryId: Int): Call<List<Comment>>

    // SozlukApiService.kt
    @POST("sozluk_like_comment.php")
    fun likeOrDislikeComment(@Body req: VoteRequest): Call<SimpleResponse>

    @POST("sozluk_entry_delete.php")
    fun deleteEntry(@Body body: Map<String, Int>): Call<SimpleResponse>
    @GET("sozluk_entry_get.php")
    fun getEntryById(@Query("entry_id") entryId: Int): Call<Entry?>

}
