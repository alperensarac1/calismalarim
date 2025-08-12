package com.example.sozlukkotlin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sozlukkotlin.dao.SozlukDao
import com.example.sozlukkotlin.model.Comment
import com.example.sozlukkotlin.model.Entry
import com.example.sozlukkotlin.model.SimpleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntryDetayViewModel : ViewModel() {
    private val dao = SozlukDao()

    private val _entry = MutableStateFlow<Entry?>(null)
    val entry: StateFlow<Entry?> = _entry

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun loadComments(entryId: Int) {
        dao.getCommentsByEntry(entryId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                }
            }

            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {}
        })
    }

    fun addComment(entryId: Int, userId: Int, text: String) {
        dao.addComment(entryId, userId, text).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    loadComments(entryId)
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {}
        })
    }

    fun voteComment(entryId: Int, commentId: Int, userId: Int, isLike: Boolean) {
        dao.voteComment(commentId, userId, isLike).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                if (response.isSuccessful) loadComments(entryId)
            }
            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {}
        })
    }

    fun loadEntry(entryId: Int) {
        dao.getEntryById(entryId).enqueue(object : Callback<Entry?> {
            override fun onResponse(c: Call<Entry?>, r: Response<Entry?>) {
                _entry.value = r.body()
            }
            override fun onFailure(c: Call<Entry?>, t: Throwable) {}
        })
    }

}
