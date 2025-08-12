package com.example.sozlukjetpack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sozlukjetpack.dao.SozlukDao
import com.example.sozlukjetpack.model.Entry
import com.example.sozlukjetpack.util.EntriesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BugunViewModel : ViewModel() {
    private val dao = SozlukDao()

    private val _ui = MutableStateFlow(EntriesUiState())
    val ui: StateFlow<EntriesUiState> = _ui.asStateFlow()

    private val _all = MutableStateFlow<List<Entry>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val entries: StateFlow<List<Entry>> =
        combine(
            _all,
            _searchQuery.debounce(300).distinctUntilChanged()
        ) { list, q ->
            if (q.isBlank()) list
            else list.filter { it.title.contains(q, ignoreCase = true) }
        }.stateIn(
            scope = kotlinx.coroutines.CoroutineScope(Dispatchers.Main.immediate),
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun loadTodayEntries() {
        _ui.value = _ui.value.copy(loading = true, error = null)
        dao.getAllEntries().enqueue(object : Callback<List<Entry>> {
            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                _ui.value = _ui.value.copy(loading = false)
                if (response.isSuccessful) {
                    // created_at string ise sunucu formatına göre gerçek “bugün” filtresi eklenebilir.
                    val list = response.body()?.sortedByDescending { it.created_at } ?: emptyList()
                    _all.value = list
                } else {
                    _ui.value = _ui.value.copy(error = "Veri alınamadı (${response.code()})")
                }
            }

            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = "Bağlantı hatası")
            }
        })
    }
}
