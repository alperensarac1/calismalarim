package com.example.memesharejetpack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memesharejetpack.model.KullaniciResponse
import com.example.memesharejetpack.service.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<KullaniciResponse>()
    val loginResult: LiveData<KullaniciResponse> get() = _loginResult

    fun loginUser(username: String, password: String) {
        ApiClient.getService().loginUser(username, password)
            .enqueue(object : Callback<KullaniciResponse> {
                override fun onResponse(
                    call: Call<KullaniciResponse>,
                    response: Response<KullaniciResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        _loginResult.value = response.body()
                    } else {
                        _loginResult.value = KullaniciResponse(false, "Giriş başarısız", -1)
                    }
                }

                override fun onFailure(call: Call<KullaniciResponse>, t: Throwable) {
                    _loginResult.value =
                        KullaniciResponse(false, "Bağlantı hatası: ${t.message}", -1)
                }
            })
    }
}