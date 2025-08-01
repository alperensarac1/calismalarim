package com.example.haberuygulamajetpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.model.YorumInsertRequest

import com.example.haberuygulamajetpack.model.YorumModel
import com.example.haberuygulamajetpack.servis.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// HaberDetayViewModel - Parametre ile başlatılıyor
class HaberDetayViewModel(private val haberDao: HaberDao = HaberDao(ApiClient.retrofit)) : ViewModel() {
    private val _yorumlar = MutableStateFlow<List<YorumModel>>(emptyList())
    val yorumlar: StateFlow<List<YorumModel>> = _yorumlar

    // Yorumları yükle
    fun loadYorumlar(haberId: Int) {
        viewModelScope.launch {
            _yorumlar.value = haberDao.getYorumlar(haberId) ?: emptyList()
        }
    }

    // Yorum ekle
    fun yorumEkle(haberId: Int, takmaAd: String, yorumMetni: String) {
        viewModelScope.launch {
            val response = haberDao.insertYorum(
                YorumInsertRequest(haberId, takmaAd, yorumMetni)
            )
            if (response?.success == true) {
                loadYorumlar(haberId)
            }
        }
    }
}
