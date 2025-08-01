package com.example.haberuygulama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.model.YorumInsertRequest
import com.example.haberuygulama.model.YorumModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HaberDetayViewModel(private val haberDao: HaberDao) : ViewModel() {
    private val _yorumlar = MutableStateFlow<List<YorumModel>>(emptyList())
    val yorumlar: StateFlow<List<YorumModel>> = _yorumlar

    fun loadYorumlar(haberId: Int) {
        viewModelScope.launch {
            _yorumlar.value = haberDao.getYorumlar(haberId) ?: emptyList()
        }
    }

    fun yorumEkle(haberId: Int, takmaAd: String, yorumMetni: String) {
        viewModelScope.launch {
            val response = haberDao.insertYorum(
                YorumInsertRequest(
                    haber_id = haberId,
                    takma_ad = takmaAd,
                    yorum_metni = yorumMetni
                )
            )
            if (response?.success == true) {
                loadYorumlar(haberId) // yeniden yükle
            }
        }
    }
}