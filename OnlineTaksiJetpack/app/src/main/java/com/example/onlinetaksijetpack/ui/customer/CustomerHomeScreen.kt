package com.example.onlinetaksijetpack.ui.customer

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.remote.api.ApiClient
import com.example.onlinetaksijetpack.data.repository.RideRepository

@Composable
fun CustomerHomeScreen() {
    val context = LocalContext.current

    val viewModel: CustomerHomeViewModel = viewModel(
        factory = CustomerHomeViewModelFactory(
            rideRepository = RideRepository(ApiClient.create(context)),
            sessionManager = SessionManager(context)
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val activeRide by viewModel.activeRide.collectAsState()

    val pickupLat = remember { mutableStateOf("") }
    val pickupLng = remember { mutableStateOf("") }
    val pickupAddress = remember { mutableStateOf("") }

    val dropoffLat = remember { mutableStateOf("") }
    val dropoffLng = remember { mutableStateOf("") }
    val dropoffAddress = remember { mutableStateOf("") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
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
            text = "Customer Home",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = if (uiState.socketConnected) "Socket: Bağlı" else "Socket: Bağlı değil")
        Text(text = "Ride Durumu: ${uiState.rideStatus}")
        Text(text = "Taksi Enlem: ${uiState.driverLatText}")
        Text(text = "Taksi Boylam: ${uiState.driverLngText}")
        Text(text = "Son Konum Güncelleme: ${uiState.lastLocationUpdateText}")
        Text(text = "Son Event: ${uiState.lastSocketEvent}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.connectSocket() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Socket Bağlan")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { viewModel.sendPing() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ping Gönder")
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Harita",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomerMapSection(
            pickupLat = uiState.pickupLat,
            pickupLng = uiState.pickupLng,
            dropoffLat = uiState.dropoffLat,
            dropoffLng = uiState.dropoffLng,
            driverLat = uiState.driverLat,
            driverLng = uiState.driverLng
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pickup / Dropoff Bilgileri",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pickupLat.value,
            onValueChange = { pickupLat.value = it },
            label = { Text("Pickup Enlem") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pickupLng.value,
            onValueChange = { pickupLng.value = it },
            label = { Text("Pickup Boylam") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pickupAddress.value,
            onValueChange = { pickupAddress.value = it },
            label = { Text("Pickup Adres") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dropoffLat.value,
            onValueChange = { dropoffLat.value = it },
            label = { Text("Dropoff Enlem") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dropoffLng.value,
            onValueChange = { dropoffLng.value = it },
            label = { Text("Dropoff Boylam") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dropoffAddress.value,
            onValueChange = { dropoffAddress.value = it },
            label = { Text("Dropoff Adres") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val pLat = pickupLat.value.toDoubleOrNull()
                val pLng = pickupLng.value.toDoubleOrNull()
                val dLat = dropoffLat.value.toDoubleOrNull()
                val dLng = dropoffLng.value.toDoubleOrNull()

                if (pLat == null || pLng == null || dLat == null || dLng == null ||
                    pickupAddress.value.isBlank() || dropoffAddress.value.isBlank()
                ) {
                    Toast.makeText(context, "Tüm alanları doğru doldur", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.createRide(
                        pickupLat = pLat,
                        pickupLng = pLng,
                        pickupAddress = pickupAddress.value.trim(),
                        dropoffLat = dLat,
                        dropoffLng = dLng,
                        dropoffAddress = dropoffAddress.value.trim()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isCreatingRide
        ) {
            if (uiState.isCreatingRide) {
                CircularProgressIndicator()
            } else {
                Text("Taksi Çağır")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aktif Ride",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (activeRide == null) {
            Text("Aktif ride yok")
        } else {
            Text("Ride ID: ${activeRide!!.id}")
            Text("Pickup: ${activeRide!!.pickup_address}")
            Text("Dropoff: ${activeRide!!.dropoff_address}")
            Text("Durum: ${activeRide!!.status}")
            Text("Tahmini Ücret: ${activeRide!!.estimated_fare ?: "-"}")
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Çıkış Yap")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}