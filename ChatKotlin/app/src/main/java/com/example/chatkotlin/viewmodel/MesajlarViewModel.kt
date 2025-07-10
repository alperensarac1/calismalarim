package com.example.chatkotlin.viewmodel

import androidx.lifecycle.*
import com.example.chatkotlin.model.Mesaj
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.service.response.MesajListResponse
import com.example.chatkotlin.service.response.SimpleResponse
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MesajlarViewModel : ViewModel() {

    private val _mesajlar = MutableLiveData<List<Mesaj>>()
    val mesajlar: LiveData<List<Mesaj>> get() = _mesajlar

    private val _hataMesaji = MutableLiveData<String?>()
    val hataMesaji: LiveData<String?> get() = _hataMesaji

    private val api = RetrofitClient.apiService
    private var mesajGuncelleJob: Job? = null

    // İlk çalıştırmada ve sonra 15 saniyede bir çağrılır
    fun mesajlariYuklePeriyodik(gonderenId: Int, aliciId: Int) {
        mesajGuncelleJob?.cancel() // eski varsa iptal et

        mesajGuncelleJob = viewModelScope.launch {
            while (isActive) {
                yukleMesajlar(gonderenId, aliciId)
                delay(15_000) // 15 saniye
            }
        }
    }

    private fun yukleMesajlar(gonderenId: Int, aliciId: Int) {
        api.mesajlariGetir(gonderenId, aliciId).enqueue(object : Callback<MesajListResponse> {
            override fun onResponse(call: Call<MesajListResponse>, response: Response<MesajListResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    _mesajlar.postValue(response.body()?.mesajlar ?: emptyList())
                    _hataMesaji.postValue(null)
                } else {
                    _hataMesaji.postValue("Mesajlar yüklenemedi")
                }
            }

            override fun onFailure(call: Call<MesajListResponse>, t: Throwable) {
                _hataMesaji.postValue("Hata: ${t.message}")
            }
        })
    }

    fun mesajGonder(
        gonderenId: Int,
        aliciId: Int,
        mesajText: String,
        base64Image: String? = null
    ) {
        val resimVar = if (base64Image.isNullOrEmpty()) 0 else 1

        api.mesajGonder(gonderenId, aliciId, mesajText, resimVar, base64Image)
            .enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        yukleMesajlar(gonderenId, aliciId)
                    } else {
                        _hataMesaji.postValue("Mesaj gönderilemedi")
                    }
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    _hataMesaji.postValue("Hata: ${t.message}")
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
        mesajGuncelleJob?.cancel()
    }
}
