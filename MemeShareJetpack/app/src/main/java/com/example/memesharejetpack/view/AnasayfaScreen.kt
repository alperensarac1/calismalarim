package com.example.memesharejetpack.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memesharejetpack.viewmodel.OdaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnasayfaScreen(
    userId: Int,
    onOpenRoom: (roomId: Int, userId: Int) -> Unit,
    viewModel: OdaViewModel = viewModel()
) {
    val context = LocalContext.current

    // ViewModel verileri
    val rooms by viewModel.joinedRooms.observeAsState(emptyList())
    val createResult by viewModel.odaOlusturmaSonucu.observeAsState()
    val joinResult by viewModel.joinResult.observeAsState()

    // Dialog state
    var showJoinDialog by remember { mutableStateOf(false) }
    var roomCodeField by remember { mutableStateOf(TextFieldValue("")) }

    // İlk açılışta odaları çek
    LaunchedEffect(userId) {
        viewModel.fetchJoinedRooms(userId)
    }

    // Oda oluşturma sonucu side-effect
    LaunchedEffect(createResult) {
        createResult?.let { res ->
            if (res.success) {
                Toast.makeText(context, "Oda oluşturuldu: ${res.roomCode}", Toast.LENGTH_SHORT).show()
                // Listeyi yenile
                viewModel.fetchJoinedRooms(userId)
            } else if (res.message.isNotBlank()) {
                Toast.makeText(context, "Hata: ${res.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Odaya katılma sonucu side-effect
    LaunchedEffect(joinResult) {
        joinResult?.let { res ->
            if (res.success) {
                Toast.makeText(context, "Odaya katıldınız", Toast.LENGTH_SHORT).show()
                viewModel.fetchJoinedRooms(userId)
            } else if (res.message.isNotBlank()) {
                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Odalarım") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.createRoom(userId) },
                content = { Text("Oda Oluştur") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = { showJoinDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Odaya Katıl (Kod ile)")
            }

            Spacer(Modifier.height(12.dp))

            if (rooms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("Henüz oda yok") }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(rooms) { oda ->
                        RoomCard(
                            roomCode = oda.roomCode,
                            createdBy = oda.createdBy,
                            onClick = { onOpenRoom(oda.odaId, userId) }
                        )
                    }
                }
            }
        }
    }

    // Join dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Oda Katılım") },
            text = {
                OutlinedTextField(
                    value = roomCodeField,
                    onValueChange = { roomCodeField = it },
                    singleLine = true,
                    label = { Text("Oda Kodu") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val code = roomCodeField.text.trim()
                    if (code.isNotEmpty()) {
                        viewModel.joinRoom(userId, code)
                        showJoinDialog = false
                        roomCodeField = TextFieldValue("")
                    }
                }) { Text("Katıl") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun RoomCard(
    roomCode: String,
    createdBy: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = roomCode, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(text = "Oluşturan: $createdBy", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
