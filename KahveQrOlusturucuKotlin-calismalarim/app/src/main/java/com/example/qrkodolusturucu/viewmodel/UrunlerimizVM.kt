package com.example.qrkodolusturucu.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qrkodolusturucu.adapter.UrunAdapter
import com.example.qrkodolusturucu.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucu.model.Urun
import com.example.qrkodolusturucu.model.UrunKategori
import com.example.qrkodolusturucu.services.Services
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "UrunlerimizVM"
@HiltViewModel
class UrunlerimizVM @Inject constructor(
    private val kahveServisDao: Services,
) : ViewModel() {


    fun recyclerviewAyarla(urunKategori: UrunKategori, recyclerView: RecyclerView, context: Context) {
        viewModelScope.launch {
            try {
                val urunler = kahveServisDao.kahveWithKategoriId(urunKategori).kahve_urun
                val adapter = UrunAdapter(urunListesi = urunler)
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                recyclerView.adapter = adapter
                Log.d(TAG, "Alınan ürünler: ${urunler}")
            } catch (e: Exception) {
                Log.e(TAG, "Veri alırken hata oluştu: ${e.message}")
            }
        }
    }


    // İndirimli ürünleri getir
    fun indirimliRecyclerAyarla(recyclerView: RecyclerView, context: Context) {
        viewModelScope.launch {
            try {
                val urunler = kahveServisDao.tumKahveler().kahve_urun
                val indirimliUrunler = urunler.filter { it.urunIndirim == 1 }
                val adapter = UrunAdapter(urunListesi = indirimliUrunler)
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                recyclerView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
