package com.example.sozlukjetpack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sozlukjetpack.dao.SozlukDao
import com.example.sozlukjetpack.model.Entry
import com.example.sozlukjetpack.model.SimpleResponse
import com.example.sozlukjetpack.util.EntriesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilViewModel : ViewModel() {
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

    // Silme geri bildirimi (Compose tarafında observeAsState yerine collectAsState)
    private val _deleteResult = MutableStateFlow<SimpleResponse?>(null)
    val deleteResult: StateFlow<SimpleResponse?> = _deleteResult.asStateFlow()

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun loadUserEntries(userId: Int) {
        _ui.value = _ui.value.copy(loading = true, error = null)
        dao.getEntriesByUser(userId).enqueue(object : Callback<List<Entry>> {
            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                _ui.value = _ui.value.copy(loading = false)
                if (response.isSuccessful) {
                    _all.value = response.body() ?: emptyList()
                } else {
                    _ui.value = _ui.value.copy(error = "Veri alınamadı (${response.code()})")
                }
            }

            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = "Bağlantı hatası")
            }
        })
    }

    fun deleteEntry(entryId: Int, userId: Int) {
        _ui.value = _ui.value.copy(loading = true)
        dao.deleteEntry(entryId).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                _ui.value = _ui.value.copy(loading = false)
                val body = response.body() ?: SimpleResponse(false, "Silme başarısız")
                _deleteResult.value = body
                if (body.success) {
                    // Listeyi tazele
                    loadUserEntries(userId)
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                _ui.value = _ui.value.copy(loading = false)
                _deleteResult.value = SimpleResponse(false, "Bağlantı hatası")
            }
        })
    }
}
