package com.example.onlinetaksijetpack.ui.driver


import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.remote.api.ApiClient
import com.example.onlinetaksijetpack.data.repository.DriverRepository
import com.example.onlinetaksijetpack.location.DriverLocationTracker

@Composable
fun DriverHomeScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val locationTracker = remember { DriverLocationTracker(context) }

    val viewModel: DriverHomeViewModel = viewModel(
        factory = DriverHomeViewModelFactory(
            DriverRepository(ApiClient.create(context))
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                locationTracker.start { lat, lng ->
                    viewModel.sendLocation(lat, lng)
                }
            } else {
                Toast.makeText(context, "Konum izni gerekli", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationTracker.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Driver Home",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = if (uiState.isOnline) "Durum: Online" else "Durum: Offline")
        Text(text = "Konum: ${uiState.currentLat}, ${uiState.currentLng}")
        Text(text = "Log: ${uiState.lastLog}")

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.connectSocket(sessionManager.getToken()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Socket Bağlan")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.setOnline(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ONLINE OL")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.setOnline(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("OFFLINE OL")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.loadAvailableRides() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Açık Ride'ları Getir")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.loadActiveRide() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aktif Ride'ı Getir")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Konum Başlat")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { locationTracker.stop() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Konum Durdur")
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aktif Ride",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.activeRide == null) {
            Text("Aktif ride yok")
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ride ID: ${uiState.activeRide!!.id}")
                    Text("Pickup: ${uiState.activeRide!!.pickup_address}")
                    Text("Dropoff: ${uiState.activeRide!!.dropoff_address}")
                    Text("Durum: ${uiState.activeRide!!.status}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateRideStatus(
                                status = "DRIVER_ARRIVING",
                                note = "Şoför müşteriye doğru yola çıktı."
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Yoldayım")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.updateRideStatus(
                                status = "DRIVER_ARRIVED",
                                note = "Şoför alım noktasına ulaştı."
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Geldim")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.updateRideStatus(
                                status = "RIDE_STARTED",
                                note = "Müşteri araca bindi."
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Başlat")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.updateRideStatus(
                                status = "RIDE_COMPLETED",
                                note = "Yolculuk tamamlandı."
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Bitir")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Açık Ride Listesi",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoadingAvailableRides) {
            CircularProgressIndicator()
        } else {
            uiState.availableRides.forEach { ride ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ride ID: ${ride.id}")
                        Text("Pickup: ${ride.pickup_address}")
                        Text("Dropoff: ${ride.dropoff_address}")
                        Text("Tahmini Ücret: ${ride.estimated_fare ?: "-"}")

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.acceptRide(ride.id) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isAcceptingRide
                        ) {
                            Text("Kabul Et")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}