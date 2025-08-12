package com.example.sozlukjetpack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sozlukjetpack.dao.SozlukDao
import com.example.sozlukjetpack.model.SimpleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GirisViewModel : ViewModel() {
    private val dao = SozlukDao()
    val loginResult = MutableLiveData<SimpleResponse>()

    fun login(username: String, password: String) {
        dao.login(username, password).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                loginResult.value = response.body()
            }
            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                loginResult.value = SimpleResponse(false, "Bağlantı hatası")
            }
        })
    }
}