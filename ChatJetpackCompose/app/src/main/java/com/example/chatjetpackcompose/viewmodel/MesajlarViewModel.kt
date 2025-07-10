package com.example.chatjetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatkotlin.model.Mesaj
import com.example.chatkotlin.service.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MesajlarViewModel : ViewModel() {

    private val _mesajlar = MutableStateFlow<List<Mesaj>>(emptyList())
    val mesajlar: StateFlow<List<Mesaj>> = _mesajlar.asStateFlow()

    private val _hataMesaji = MutableStateFlow<String?>(null)
    val hataMesaji: StateFlow<String?> = _hataMesaji.asStateFlow()

    private val api = RetrofitClient.apiService
    private var mesajGuncelleJob: Job? = null

    fun mesajlariYuklePeriyodik(gonderenId: Int, aliciId: Int) {
        mesajGuncelleJob?.cancel()

        mesajGuncelleJob = viewModelScope.launch {
            while (isActive) {
                yukleMesajlar(gonderenId, aliciId)
                delay(15_000)
            }
        }
    }

    private suspend fun yukleMesajlar(gonderenId: Int, aliciId: Int) {
        try {
            val response = api.mesajlariGetir(gonderenId, aliciId)
            if (response.success) {
                _mesajlar.value = response.mesajlar
                _hataMesaji.value = null
            } else {
                _hataMesaji.value = "Mesajlar yüklenemedi"
            }
        } catch (e: Exception) {
            _hataMesaji.value = "Hata: ${e.message}"
        }
    }

    fun mesajGonder(
        gonderenId: Int,
        aliciId: Int,
        mesajText: String,
        base64Image: String? = null
    ) {
        val resimVar = if (base64Image.isNullOrEmpty()) 0 else 1

        viewModelScope.launch {
            try {
                val response = api.mesajGonder(
                    gonderenId,
                    aliciId,
                    mesajText,
                    resimVar,
                    base64Image
                )

                if (response.success) {
                    yukleMesajlar(gonderenId, aliciId)
                } else {
                    _hataMesaji.value = response.error ?: "Mesaj gönderilemedi"
                }

            } catch (e: Exception) {
                _hataMesaji.value = "Hata: ${e.message}"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mesajGuncelleJob?.cancel()
    }
}