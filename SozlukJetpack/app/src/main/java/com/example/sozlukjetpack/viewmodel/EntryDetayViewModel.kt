package com.example.sozlukjetpack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sozlukjetpack.dao.SozlukDao
import com.example.sozlukjetpack.model.Comment
import com.example.sozlukjetpack.model.Entry
import com.example.sozlukjetpack.model.SimpleResponse
import com.example.sozlukjetpack.util.EntryDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntryDetayViewModel : ViewModel() {
    private val dao = SozlukDao()

    private val _entry = MutableStateFlow<Entry?>(null)
    val entry: StateFlow<Entry?> = _entry.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _ui = MutableStateFlow(EntryDetailUiState())
    val ui: StateFlow<EntryDetailUiState> = _ui.asStateFlow()

    fun loadEntry(entryId: Int) {
        _ui.value = _ui.value.copy(loadingEntry = true, error = null)
        dao.getEntryById(entryId).enqueue(object : Callback<Entry?> {
            override fun onResponse(c: Call<Entry?>, r: Response<Entry?>) {
                _ui.value = _ui.value.copy(loadingEntry = false)
                if (r.isSuccessful) {
                    _entry.value = r.body()
                } else {
                    _ui.value = _ui.value.copy(error = "Entry alınamadı (${r.code()})")
                }
            }
            override fun onFailure(c: Call<Entry?>, t: Throwable) {
                _ui.value = _ui.value.copy(loadingEntry = false, error = "Bağlantı hatası")
            }
        })
    }

    fun loadComments(entryId: Int) {
        _ui.value = _ui.value.copy(loadingComments = true, error = null)
        dao.getCommentsByEntry(entryId).enqueue(object : Callback<List<Comment>> {
            override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                _ui.value = _ui.value.copy(loadingComments = false)
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                } else {
                    _ui.value = _ui.value.copy(error = "Yorum alınamadı (${response.code()})")
                }
            }
            override fun onFailure(call: Call<List<Comment>>, t: Throwable) {
                _ui.value = _ui.value.copy(loadingComments = false, error = "Bağlantı hatası")
            }
        })
    }

    fun addComment(entryId: Int, userId: Int, text: String) {
        _ui.value = _ui.value.copy(posting = true, error = null)
        dao.addComment(entryId, userId, text).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                _ui.value = _ui.value.copy(posting = false)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadComments(entryId)
                } else {
                    _ui.value = _ui.value.copy(error = response.body()?.message ?: "Yorum eklenemedi")
                }
            }
            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                _ui.value = _ui.value.copy(posting = false, error = "Bağlantı hatası")
            }
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
}
