package com.example.adisyonuygulamajetpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

// MasalarViewModel.kt
class MasalarViewModelFactory : ViewModel() {
    // … sizin paylaştığınız kod …
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MasalarViewModel() as T
        }
    }
}

// UrunViewModel.kt
class UrunViewModelFactory : ViewModel() {
    // … sizin paylaştığınız kod …
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                UrunViewModel() as T
        }
    }
}

// MassaDetayViewModel.kt
class MasaDetayViewModelFactory(private val masaId: Int) : ViewModel() {
    // … sizin paylaştığınız kod …
    companion object {
        fun provideFactory(masaId: Int) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MasaDetayViewModel(masaId) as T
        }
    }
}
