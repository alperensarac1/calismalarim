package com.example.haberuygulama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.model.HaberModel
import com.example.haberuygulama.model.HaberTuruModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HaberlerViewModel(private val haberDao: HaberDao) : ViewModel() {

    private val _tumHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    private val _filtrelenmisHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val haberler: StateFlow<List<HaberModel>> = _filtrelenmisHaberler

    private val _kategoriler = MutableStateFlow<List<HaberTuruModel>>(emptyList())
    val kategoriler: StateFlow<List<HaberTuruModel>> = _kategoriler

    private val _sonHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val sonHaberler: StateFlow<List<HaberModel>> = _sonHaberler

    private val _gundemHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val gundemHaberler: StateFlow<List<HaberModel>> = _gundemHaberler
    private val _sonDakikaHaberler = MutableStateFlow<List<HaberModel>>(emptyList())
    val sonDakikaHaberler: StateFlow<List<HaberModel>> = _sonDakikaHaberler

    fun loadData() {
        viewModelScope.launch {
            val haberList = haberDao.getHaberler() ?: emptyList()
            _tumHaberler.value = haberList
            _filtrelenmisHaberler.value = haberList
            _kategoriler.value = haberDao.getKategoriler() ?: emptyList()
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



