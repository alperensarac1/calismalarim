package com.example.qrkodolusturucujetpack.usecases

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.UUID

class DogrulamaKoduOlustur {
    private val _dogrulamaKoduLiveData = MutableLiveData<String>()

    val dogrulamaKoduLiveData: LiveData<String> get() = _dogrulamaKoduLiveData

    fun dogrulamaKodunuOlustur(){
        _dogrulamaKoduLiveData.postValue(randomIdOlustur())
    }
    fun randomIdOlustur():String = UUID.randomUUID().toString()
}