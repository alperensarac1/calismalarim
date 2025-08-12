package com.example.sozlukkotlin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sozlukkotlin.dao.SozlukDao
import com.example.sozlukkotlin.model.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BugunViewModel : ViewModel() {
    private val dao = SozlukDao()
    private val _entries = MutableStateFlow<List<Entry>>(emptyList())
    val entries: StateFlow<List<Entry>> = _entries

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

    fun loadTodayEntries() {
        dao.getAllEntries().enqueue(object : Callback<List<Entry>> {
            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                if (response.isSuccessful) {
                    val list = response.body()?.sortedByDescending { it.created_at } ?: emptyList()
                    _entries.value = list
                }
            }

            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {}
        })
    }
}
