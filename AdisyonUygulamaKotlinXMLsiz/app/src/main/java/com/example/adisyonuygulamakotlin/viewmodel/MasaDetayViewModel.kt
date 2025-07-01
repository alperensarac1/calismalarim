package com.example.adisyonuygulamakotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.services.retrofit.AdisyonServisDao
import kotlinx.coroutines.launch


class MasaDetayViewModel(private val masaId: Int) : ViewModel() {

    private val dao = AdisyonServisDao()

    private val _urunler = MutableLiveData<List<MasaUrun>>()             // masaya ait ürünler
    val urunler: LiveData<List<MasaUrun>> = _urunler

    private val _tumUrunler = MutableLiveData<List<Urun>>()              // tüm ürünler
    val tumUrunler: LiveData<List<Urun>> = _tumUrunler

    private val _kategoriler = MutableLiveData<List<Kategori>>()         // kategoriler
    val kategoriler: LiveData<List<Kategori>> = _kategoriler

    private val _toplamFiyat = MutableLiveData<Float>()                  // toplam fiyat
    val toplamFiyat: LiveData<Float> = _toplamFiyat

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _masa = MutableLiveData<Masa>()
    val masa: LiveData<Masa> = _masa


    fun yukleTumVeriler() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val masa = dao.masaGetir(masaId)
                _masa.value = masa

                val masaUrunleri = dao.masaUrunleriniGetir(masaId)
                val urunler = dao.urunleriGetir()
                val kategoriler = dao.kategorileriGetir()

                // Masadaki ürün adetlerini Urun listesine eşleştir
                val guncellenmisUrunler = urunler.map { urun ->
                    val eslesenMasaUrun = masaUrunleri.find { it.urun_id == urun.id }
                    if (eslesenMasaUrun != null) {
                        urun.copy(urun_adet = eslesenMasaUrun.adet)
                    } else {
                        urun.copy(urun_adet = 0)
                    }
                }

                // LiveData güncellemeleri
                _urunler.value = masaUrunleri
                _tumUrunler.value = guncellenmisUrunler
                _kategoriler.value = kategoriler
                _toplamFiyat.value = masaUrunleri.sumOf { it.toplam_fiyat.toDouble() }.toFloat()

            } catch (e: Exception) {
                Log.e("VM", "Veri yüklenemedi: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun urunEkle(urunId: Int, adet: Int = 1) {
        viewModelScope.launch {
            try {
                dao.urunEkle(masaId, urunId, adet)
                // Masa ürünlerini ve toplam fiyatı güncelle
                val masaUrunleri = dao.masaUrunleriniGetir(masaId)
                _urunler.postValue(masaUrunleri)
                _toplamFiyat.postValue(masaUrunleri.sumOf { it.toplam_fiyat.toDouble() }.toFloat())
                yukleTumVeriler()
2

            } catch (e: Exception) {
                Log.e("VM", "Ürün ekleme hatası: ${e.localizedMessage}")
            }
        }
    }


    fun urunCikar(urunId: Int) {
        viewModelScope.launch {
            dao.urunCikar(masaId, urunId)

            // tumUrunler listesindeki ilgili ürünün adetini azalt
            _tumUrunler.value = _tumUrunler.value?.map {
                if (it.id == urunId && it.urun_adet > 0) {
                    it.copy(urun_adet = it.urun_adet - 1)
                } else it
            }

            // masaya ait ürünleri ve toplam fiyatı güncelle
            val masaUrunleri = dao.masaUrunleriniGetir(masaId)
            _urunler.postValue(masaUrunleri)
            _toplamFiyat.postValue(masaUrunleri.sumOf { it.toplam_fiyat.toDouble() }.toFloat())
            yukleTumVeriler()
        }
    }

    val odemeTamamlandi = MutableLiveData<Boolean>(false)


    fun odemeAl(onSuccess: () -> Unit) {
        viewModelScope.launch {
            dao.masaOdemeYap(masaId)

            // tumUrunler listesindeki tüm adetleri sıfırla
            _tumUrunler.value = _tumUrunler.value?.map {
                it.copy(urun_adet = 0)
            }
            _toplamFiyat.value = 0f
            odemeTamamlandi.postValue(true)
            onSuccess()
        }
    }

}
