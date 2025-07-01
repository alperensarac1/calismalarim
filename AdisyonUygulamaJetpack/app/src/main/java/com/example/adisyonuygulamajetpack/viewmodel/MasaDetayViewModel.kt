package com.example.adisyonuygulamakotlin.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.services.retrofit.AdisyonServisDao
import kotlinx.coroutines.launch

class MasaDetayViewModel(private val masaId: Int) : ViewModel() {

    private val dao = AdisyonServisDao()

    private val _urunler = MutableLiveData<List<MasaUrun>>(emptyList())
    val urunler: LiveData<List<MasaUrun>> = _urunler

    private val _tumUrunler = MutableLiveData<List<Urun>>(emptyList())
    val tumUrunler: LiveData<List<Urun>> = _tumUrunler

    private val _kategoriler = MutableLiveData<List<Kategori>>(emptyList())
    val kategoriler: LiveData<List<Kategori>> = _kategoriler

    private val _toplamFiyat = MutableLiveData<Float>(0f)
    val toplamFiyat: LiveData<Float> = _toplamFiyat

    private val _masa = MutableLiveData<Masa>()
    val masa: LiveData<Masa> = _masa

    val odemeTamamlandi = MutableLiveData<Boolean>(false)

    fun yukleTumVeriler() {
        viewModelScope.launch {
            try {
                // 1) Masa bilgisini çek
                val m = dao.masaGetir(masaId)
                _masa.value = m

                // 2) Masaya ait ürünleri çek ve tüm ürünleri çek
                val masaUrunleri = dao.masaUrunleriniGetir(masaId)
                val urunler = dao.urunleriGetir()
                val kategoriler = dao.kategorileriGetir()

                // 3) Ürün listesini güncelle (adetleri eşitle)
                val updatedUrunler: List<Urun> = urunler.map { urun ->
                    val mu = masaUrunleri.find { it.urun_id == urun.id }
                    if (mu != null) urun.copy(urun_adet = mu.adet)
                    else urun.copy(urun_adet = 0)
                }

                _urunler.value = masaUrunleri
                _tumUrunler.value = updatedUrunler
                _kategoriler.value = kategoriler
                _toplamFiyat.value = masaUrunleri.sumOf { it.toplam_fiyat.toDouble() }.toFloat()
            } catch (e: Exception) {
                Log.e("MasaDetayVM", "Veri yüklenemedi: ${e.localizedMessage}")
            }
        }
    }

    fun urunEkle(urunId: Int, adet: Int = 1) {
        viewModelScope.launch {
            try {
                dao.urunEkle(masaId, urunId, adet)
                // Yüklemeyi yeniden yap:
                yukleTumVeriler()
            } catch (e: Exception) {
                Log.e("MasaDetayVM", "Ürün ekleme hatası: ${e.localizedMessage}")
            }
        }
    }

    fun urunCikar(urunId: Int) {
        viewModelScope.launch {
            try {
                dao.urunCikar(masaId, urunId)
                yukleTumVeriler()
            } catch (e: Exception) {
                Log.e("MasaDetayVM", "Ürün çıkarma hatası: ${e.localizedMessage}")
            }
        }
    }

    fun odemeAl(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                dao.masaOdemeYap(masaId)
                _toplamFiyat.value = 0f
                odemeTamamlandi.value = true
                onSuccess()
            } catch (e: Exception) {
                Log.e("MasaDetayVM", "Ödeme hatası: ${e.localizedMessage}")
            }
        }
    }

    companion object {
        fun provideFactory(masaId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MasaDetayViewModel(masaId) as T
            }
        }
    }
}
