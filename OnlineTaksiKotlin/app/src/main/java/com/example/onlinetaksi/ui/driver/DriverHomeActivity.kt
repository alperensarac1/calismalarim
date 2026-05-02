package com.example.onlinetaksi.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.onlinetaksi.data.local.SessionManager
import com.example.onlinetaksi.data.remote.api.ApiClient
import com.example.onlinetaksi.data.remote.model.AvailableRideItem
import com.example.onlinetaksi.data.remote.socket.SocketManager
import com.example.onlinetaksi.data.repository.DriverRepository
import com.example.onlinetaksi.databinding.ActivityDriverHomeBinding
import com.example.onlinetaksi.location.DriverLocationTracker
import com.example.onlinetaksi.util.Resource
import kotlinx.coroutines.launch
import org.json.JSONObject

class DriverHomeActivity : AppCompatActivity(), SocketManager.SocketEventListener {

    private lateinit var binding: ActivityDriverHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var locationTracker: DriverLocationTracker
    private lateinit var rideAdapter: AvailableRideAdapter

    private val viewModel: DriverHomeViewModel by viewModels {
        DriverHomeViewModelFactory(
            DriverRepository(ApiClient.create(this))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        locationTracker = DriverLocationTracker(this)

        setupRecycler()
        observeUi()

        binding.btnConnectSocket.setOnClickListener {
            connectSocket()
        }

        binding.btnLoadAvailableRides.setOnClickListener {
            viewModel.loadAvailableRides()
        }

        binding.btnLoadActiveRide.setOnClickListener {
            viewModel.loadActiveRide()
        }

        binding.btnStartLocation.setOnClickListener {
            checkPermissionAndStart()
        }

        binding.btnStopLocation.setOnClickListener {
            locationTracker.stop()
            viewModel.setLog("Konum güncellemesi durduruldu")
        }
        binding.btnOnline.setOnClickListener {
            viewModel.setOnline()
        }

        binding.btnOffline.setOnClickListener {
            viewModel.setOffline()
        }

        binding.btnArriving.setOnClickListener {
            viewModel.uiState.value?.activeRide?.id?.let {
                viewModel.updateRideStatus(it, "DRIVER_ARRIVING")
            }
        }

        binding.btnArrived.setOnClickListener {
            viewModel.uiState.value?.activeRide?.id?.let {
                viewModel.updateRideStatus(it, "DRIVER_ARRIVED")
            }
        }

        binding.btnStartRide.setOnClickListener {
            viewModel.uiState.value?.activeRide?.id?.let {
                viewModel.updateRideStatus(it, "RIDE_STARTED")
            }
        }

        binding.btnCompleteRide.setOnClickListener {
            viewModel.uiState.value?.activeRide?.id?.let {
                viewModel.updateRideStatus(it, "RIDE_COMPLETED")
            }
        }
    }

    private fun setupRecycler() {
        rideAdapter = AvailableRideAdapter { ride ->
            viewModel.acceptRide(ride.id)
        }

        binding.rvAvailableRides.apply {
            layoutManager = LinearLayoutManager(this@DriverHomeActivity)
            adapter = rideAdapter
        }
    }

    private fun observeUi() {
        viewModel.uiState.observe(this) { state ->
            binding.tvLocation.text = "Lat: ${state.currentLat}  Lng: ${state.currentLng}"
            binding.tvLog.text = state.lastLog

            val activeRide = state.activeRide
            binding.tvActiveRideInfo.text =
                if (activeRide == null) {
                    "Aktif ride yok"
                } else {
                    "Ride ID: ${activeRide.id}\n" +
                            "Pickup: ${activeRide.pickup_address}\n" +
                            "Dropoff: ${activeRide.dropoff_address}\n" +
                            "Durum: ${activeRide.status}"
                }

            rideAdapter.submitList(state.availableRides)
        }

        viewModel.acceptRideState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    Toast.makeText(this, "Ride kabul ediliyor...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    Toast.makeText(this, "Ride kabul edildi", Toast.LENGTH_SHORT).show()
                    viewModel.setLog("Ride kabul edildi. id=${result.data.id}")
                }
                is Resource.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    viewModel.setLog("Ride kabul hatası: ${result.message}")
                }
            }
        }
    }

    private fun connectSocket() {
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Token yok", Toast.LENGTH_SHORT).show()
            return
        }

        SocketManager.setListener(this)
        SocketManager.connect(token)
    }

    private fun checkPermissionAndStart() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startLocation()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) startLocation()
            else Toast.makeText(this, "İzin gerekli", Toast.LENGTH_SHORT).show()
        }

    private fun startLocation() {
        locationTracker.start { lat, lng ->
            runOnUiThread {
                viewModel.setLocation(lat, lng)
            }

            lifecycleScope.launch {
                viewModel.sendLocation(lat, lng)
            }
        }
    }

    override fun onConnected() {
        runOnUiThread {
            viewModel.setLog("Socket bağlandı")
            Toast.makeText(this, "Socket bağlandı", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            viewModel.setLog("Socket bağlantısı kapandı")
        }
    }

    override fun onMessage(message: String) {
        runOnUiThread {
            viewModel.setLog(message)

            try {
                val json = JSONObject(message)
                val event = json.optString("event")

                if (event == "NEW_RIDE_REQUEST") {
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        val ride = AvailableRideItem(
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

                        viewModel.addIncomingRide(ride)
                        Toast.makeText(this, "Yeni ride geldi!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                viewModel.setLog("Socket parse hatası: ${e.message}")
            }
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            viewModel.setLog("Socket hata: $errorMessage")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketManager.setListener(null)
        locationTracker.stop()
    }
}