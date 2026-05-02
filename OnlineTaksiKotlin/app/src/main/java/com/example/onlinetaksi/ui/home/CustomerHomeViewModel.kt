package com.example.onlinetaksi.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinetaksi.data.remote.model.CreateRideRequest
import com.example.onlinetaksi.data.remote.model.RideResponse
import com.example.onlinetaksi.data.repository.RideRepository
import com.example.onlinetaksi.util.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerHomeViewModel(
    private val rideRepository: RideRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(CustomerHomeUiState())
    val uiState: LiveData<CustomerHomeUiState> = _uiState

    private val _createRideState = MutableLiveData<Resource<RideResponse>>()
    val createRideState: LiveData<Resource<RideResponse>> = _createRideState

    fun createRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String
    ) {
        viewModelScope.launch {
            _createRideState.value = Resource.Loading

            val result = rideRepository.createRide(
                CreateRideRequest(
                    pickup_lat = pickupLat,
                    pickup_lng = pickupLng,
                    pickup_address = pickupAddress,
                    dropoff_lat = dropoffLat,
                    dropoff_lng = dropoffLng,
                    dropoff_address = dropoffAddress
                )
            )

            _createRideState.value = result
        }
    }

    fun onSocketConnected() {
        val current = _uiState.value ?: CustomerHomeUiState()
        _uiState.value = current.copy(
            socketConnected = true,
            lastSocketEvent = "Socket bağlantısı kuruldu"
        )
    }

    fun onSocketDisconnected() {
        val current = _uiState.value ?: CustomerHomeUiState()
        _uiState.value = current.copy(
            socketConnected = false,
            lastSocketEvent = "Socket bağlantısı kapandı"
        )
    }

    fun onSocketError(message: String) {
        val current = _uiState.value ?: CustomerHomeUiState()
        _uiState.value = current.copy(
            lastSocketEvent = "Socket hata: $message"
        )
    }

    fun onRawSocketMessage(message: String) {
        val current = _uiState.value ?: CustomerHomeUiState()
        _uiState.value = current.copy(
            lastSocketEvent = message
        )
    }

    fun onRideStatusChanged(status: String) {
        val current = _uiState.value ?: CustomerHomeUiState()
        _uiState.value = current.copy(
            rideStatus = status
        )
    }

    fun onDriverLocationUpdated(lat: Double, lng: Double) {
        val current = _uiState.value ?: CustomerHomeUiState()
        _uiState.value = current.copy(
            driverLatText = lat.toString(),
            driverLngText = lng.toString(),
            lastLocationUpdateText = getCurrentFormattedTime()
        )
    }

    private fun getCurrentFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}