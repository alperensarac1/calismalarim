package com.example.amiralbattijetpack.ui.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LobbyScreen(
    uiState: StateFlow<LobbyUiState>,
    onPlayerNameChange: (String) -> Unit,
    onRoomCodeChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onJoinRoomClick: () -> Unit,
    onNavigateToPlacement: (String, String, String) -> Unit,
    onPlacementNavigationConsumed: () -> Unit
) {
    val state by uiState.collectAsStateWithLifecycle()

    LobbyScreenContent(
        state = state,
        onPlayerNameChange = onPlayerNameChange,
        onRoomCodeChange = onRoomCodeChange,
        onConnectClick = onConnectClick,
        onCreateRoomClick = onCreateRoomClick,
        onJoinRoomClick = onJoinRoomClick
    )

    if (state.shouldNavigateToPlacement) {
        LaunchedEffect(state.shouldNavigateToPlacement) {
            onNavigateToPlacement(
                state.currentRoomCode,
                state.currentPlayerId,
                state.playerName
            )
            onPlacementNavigationConsumed()
        }
    }
}

@Composable
private fun LobbyScreenContent(
    state: LobbyUiState,
    onPlayerNameChange: (String) -> Unit,
    onRoomCodeChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onJoinRoomClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Amiral Battı",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = state.playerName,
            onValueChange = onPlayerNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Oyuncu adı") },
            singleLine = true
        )

        OutlinedTextField(
            value = state.roomCode,
            onValueChange = onRoomCodeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Oda kodu") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = onConnectClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Sunucuya Bağlan")
        }

        Button(
            onClick = onCreateRoomClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Oda Oluştur")
        }

        Button(
            onClick = onJoinRoomClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Odaya Katıl")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = state.roomInfo)
                Text(text = state.playersText, modifier = Modifier.padding(top = 8.dp))
                Text(text = state.statusText, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
