package com.example.adisyonuygulamajetpack.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.qrkodolusturucu.services.ServicesImpl

import kotlinx.coroutines.launch

class UrunViewModel : ViewModel() {

    private val _urunler = MutableLiveData<List<Urun>>(emptyList())
    val urunler: LiveData<List<Urun>> = _urunler

    private val _kategoriler = MutableLiveData<List<Kategori>>(emptyList())
    val kategoriler: LiveData<List<Kategori>> = _kategoriler

    // Retrofit DAO
    private val dao = ServicesImpl.getInstance()

    fun urunleriYukle() {
        viewModelScope.launch {
            try {
                _urunler.value = dao.urunleriGetir()
            } catch (e: Exception) {
                Log.e("UrunVM", "Ürün yüklenemedi: ${e.localizedMessage}")
            }
        }
    }

    fun kategorileriYukle() {
        viewModelScope.launch {
            try {
                _kategoriler.value = dao.kategorileriGetir()
            } catch (e: Exception) {
                Log.e("UrunVM", "Kategori yüklenemedi: ${e.localizedMessage}")
            }
        }
    }

    fun urunEkle(ad: String, fiyat: Float, kategoriId: Int, base64: String,) {
        viewModelScope.launch {
            try {
                dao.urunEkle(ad, fiyat,kategoriId,0, base64)
            } catch (e: Exception) {
                Log.e("UrunVM", "Ürün ekleme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun urunSil(urunAd: String) {
        viewModelScope.launch {
            try {
                dao.urunSil(urunAd)
            } catch (e: Exception) {
                Log.e("UrunVM", "Ürün silme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun kategoriEkle(ad: String) {
        viewModelScope.launch {
            try {
                dao.kategoriEkle(ad)
            } catch (e: Exception) {
                Log.e("UrunVM", "Kategori ekleme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun kategoriSil(kategoriId: Int) {
        viewModelScope.launch {
            try {
                dao.kategoriSil(kategoriId)
            } catch (e: Exception) {
                Log.e("UrunVM", "Kategori silme hatası: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UrunViewModel() as T
            }
        }
    }
}
