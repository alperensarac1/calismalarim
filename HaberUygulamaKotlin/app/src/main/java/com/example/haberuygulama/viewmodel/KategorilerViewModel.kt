package com.example.haberuygulama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haberuygulama.deo.HaberDao
import com.example.haberuygulama.model.HaberModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class KategorilerViewModel(private val haberDao: HaberDao) : ViewModel() {

    private val _kategoriHaberleri = MutableStateFlow<List<HaberModel>>(emptyList())
    val kategoriHaberleri: StateFlow<List<HaberModel>> = _kategoriHaberleri

    fun loadKategoriHaberleri(turId: Int) {
        viewModelScope.launch {
            val tumHaberler = haberDao.getHaberler()
            _kategoriHaberleri.value = tumHaberler?.filter { it.tur_id == turId } ?: emptyList()
        }
    }
}