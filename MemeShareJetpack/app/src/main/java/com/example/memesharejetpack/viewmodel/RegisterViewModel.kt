package com.example.memesharejetpack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memesharejetpack.model.KullaniciResponse
import com.example.memesharejetpack.service.ApiClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val _registerResult = MutableLiveData<KullaniciResponse>()
    val registerResult: LiveData<KullaniciResponse> get() = _registerResult

    fun registerUser(username: String, password: String) {
        ApiClient.getService().registerUser(username, password)
            .enqueue(object : Callback<KullaniciResponse> {
                override fun onResponse(
                    call: Call<KullaniciResponse>,
                    response: Response<KullaniciResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _registerResult.value = response.body()
                    } else {
                        _registerResult.value = KullaniciResponse(false, "Sunucu hatası", -1)
                    }
                }

                override fun onFailure(call: Call<KullaniciResponse>, t: Throwable) {
                    _registerResult.value =
                        KullaniciResponse(false, "Bağlantı hatası: ${t.message}", -1)
                }
            })
    }
}