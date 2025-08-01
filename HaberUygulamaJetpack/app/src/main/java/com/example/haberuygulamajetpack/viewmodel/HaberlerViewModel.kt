package com.example.haberuygulamajetpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.model.HaberTuruModel
import com.example.haberuygulamajetpack.servis.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HaberlerViewModel : ViewModel() {

    private val _tumHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    private val _filtrelenmisHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val haberler: StateFlow<List<HaberModel>> = _filtrelenmisHaberler

    private val _kategoriler = MutableStateFlow<List<HaberTuruModel>>(emptyList())
    val kategoriler: StateFlow<List<HaberTuruModel>> = _kategoriler

    private val _haber = MutableStateFlow<HaberModel?>(null)
    val haber: StateFlow<HaberModel?> = _haber

    private val _sonHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val sonHaberler: StateFlow<List<HaberModel>> = _sonHaberler

    private val _gundemHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val gundemHaberler: StateFlow<List<HaberModel>> = _gundemHaberler

    private val _sonDakikaHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val sonDakikaHaberler: StateFlow<List<HaberModel>> = _sonDakikaHaberler

    // HaberDao Singleton veya başka bir yolla alınabilir
    private val haberDao = HaberDao(ApiClient.retrofit)

    fun loadData() {
        viewModelScope.launch {
            val haberList = haberDao.getHaberler() ?: emptyList()
            _tumHaberler.value = haberList
            _filtrelenmisHaberler.value = haberList
            _kategoriler.value = haberDao.getKategoriler() ?: emptyList()
        }
    }

    fun getHaberById(haberId: Int) {
        viewModelScope.launch {
            val response = haberDao.getHaberById(haberId)
            _haber.value = response
        }
    }

    fun filtreleKategori(turId: Int?) {
        viewModelScope.launch {
            _filtrelenmisHaberler.value = if (turId == null) {
                _tumHaberler.value
            } else {
                _tumHaberler.value.filter { it.tur_id == turId }
            }
        }
    }

    fun loadSon3Haber() {
        viewModelScope.launch {
            _sonHaberler.value = haberDao.getSon3Haber() ?: emptyList()
        }
    }

    fun loadGundemHaberler() {
        viewModelScope.launch {
            _gundemHaberler.value = haberDao.getGundemHaberler() ?: emptyList()
        }
    }

    fun loadSonDakikaHaberler() {
        viewModelScope.launch {
            _sonDakikaHaberler.value = haberDao.getSonDakikaHaberler() ?: emptyList()
        }
    }

    fun loadKategoriler() {
        viewModelScope.launch {
            _kategoriler.value = haberDao.getKategoriler() ?: emptyList()
        }
    }
}
