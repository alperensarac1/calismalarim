package com.example.chatjetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatkotlin.model.KonusulanKisi
import com.example.chatkotlin.service.RetrofitClient

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SohbetListesiViewModel : ViewModel() {

    private val _konusulanKisiler = MutableStateFlow<List<KonusulanKisi>>(emptyList())
    val konusulanKisiler: StateFlow<List<KonusulanKisi>> = _konusulanKisiler.asStateFlow()

    private val _hataMesaji = MutableStateFlow<String?>(null)
    val hataMesaji: StateFlow<String?> = _hataMesaji.asStateFlow()

    private var yenilemeJob: Job? = null

    fun sohbetListesiniBaslat(kullaniciId: Int) {
        yenilemeJob?.cancel()

        yenilemeJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val response = RetrofitClient.apiService.konusulanKisiler(kullaniciId)
                    if (response.success) {
                        _konusulanKisiler.value = response.kisiler
                        _hataMesaji.value = null
                    } else {
                        _hataMesaji.value = "Liste alınamadı"
                    }
                } catch (e: Exception) {
                    _hataMesaji.value = "Sunucu hatası: ${e.message}"
                }

                delay(15_000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        yenilemeJob?.cancel()
    }
}