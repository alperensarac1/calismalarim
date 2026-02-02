package com.example.kargopaylasimkotlin.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kargopaylasimkotlin.repo.CargoRepository
import com.example.kargopaylasimkotlin.service.TokenStore
import com.example.kargopaylasimkotlin.viewmodel.AddressListViewModel
import com.example.kargopaylasimkotlin.viewmodel.AddressViewModel
import com.example.kargopaylasimkotlin.viewmodel.AuthViewModel
import com.example.kargopaylasimkotlin.viewmodel.ShipmentViewModel


class AuthVmFactory(private val repo: CargoRepository, private val tokenStore: TokenStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repo, tokenStore) as T
    }
}

class ShipmentVmFactory(private val repo: CargoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShipmentViewModel(repo) as T
    }
}
class AddressVmFactory(private val repo: CargoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddressViewModel(repo) as T
    }
}
class AddressListVmFactory(private val repo: CargoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddressListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddressListViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
