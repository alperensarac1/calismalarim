package com.example.onlinetaksijetpack.ui.customer


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.remote.model.CreateRideRequest
import com.example.onlinetaksijetpack.data.remote.model.RideResponse
import com.example.onlinetaksijetpack.data.remote.socket.SocketEventParser
import com.example.onlinetaksijetpack.data.remote.socket.SocketManager
import com.example.onlinetaksijetpack.data.repository.RideRepository
import com.example.onlinetaksijetpack.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerHomeViewModel(
    private val rideRepository: RideRepository,
    private val sessionManager: SessionManager
) : ViewModel(), SocketManager.SocketEventListener {

    private val _uiState = MutableStateFlow(CustomerHomeUiState())
    val uiState: StateFlow<CustomerHomeUiState> = _uiState

    private val _activeRide = MutableStateFlow<RideResponse?>(null)
    val activeRide: StateFlow<RideResponse?> = _activeRide

    fun connectSocket() {
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(message = "Token bulunamadı")
            return
        }

        SocketManager.setListener(this)
        SocketManager.connect(token)
    }

    fun sendPing() {
        SocketManager.sendPing()
    }

    fun logout() {
        SocketManager.disconnect()
        sessionManager.clearSession()
    }

    fun createRide(
        pickupLat: Double,
        pickupLng: Double,
        pickupAddress: String,
        dropoffLat: Double,
        dropoffLng: Double,
        dropoffAddress: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreatingRide = true,
                message = null
            )

            when (
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
            ) {
                is Resource.Success -> {
                    val ride = result.data
                    _activeRide.value = ride

                    _uiState.value = _uiState.value.copy(
                        isCreatingRide = false,
                        rideStatus = ride.status,
                        message = "Taksi çağrısı oluşturuldu",
                        pickupLat = ride.pickup_lat,
                        pickupLng = ride.pickup_lng,
                        dropoffLat = ride.dropoff_lat,
                        dropoffLng = ride.dropoff_lng
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreatingRide = false,
                        message = result.message
                    )
                }

                else -> {}
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    override fun onConnected() {
        _uiState.value = _uiState.value.copy(
            socketConnected = true,
            lastSocketEvent = "Socket bağlantısı kuruldu"
        )
    }

    override fun onDisconnected() {
        _uiState.value = _uiState.value.copy(
            socketConnected = false,
            lastSocketEvent = "Socket bağlantısı kapandı"
        )
    }

    override fun onMessage(message: String) {
        _uiState.value = _uiState.value.copy(lastSocketEvent = message)

        when (SocketEventParser.getEventName(message)) {
            "RIDE_ACCEPTED", "RIDE_STATUS_CHANGED", "RIDE_CANCELLED" -> {
                SocketEventParser.parseRideStatus(message)?.let { status ->
                    _uiState.value = _uiState.value.copy(rideStatus = status)
                }
            }

            "DRIVER_LOCATION" -> {
                SocketEventParser.parseDriverLocation(message)?.let { location ->
                    _uiState.value = _uiState.value.copy(
                        driverLatText = location.lat.toString(),
                        driverLngText = location.lng.toString(),
                        lastLocationUpdateText = currentTime(),
                        driverLat = location.lat,
                        driverLng = location.lng
                    )
                }
            }

            "PONG" -> {
                _uiState.value = _uiState.value.copy(
                    lastSocketEvent = "Sunucudan pong alındı"
                )
            }
        }
    }

    override fun onError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(
            socketConnected = false,
            message = errorMessage
        )
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.setListener(null)
    }

    private fun currentTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }
}