package com.example.onlinetaksi.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.onlinetaksi.data.repository.DriverRepository

class DriverHomeViewModelFactory(
    private val repository: DriverRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DriverHomeViewModel(repository) as T
    }
}