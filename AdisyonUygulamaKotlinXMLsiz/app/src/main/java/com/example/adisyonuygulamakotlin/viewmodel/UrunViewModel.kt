package com.example.adisyonuygulamakotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.services.response.KategoriSilResponse
import com.example.adisyonuygulamakotlin.services.response.UrunSilResponse
import com.example.adisyonuygulamakotlin.services.retrofit.AdisyonServisDao
import kotlinx.coroutines.launch

class UrunViewModel : ViewModel() {
    private val dao = AdisyonServisDao()

    private val _urunler = MutableLiveData<List<Urun>>()
    val urunler: LiveData<List<Urun>> = _urunler
    private val _kategoriler = MutableLiveData<List<Kategori>>()
    val kategoriler : LiveData<List<Kategori>> = _kategoriler
    private val _sonuc = MutableLiveData<Boolean>()
    val sonuc: LiveData<Boolean> = _sonuc
    private val _silmeSonucu = MutableLiveData<UrunSilResponse>()
    val silmeSonucu: LiveData<UrunSilResponse> = _silmeSonucu

    private val _kategoriSilSonuc = MutableLiveData<KategoriSilResponse>()
    val kategoriSilSonuc: LiveData<KategoriSilResponse> = _kategoriSilSonuc


    fun urunleriYukle() {
        viewModelScope.launch {
            try {
                _urunler.value = dao.urunleriGetir()
            } catch (e: Exception) {
                Log.e("UrunVM", "Ürün yüklenemedi: ${e.localizedMessage}")
            }
        }
    }
    fun urunEkle(ad: String, fiyat: Float, resimBase64: String, kategoriId: Int) {
        viewModelScope.launch {
            try{
                dao.urunEkle(ad,fiyat,kategoriId, adet = 1, base64 = resimBase64)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    fun kategorileriYukle(){

        viewModelScope.launch {
            try{
                _kategoriler.value = dao.kategorileriGetir()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    fun kategoriEkle(ad: String) {
        viewModelScope.launch {
            dao.kategoriEkle(ad)
            _sonuc.postValue(true)
        }
    }
    fun kategoriSil(id: Int) {
        viewModelScope.launch {
            _kategoriSilSonuc.value = dao.kategoriSil(id)
        }
    }
    fun urunSil(ad: String) {
        viewModelScope.launch {
            try {
                val resp = dao.urunSil(ad)
                _silmeSonucu.value = resp
                if (resp.success) {
                    // Başarılıysa listeyi yenile
                    _urunler.value = dao.urunleriGetir()
                }
            } catch (e: Exception) {
                _silmeSonucu.value = UrunSilResponse(
                    success = false,
                    message = e.localizedMessage ?: "Bilinmeyen hata"
                )
            }
            println(silmeSonucu.value)
        }
    }
}
