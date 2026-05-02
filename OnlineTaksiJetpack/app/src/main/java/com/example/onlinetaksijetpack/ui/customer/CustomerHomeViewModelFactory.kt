package com.example.onlinetaksijetpack.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.repository.RideRepository

class CustomerHomeViewModelFactory(
    private val rideRepository: RideRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerHomeViewModel(rideRepository, sessionManager) as T
    }
}