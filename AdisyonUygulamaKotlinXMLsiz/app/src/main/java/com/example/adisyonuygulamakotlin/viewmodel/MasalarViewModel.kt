package com.example.adisyonuygulamakotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.qrkodolusturucu.services.ServicesImpl
import kotlinx.coroutines.launch

class MasalarViewModel : ViewModel() {

    private val _masalar = MutableLiveData<List<Masa>>()
    private val _birlesmeSonucu = MutableLiveData<Boolean>(false)
    val birlesmeSonucu: LiveData<Boolean> = _birlesmeSonucu
    val masalar: LiveData<List<Masa>> get() = _masalar
    val dao = ServicesImpl.getInstance()
    fun masalariYukle() {
        viewModelScope.launch {
            try {
                val liste = ServicesImpl.getInstance().masalariGetir()
                _masalar.value = liste
            } catch (e: Exception) {
                Log.e("MasalarVM", "Yükleme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun guncelleMasa(masa: Masa) {
        val liste = _masalar.value?.toMutableList() ?: return
        val index = liste.indexOfFirst { it.id == masa.id }
        if (index != -1) {
            liste[index] = masa
            _masalar.value = liste
        }
    }
    fun masaEkle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                dao.masaEkle() // retrofit servisi
            } catch (e: Exception) {
                Log.e("MasaVM", "Hata: ${e.localizedMessage}")
            }
        }
    }
    fun masaSil(masaId:Int){
        viewModelScope.launch {
            try{
                dao.masaSil(masaId)
            }catch (e:Exception){
                Log.e("hata","hata")
            }
        }
    }
    fun masaBirlestir(anaId: Int, bId: Int) {
        viewModelScope.launch {

            try {
                dao.masaBirlestir(anaId, bId)
                _birlesmeSonucu.value = true
            } catch (e: Exception) {

            }
        }
    }

}
