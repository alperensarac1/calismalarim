package com.example.onlinetaksi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onlinetaksi.data.repository.RideRepository

class CustomerHomeViewModelFactory(
    private val rideRepository: RideRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerHomeViewModel(rideRepository) as T
    }
}