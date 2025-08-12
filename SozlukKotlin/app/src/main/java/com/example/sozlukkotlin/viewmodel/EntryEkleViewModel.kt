package com.example.sozlukkotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sozlukkotlin.dao.SozlukDao
import com.example.sozlukkotlin.model.SimpleResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EntryEkleViewModel : ViewModel() {
    private val dao = SozlukDao()
    val addResult = MutableLiveData<SimpleResponse>()

    fun addEntry(userId: Int, title: String, content: String) {
        dao.addEntry(userId, title, content).enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                addResult.value = response.body()
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                addResult.value = SimpleResponse(false, "Bağlantı hatası")
            }
        })
    }
}
