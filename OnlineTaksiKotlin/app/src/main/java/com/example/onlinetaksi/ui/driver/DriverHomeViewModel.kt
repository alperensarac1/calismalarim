package com.example.onlinetaksi.ui.driver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinetaksi.data.remote.model.AvailableRideItem
import com.example.onlinetaksi.data.remote.model.RideResponse
import com.example.onlinetaksi.data.repository.DriverRepository
import com.example.onlinetaksi.util.Resource
import kotlinx.coroutines.launch

class DriverHomeViewModel(
    private val repo: DriverRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(DriverHomeUiState())
    val uiState: LiveData<DriverHomeUiState> = _uiState

    private val _acceptRideState = MutableLiveData<Resource<RideResponse>>()
    val acceptRideState: LiveData<Resource<RideResponse>> = _acceptRideState

    fun setLog(text: String) {
        val current = _uiState.value ?: DriverHomeUiState()
        _uiState.value = current.copy(lastLog = text)
    }

    fun setLocation(lat: Double, lng: Double) {
        val current = _uiState.value ?: DriverHomeUiState()
        _uiState.value = current.copy(
            currentLat = lat.toString(),
            currentLng = lng.toString()
        )
    }

    fun sendLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            repo.updateLocation(lat, lng)
        }
    }

    fun loadAvailableRides() {
        viewModelScope.launch {
            when (val result = repo.getAvailableRides()) {
                is Resource.Success -> {
                    val current = _uiState.value ?: DriverHomeUiState()
                    _uiState.value = current.copy(
                        availableRides = result.data.rides
                    )
                }
                is Resource.Error -> {
                    setLog("Ride listesi alınamadı: ${result.message}")
                }
                else -> {}
            }
        }
    }

    fun loadActiveRide() {
        viewModelScope.launch {
            when (val result = repo.getMyActiveRides()) {
                is Resource.Success -> {
                    val active = result.data.rides.firstOrNull()
                    val current = _uiState.value ?: DriverHomeUiState()
                    _uiState.value = current.copy(activeRide = active)
                }
                is Resource.Error -> {
                    setLog("Aktif ride alınamadı: ${result.message}")
                }
                else -> {}
            }
        }
    }

    fun addIncomingRide(ride: AvailableRideItem) {
        val current = _uiState.value ?: DriverHomeUiState()
        val updated = current.availableRides.toMutableList()

        val alreadyExists = updated.any { it.id == ride.id }
        if (!alreadyExists) {
            updated.add(0, ride)
        }

        _uiState.value = current.copy(availableRides = updated)
    }

    fun acceptRide(rideId: Int) {
        viewModelScope.launch {
            _acceptRideState.value = Resource.Loading

            val result = repo.acceptRide(rideId)
            _acceptRideState.value = result

            if (result is Resource.Success) {
                val current = _uiState.value ?: DriverHomeUiState()
                _uiState.value = current.copy(
                    activeRide = result.data,
                    availableRides = current.availableRides.filterNot { it.id == rideId }
                )
            }
        }
    }
    private val _onlineState = MutableLiveData<Boolean>()
    val onlineState: LiveData<Boolean> = _onlineState

    fun setOnline() {
        viewModelScope.launch {
            when (repo.setOnline()) {
                is Resource.Success -> {
                    _onlineState.value = true
                    setLog("Online oldun")
                }
                is Resource.Error -> {
                    setLog("Online hata")
                }
                else -> {}
            }
        }
    }

    fun setOffline() {
        viewModelScope.launch {
            when (repo.setOffline()) {
                is Resource.Success -> {
                    _onlineState.value = false
                    setLog("Offline oldun")
                }
                is Resource.Error -> {
                    setLog("Offline hata")
                }
                else -> {}
            }
        }
    }

    fun updateRideStatus(rideId: Int, status: String) {
        viewModelScope.launch {
            when (val result = repo.updateRideStatus(rideId, status)) {
                is Resource.Success -> {
                    val current = _uiState.value ?: DriverHomeUiState()
                    _uiState.value = current.copy(activeRide = result.data)
                    setLog("Status: $status")
                }
                is Resource.Error -> {
                    setLog("Status hata: ${result.message}")
                }
                else -> {}
            }
        }
    }
}