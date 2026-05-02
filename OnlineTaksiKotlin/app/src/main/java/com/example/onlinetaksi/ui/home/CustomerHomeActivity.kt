package com.example.onlinetaksi.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.onlinetaksi.data.local.SessionManager
import com.example.onlinetaksi.data.remote.api.ApiClient
import com.example.onlinetaksi.data.remote.socket.SocketEventParser
import com.example.onlinetaksi.data.remote.socket.SocketManager
import com.example.onlinetaksi.data.repository.RideRepository
import com.example.onlinetaksi.databinding.ActivityCustomerHomeBinding
import com.example.onlinetaksi.util.Resource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class CustomerHomeActivity : AppCompatActivity(),
    SocketManager.SocketEventListener,
    OnMapReadyCallback {

    private lateinit var binding: ActivityCustomerHomeBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: CustomerHomeViewModel by viewModels {
        CustomerHomeViewModelFactory(
            rideRepository = RideRepository(ApiClient.create(this))
        )
    }

    private var googleMap: GoogleMap? = null

    private var customerMarker: Marker? = null
    private var pickupMarker: Marker? = null
    private var dropoffMarker: Marker? = null
    private var driverMarker: Marker? = null

    private var customerLatLng: LatLng? = null
    private var pickupLatLng: LatLng? = null
    private var dropoffLatLng: LatLng? = null
    private var driverLatLng: LatLng? = null

    private var activeRideId: Int? = null

    private var currentPickupLat: Double? = null
    private var currentPickupLng: Double? = null
    private var currentPickupAddress: String = "Mevcut konum"

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCustomerLocation()
            } else {
                appendLog("Müşteri konum izni verilmedi")
                Toast.makeText(this, "Konum izni gerekli", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.tvWelcome.text = "Hoş geldin, ${sessionManager.getFullName() ?: "Müşteri"}"

        val mapFragment =
            supportFragmentManager.findFragmentById(binding.mapContainer.id) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        binding.btnConnectSocket.setOnClickListener {
            val token = sessionManager.getToken()
            if (token.isNullOrBlank()) {
                Toast.makeText(this, "Token bulunamadı", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            SocketManager.setListener(this)
            SocketManager.connect(token)
        }

        binding.btnPing.setOnClickListener {
            SocketManager.sendPing()
        }

        binding.btnCreateRide.setOnClickListener {
            createRideFromForm()
        }

        binding.btnLogout.setOnClickListener {
            SocketManager.disconnect()
            sessionManager.clearSession()
            finish()
        }

        observeUiState()
        observeCreateRideState()
        checkLocationPermissionAndFetch()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val defaultLocation = LatLng(41.0082, 28.9784)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 11f))

        map.setOnMapClickListener { clickedLatLng ->
            dropoffLatLng = clickedLatLng

            binding.etDropoffLat.setText(clickedLatLng.latitude.toString())
            binding.etDropoffLng.setText(clickedLatLng.longitude.toString())

            if (binding.etDropoffAddress.text.toString().isBlank()) {
                binding.etDropoffAddress.setText("Haritadan seçilen konum")
            }

            updateDropoffMarker(clickedLatLng)
            updateCameraForAllPoints()

            appendLog("Dropoff haritadan seçildi: ${clickedLatLng.latitude}, ${clickedLatLng.longitude}")
        }

        customerLatLng?.let {
            updateCustomerMarker(it)
            updatePickupMarker(it)
            updateCameraForAllPoints()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.setListener(null)
    }

    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            binding.tvSocketState.text =
                if (state.socketConnected) "Socket Durumu: Bağlı"
                else "Socket Durumu: Bağlı değil"

            binding.tvRideStatus.text = "Ride Durumu: ${state.rideStatus}"
            binding.tvDriverLat.text = "Taksi Enlem: ${state.driverLatText}"
            binding.tvDriverLng.text = "Taksi Boylam: ${state.driverLngText}"
            binding.tvLastLocationUpdate.text =
                "Son Konum Güncelleme: ${state.lastLocationUpdateText}"
            binding.tvLastSocketEvent.text = "Son Event: ${state.lastSocketEvent}"
        }
    }

    private fun observeCreateRideState() {
        viewModel.createRideState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnCreateRide.isEnabled = false
                    binding.btnCreateRide.text = "Çağırılıyor..."
                }

                is Resource.Success -> {
                    binding.btnCreateRide.isEnabled = true
                    binding.btnCreateRide.text = "Taksi Çağır"

                    val ride = result.data
                    activeRideId = ride.id

                    pickupLatLng = LatLng(ride.pickup_lat, ride.pickup_lng)
                    dropoffLatLng = LatLng(ride.dropoff_lat, ride.dropoff_lng)

                    updatePickupMarker(pickupLatLng!!)
                    updateDropoffMarker(dropoffLatLng!!)
                    updateCameraForAllPoints()

                    viewModel.onRideStatusChanged(ride.status)

                    appendLog("Ride oluşturuldu. id=${ride.id}")
                    Toast.makeText(this, "Taksi çağrısı oluşturuldu", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    binding.btnCreateRide.isEnabled = true
                    binding.btnCreateRide.text = "Taksi Çağır"

                    appendLog("Ride oluşturma hatası: ${result.message}")
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onConnected() {
        runOnUiThread {
            viewModel.onSocketConnected()
            appendLog("Socket bağlandı")
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            viewModel.onSocketDisconnected()
            appendLog("Socket bağlantısı kapandı")
        }
    }

    override fun onMessage(message: String) {
        runOnUiThread {
            viewModel.onRawSocketMessage(message)
            appendLog("Mesaj: $message")

            when (SocketEventParser.getEventName(message)) {
                "AUTH_SUCCESS" -> {
                    appendLog("Kimlik doğrulama başarılı")
                }

                "RIDE_ACCEPTED" -> {
                    appendLog("Şoför ride kabul etti")
                    SocketEventParser.parseRideStatus(message)?.let { status ->
                        viewModel.onRideStatusChanged(status)
                    }
                }

                "RIDE_STATUS_CHANGED" -> {
                    appendLog("Ride durumu değişti")
                    SocketEventParser.parseRideStatus(message)?.let { status ->
                        viewModel.onRideStatusChanged(status)
                    }
                }

                "RIDE_CANCELLED" -> {
                    appendLog("Ride iptal edildi")
                    SocketEventParser.parseRideStatus(message)?.let { status ->
                        viewModel.onRideStatusChanged(status)
                    }
                }

                "DRIVER_LOCATION" -> {
                    appendLog("Taksi konumu güncellendi")

                    val locationEvent = SocketEventParser.parseDriverLocation(message)
                    if (locationEvent != null) {
                        activeRideId = locationEvent.rideId

                        viewModel.onDriverLocationUpdated(
                            lat = locationEvent.lat,
                            lng = locationEvent.lng
                        )

                        updateDriverMarker(locationEvent.lat, locationEvent.lng)
                    }
                }

                "PONG" -> {
                    appendLog("Sunucudan pong alındı")
                }
            }
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            viewModel.onSocketError(errorMessage)
            appendLog("Socket hata: $errorMessage")
        }
    }

    private fun createRideFromForm() {
        val pickupLat = currentPickupLat
        val pickupLng = currentPickupLng
        val pickupAddress = currentPickupAddress

        val dropoffLat = binding.etDropoffLat.text.toString().trim().toDoubleOrNull()
        val dropoffLng = binding.etDropoffLng.text.toString().trim().toDoubleOrNull()
        val dropoffAddress = binding.etDropoffAddress.text.toString().trim()

        if (pickupLat == null || pickupLng == null) {
            Toast.makeText(this, "Telefon konumu alınamadı. Tekrar deneniyor.", Toast.LENGTH_SHORT).show()
            checkLocationPermissionAndFetch()
            return
        }

        if (dropoffLat == null || dropoffLng == null || dropoffAddress.isBlank()) {
            Toast.makeText(this, "Varış bilgilerini doldur", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createRide(
            pickupLat = pickupLat,
            pickupLng = pickupLng,
            pickupAddress = pickupAddress,
            dropoffLat = dropoffLat,
            dropoffLng = dropoffLng,
            dropoffAddress = dropoffAddress
        )
    }

    private fun checkLocationPermissionAndFetch() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            fetchCustomerLocation()
        } else {
            locationPermissionLauncher.launch(permission)
        }
    }

    private fun fetchCustomerLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)

                    currentPickupLat = location.latitude
                    currentPickupLng = location.longitude
                    currentPickupAddress = "Mevcut konum"

                    customerLatLng = latLng
                    pickupLatLng = latLng

                    binding.tvPickupInfo.text =
                        "Alınış noktası: ${location.latitude}, ${location.longitude}"

                    updateCustomerMarker(latLng)
                    updatePickupMarker(latLng)
                    updateCameraForAllPoints()

                    appendLog("Telefon konumu pickup olarak ayarlandı: ${location.latitude}, ${location.longitude}")
                } else {
                    binding.tvPickupInfo.text = "Alınış noktası: Konum alınamadı"
                    appendLog("Güncel konum alınamadı")
                }
            }.addOnFailureListener {
                binding.tvPickupInfo.text = "Alınış noktası: Konum hatası"
                appendLog("Konum alınırken hata oluştu: ${it.message}")
            }
        } catch (e: SecurityException) {
            appendLog("Konum izni yok: ${e.message}")
        }
    }

    private fun updateCustomerMarker(latLng: LatLng) {
        val map = googleMap ?: return

        if (customerMarker == null) {
            customerMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Benim Konumum")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        } else {
            customerMarker?.position = latLng
        }
    }

    private fun updatePickupMarker(latLng: LatLng) {
        val map = googleMap ?: return

        if (pickupMarker == null) {
            pickupMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Alınış Noktası")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        } else {
            pickupMarker?.position = latLng
        }
    }

    private fun updateDropoffMarker(latLng: LatLng) {
        val map = googleMap ?: return

        if (dropoffMarker == null) {
            dropoffMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Varış Noktası")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        } else {
            dropoffMarker?.position = latLng
        }
    }

    private fun updateDriverMarker(lat: Double, lng: Double) {
        val map = googleMap ?: return
        val driverPosition = LatLng(lat, lng)
        driverLatLng = driverPosition

        if (driverMarker == null) {
            driverMarker = map.addMarker(
                MarkerOptions()
                    .position(driverPosition)
                    .title("Taksiniz")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
        } else {
            driverMarker?.position = driverPosition
        }

        updateCameraForAllPoints()
    }

    private fun updateCameraForAllPoints() {
        val map = googleMap ?: return

        val points = mutableListOf<LatLng>()
        customerLatLng?.let { points.add(it) }
        pickupLatLng?.let { points.add(it) }
        dropoffLatLng?.let { points.add(it) }
        driverLatLng?.let { points.add(it) }

        if (points.isEmpty()) return

        if (points.size == 1) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 16f))
            return
        }

        val boundsBuilder = LatLngBounds.Builder()
        points.forEach { boundsBuilder.include(it) }

        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 160)
        )
    }

    private fun appendLog(text: String) {
        val current = binding.tvSocketLog.text.toString()
        binding.tvSocketLog.text = "$current\n$text"
    }
}