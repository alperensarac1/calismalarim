package com.example.sozlukjetpack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sozlukjetpack.dao.SozlukDao
import com.example.sozlukjetpack.model.SimpleResponse
import com.example.sozlukjetpack.util.AddEntryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntryEkleViewModel : ViewModel() {
    private val dao = SozlukDao()

    // UI state: loading / error
    private val _ui = MutableStateFlow(AddEntryUiState())
    val ui: StateFlow<AddEntryUiState> = _ui.asStateFlow()

    // Sonuç: SimpleResponse döner; null => henüz bir işlem yapılmadı
    private val _addResult = MutableStateFlow<SimpleResponse?>(null)
    val addResult: StateFlow<SimpleResponse?> = _addResult.asStateFlow()

    fun addEntry(userId: Int, title: String, content: String) {
        _ui.value = AddEntryUiState(loading = true, error = null)
        dao.addEntry(userId, title, content).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {
                _ui.value = _ui.value.copy(loading = false)
                if (response.isSuccessful) {
                    _addResult.value = response.body()
                    if (response.body() == null) {
                        _ui.value = _ui.value.copy(error = "Boş yanıt alındı")
                    }
                } else {
                    _ui.value = _ui.value.copy(error = "Sunucu hatası (${response.code()})")
                    _addResult.value = SimpleResponse(false, "Sunucu hatası")
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                _ui.value = _ui.value.copy(loading = false, error = "Bağlantı hatası")
                _addResult.value = SimpleResponse(false, "Bağlantı hatası")
            }
        })
    }

    // (İsteğe bağlı) Sonucu bir kez tüketip sıfırlamak için:
    fun clearResult() { _addResult.value = null }
}
