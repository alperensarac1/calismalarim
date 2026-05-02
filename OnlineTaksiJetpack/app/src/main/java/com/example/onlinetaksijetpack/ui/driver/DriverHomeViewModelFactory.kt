package com.example.onlinetaksijetpack.ui.driver


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onlinetaksijetpack.data.repository.DriverRepository

class DriverHomeViewModelFactory(
    private val driverRepository: DriverRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DriverHomeViewModel(driverRepository) as T
    }
}