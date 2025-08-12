package com.example.sozlukjetpack.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sozlukjetpack.dao.SozlukDao
import com.example.sozlukjetpack.model.SimpleResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KayitViewModel : ViewModel() {
    private val dao = SozlukDao()
    val registerResult = MutableLiveData<SimpleResponse>()

    fun register(username: String, password: String, email: String) {
        dao.register(username, password, email).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                registerResult.value = response.body()
            }
            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                registerResult.value = SimpleResponse(false, "Bağlantı hatası")
            }
        })
    }
}