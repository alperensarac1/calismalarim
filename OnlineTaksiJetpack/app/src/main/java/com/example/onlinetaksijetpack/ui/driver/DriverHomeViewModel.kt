package com.example.onlinetaksijetpack.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinetaksijetpack.data.remote.model.AvailableRideItem
import com.example.onlinetaksijetpack.data.remote.model.RideResponse
import com.example.onlinetaksijetpack.data.remote.socket.SocketManager
import com.example.onlinetaksijetpack.data.repository.DriverRepository
import com.example.onlinetaksijetpack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class DriverHomeViewModel(
    private val driverRepository: DriverRepository
) : ViewModel(), SocketManager.SocketEventListener {

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState

    fun connectSocket(token: String?) {
        if (token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(message = "Token yok")
            return
        }

        SocketManager.setListener(this)
        SocketManager.connect(token)
    }

    fun setOnline(isOnline: Boolean) {
        viewModelScope.launch {
            when (val result = driverRepository.setOnline(isOnline)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isOnline = result.data.is_online,
                        lastLog = if (isOnline) "Online oldun" else "Offline oldun",
                        message = if (isOnline) "Online oldun" else "Offline oldun"
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        message = result.message,
                        lastLog = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun loadAvailableRides() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAvailableRides = true)

            when (val result = driverRepository.getAvailableRides()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingAvailableRides = false,
                        availableRides = result.data.rides,
                        lastLog = "Açık ride listesi güncellendi"
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingAvailableRides = false,
                        message = result.message,
                        lastLog = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun loadActiveRide() {
        viewModelScope.launch {
            when (val result = driverRepository.getMyActiveRides()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        activeRide = result.data.rides.firstOrNull(),
                        lastLog = "Aktif ride güncellendi"
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        message = result.message,
                        lastLog = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun acceptRide(rideId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAcceptingRide = true)

            when (val result = driverRepository.acceptRide(rideId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isAcceptingRide = false,
                        activeRide = result.data,
                        availableRides = _uiState.value.availableRides.filterNot { it.id == rideId },
                        message = "Ride kabul edildi",
                        lastLog = "Ride kabul edildi. id=$rideId"
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAcceptingRide = false,
                        message = result.message,
                        lastLog = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun updateRideStatus(status: String, note: String? = null) {
        val activeRide = _uiState.value.activeRide ?: run {
            _uiState.value = _uiState.value.copy(message = "Aktif ride yok")
            return
        }

        viewModelScope.launch {
            when (val result = driverRepository.updateRideStatus(activeRide.id, status, note)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        activeRide = result.data,
                        lastLog = "Status güncellendi: $status",
                        message = "Status güncellendi: $status"
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        message = result.message,
                        lastLog = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun sendLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            currentLat = lat.toString(),
            currentLng = lng.toString()
        )

        viewModelScope.launch {
            driverRepository.updateLocation(lat, lng)
        }
    }

    fun addIncomingRide(item: AvailableRideItem) {
        val exists = _uiState.value.availableRides.any { it.id == item.id }
        if (!exists) {
            _uiState.value = _uiState.value.copy(
                availableRides = listOf(item) + _uiState.value.availableRides,
                lastLog = "Yeni ride geldi",
                message = "Yeni ride geldi"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    override fun onConnected() {
        _uiState.value = _uiState.value.copy(lastLog = "Socket bağlandı")
    }

    override fun onDisconnected() {
        _uiState.value = _uiState.value.copy(lastLog = "Socket bağlantısı kapandı")
    }

    override fun onMessage(message: String) {
        _uiState.value = _uiState.value.copy(lastLog = message)

        try {
            val json = JSONObject(message)
            val event = json.optString("event")

            if (event == "NEW_RIDE_REQUEST") {
                val data = json.optJSONObject("data")
                if (data != null) {
                    val incomingRide = AvailableRideItem(
                        id = data.optInt("ride_id"),
                        customer_id = data.optInt("customer_id"),
                        pickup_lat = data.optDouble("pickup_lat"),
                        pickup_lng = data.optDouble("pickup_lng"),
                        pickup_address = data.optString("pickup_address"),
                        dropoff_lat = data.optDouble("dropoff_lat"),
                        dropoff_lng = data.optDouble("dropoff_lng"),
                        dropoff_address = data.optString("dropoff_address"),
                        status = data.optString("status"),
                        estimated_fare = if (data.has("estimated_fare")) data.optDouble("estimated_fare") else null
                    )

                    addIncomingRide(incomingRide)
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(lastLog = "Socket parse hatası: ${e.message}")
        }
    }

    override fun onError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            message = errorMessage,
            lastLog = errorMessage
        )
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.setListener(null)
    }
}