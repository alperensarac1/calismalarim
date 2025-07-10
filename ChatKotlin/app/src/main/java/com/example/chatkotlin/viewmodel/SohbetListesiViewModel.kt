package com.example.chatkotlin.viewmodel

import androidx.lifecycle.*
import com.example.chatkotlin.model.KonusulanKisi
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.service.response.KonusulanKisiListResponse
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SohbetListesiViewModel : ViewModel() {

    private val _konusulanKisiler = MutableLiveData<List<KonusulanKisi>>()
    val konusulanKisiler: LiveData<List<KonusulanKisi>> get() = _konusulanKisiler

    private val _hataMesaji = MutableLiveData<String?>()
    val hataMesaji: LiveData<String?> get() = _hataMesaji

    private var yenilemeJob: Job? = null

    fun sohbetListesiniBaslat(kullaniciId: Int) {
        yenilemeJob?.cancel()

        yenilemeJob = viewModelScope.launch {
            while (isActive) {
                sohbetListesiniGetir(kullaniciId)
                delay(15_000)
            }
        }
    }

    private fun sohbetListesiniGetir(kullaniciId: Int) {
        RetrofitClient.apiService.konusulanKisiler(kullaniciId)
            .enqueue(object : Callback<KonusulanKisiListResponse> {
                override fun onResponse(
                    call: Call<KonusulanKisiListResponse>,
                    response: Response<KonusulanKisiListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        _konusulanKisiler.postValue(response.body()?.kisiler ?: emptyList())
                        _hataMesaji.postValue(null)
                    } else {
                        _hataMesaji.postValue("Liste alınamadı")
                    }
                }

                override fun onFailure(call: Call<KonusulanKisiListResponse>, t: Throwable) {
                    _hataMesaji.postValue("Sunucu hatası: ${t.message}")
                }
            })
    }

    override fun onCleared() {
        super.onCleared()
        yenilemeJob?.cancel()
    }
}
