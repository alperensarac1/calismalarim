package com.example.sozlukjetpack.dao


import com.example.sozlukjatpack.servis.ApiUtils
import com.example.sozlukjetpack.model.Comment
import com.example.sozlukjetpack.model.Entry
import com.example.sozlukjetpack.model.SimpleResponse
import com.example.sozlukjetpack.model.VoteRequest

import retrofit2.Call

class SozlukDao {

    private val api = ApiUtils.getService()

    // Kullanıcı Kaydı
    fun register(username: String, password: String, email: String): Call<SimpleResponse> {
        val body = mapOf(
            "username" to username,
            "password" to password,
            "email" to email
        )
        return api.registerUser(body)
    }

    // Giriş
    fun login(username: String, password: String): Call<SimpleResponse> {
        val body = mapOf(
            "username" to username,
            "password" to password
        )
        return api.loginUser(body)
    }

    // Entry Ekle
    fun addEntry(userId: Int, title: String, content: String): Call<SimpleResponse> {
        val body = mapOf(
            "user_id" to userId.toString(),
            "title" to title,
            "content" to content
        )
        return api.addEntry(body)
    }

    // Entry Listele
    fun getAllEntries(): Call<List<Entry>> {
        return api.getAllEntries()
    }

    // Kullanıcının Entry’leri
    fun getEntriesByUser(userId: Int): Call<List<Entry>> {
        return api.getEntriesByUser(userId)
    }

    // Yorum Ekle
    fun addComment(entryId: Int, userId: Int, commentText: String): Call<SimpleResponse> {
        val body = mapOf(
            "entry_id" to entryId.toString(),
            "user_id" to userId.toString(),
            "comment_text" to commentText
        )
        return api.addComment(body)
    }

    // Entry’ye Yorumları Getir
    fun getCommentsByEntry(entryId: Int): Call<List<Comment>> {
        return api.getCommentsByEntry(entryId)
    }

    // Yorum Like/Dislike
    // SozlukDao.kt
    fun voteComment(commentId: Int, userId: Int, isLike: Boolean): Call<SimpleResponse> {
        val req = VoteRequest(
            comment_id = commentId,
            user_id = userId,
            is_like = if (isLike) 1 else 0
        )
        return api.likeOrDislikeComment(req)
    }

    fun deleteEntry(entryId: Int): Call<SimpleResponse> {
        val body = mapOf("entry_id" to entryId)
        return api.deleteEntry(body)
    }
    fun getEntryById(entryId: Int): Call<Entry?> = api.getEntryById(entryId)


}