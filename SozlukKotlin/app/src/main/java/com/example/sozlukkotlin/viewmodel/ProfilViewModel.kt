package com.example.sozlukkotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sozlukkotlin.dao.SozlukDao
import com.example.sozlukkotlin.model.Entry
import com.example.sozlukkotlin.model.SimpleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilViewModel : ViewModel() {
    private val dao = SozlukDao()
    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries
    val deleteResult = MutableLiveData<SimpleResponse>()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterEntries()
    }

    private fun filterEntries() {
        val all = _entries.value
        _entries.value = if (_searchQuery.value.isBlank()) all
        else all.filter {
            it.title.contains(_searchQuery.value, ignoreCase = true)
        }
    }

    fun loadUserEntries(userId: Int) {
        dao.getEntriesByUser(userId).enqueue(object : Callback<List<Entry>> {
            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                _entries.value = response.body() ?: emptyList()
            }

            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {}
        })
    }


    fun deleteEntry(entryId: Int, userId: Int) {
        dao.deleteEntry(entryId).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                deleteResult.value = response.body()
                // Silme başarılıysa tekrar yükle
                loadUserEntries(userId)
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                deleteResult.value = SimpleResponse(false, "Bağlantı hatası")
            }
        })
    }

}
