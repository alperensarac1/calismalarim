package com.example.adisyonuygulamakotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class MasaDetayVMFactory(private val masaId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MasaDetayViewModel(masaId) as T
    }
}
