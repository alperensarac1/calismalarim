package com.example.qrkodolusturucujetpack.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucujetpack.model.Urun
import com.example.qrkodolusturucujetpack.model.UrunKategori
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UrunlerimizVM @Inject constructor(
    private val kahveServisDao: Services
) : ViewModel() {

    var icecekler by mutableStateOf<List<Urun>>(emptyList())
        private set

    var atistirmaliklar by mutableStateOf<List<Urun>>(emptyList())
        private set

    var kampanyalar by mutableStateOf<List<Urun>>(emptyList())
        private set

    init {
        urunleriYukle()
    }

    private fun urunleriYukle() {
        viewModelScope.launch {
            try {
                icecekler = kahveServisDao.kahveWithKategoriId(UrunKategori.ICECEKLER).kahve_urun
                atistirmaliklar = kahveServisDao.kahveWithKategoriId(UrunKategori.ATISTIRMALIKLAR).kahve_urun
                val tumUrunler = kahveServisDao.tumKahveler().kahve_urun
                kampanyalar = tumUrunler.filter { it.urunIndirim == 1 }
            } catch (e: Exception) {
                Log.e("UrunlerimizVM", "Veri alırken hata: ${e.message}")
            }
        }
    }
}
