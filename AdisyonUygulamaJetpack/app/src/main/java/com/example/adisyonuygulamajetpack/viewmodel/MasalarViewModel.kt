package com.example.adisyonuygulamajetpack.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.qrkodolusturucu.services.ServicesImpl

import kotlinx.coroutines.launch

class MasalarViewModel : ViewModel() {

    private val _masalar = MutableLiveData<List<Masa>>(emptyList())
    val masalar: LiveData<List<Masa>> = _masalar

    private val _birlesmeSonucu = MutableLiveData<Boolean>(false)
    val birlesmeSonucu: LiveData<Boolean> = _birlesmeSonucu


    private val dao = ServicesImpl.getInstance()

    fun masalariYukle() {
        viewModelScope.launch {
            try {
                val liste = dao.masalariGetir()
                _masalar.value = liste
            } catch (e: Exception) {
                Log.e("MasalarVM", "Yükleme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun masaEkle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                dao.masaEkle()
                onSuccess()
            } catch (e: Exception) {
                Log.e("MasalarVM", "Masa ekleme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun masaSil(masaId: Int) {
        viewModelScope.launch {
            try {
                dao.masaSil(masaId)
            } catch (e: Exception) {
                Log.e("MasalarVM", "Masa silme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun masaBirlestir(anaId: Int, bId: Int) {
        viewModelScope.launch {
            try {
                dao.masaBirlestir(anaId, bId)
                _birlesmeSonucu.value = true
            } catch (e: Exception) {
                Log.e("MasalarVM", "Masa birleştirme hatası: ${e.localizedMessage}")
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

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MasalarViewModel() as T
            }
        }
    }
}
