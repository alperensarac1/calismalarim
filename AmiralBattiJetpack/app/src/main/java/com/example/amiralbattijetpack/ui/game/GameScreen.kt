package com.example.amiralbattijetpack.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.amiralbattijetpack.model.BoardCell
import com.example.amiralbattijetpack.model.CellState

@Composable
fun GameScreen(
    roomCode: String,
    playerId: String,
    playerName: String,
    firstTurnPlayerId: String,
    ownBoardJson: String,
    viewModel: GameViewModel = viewModel(),
    onNavigateToPlacement: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToLobby: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomCode, playerId, playerName, firstTurnPlayerId, ownBoardJson) {
        viewModel.initialize(
            roomCode = roomCode,
            playerId = playerId,
            playerName = playerName,
            firstTurnPlayerId = firstTurnPlayerId,
            ownBoardJson = ownBoardJson
        )
    }

    if (state.navigateToPlacement) {
        LaunchedEffect(state.navigateToPlacement) {
            onNavigateToPlacement(
                state.roomCode,
                state.playerId,
                state.playerName
            )
            viewModel.consumePlacementNavigation()
        }
    }

    GameScreenContent(
        state = state,
        onEnemyCellClick = viewModel::onEnemyCellClick
    )

    if (state.showGameOverDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(if (state.gameOverWinner) "Tebrikler" else "Oyun Bitti")
            },
            text = {
                Text(
                    if (state.gameOverWinner) {
                        "Rakibin tüm gemilerini batırdın.\n\nYeniden oynamak ister misin?"
                    } else {
                        "Tüm gemilerin batırıldı.\n\nYeniden oynamak ister misin?"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.requestRematch() }) {
                    Text("Yeniden Oyna")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.dismissGameOverDialog()
                    onNavigateToLobby()
                }) {
                    Text("Lobiye Dön")
                }
            }
        )
    }

    if (state.showPlayerLeftDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Rakip Ayrıldı") },
            text = { Text("Rakip oyundan çıktı. Lobiye dönmek ister misin?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissPlayerLeftDialog()
                    onNavigateToLobby()
                }) {
                    Text("Lobiye Dön")
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
private fun GameScreenContent(
    state: GameUiState,
    onEnemyCellClick: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Oyun Ekranı",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = state.turnText)
                Text(text = state.statusText, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Text(
            text = "Kendi Tahtan",
            style = MaterialTheme.typography.titleMedium
        )

        BoardGrid(
            cells = state.ownBoardCells,
            clickable = false,
            onCellClick = { _, _ -> }
        )

        Text(
            text = "Rakip Tahtası",
            style = MaterialTheme.typography.titleMedium
        )

        BoardGrid(
            cells = state.enemyBoardCells,
            clickable = true,
            onCellClick = onEnemyCellClick
        )
    }
}

@Composable
private fun BoardGrid(
    cells: List<BoardCell>,
    clickable: Boolean,
    onCellClick: (Int, Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(cells) { cell ->
            GameBoardCellItem(
                cell = cell,
                clickable = clickable,
                onClick = { onCellClick(cell.row, cell.col) }
            )
        }
    }
}

@Composable
private fun GameBoardCellItem(
    cell: BoardCell,
    clickable: Boolean,
    onClick: () -> Unit
) {
    val color = when (cell.state) {
        CellState.EMPTY -> Color(0xFFD9EAF7)
        CellState.SHIP -> Color(0xFF5B7C99)
        CellState.HIT -> Color.Red
        CellState.MISS -> Color.White
    }

    val modifier = if (clickable) {
        Modifier
            .aspectRatio(1f)
            .background(color)
            .clickable { onClick() }
    } else {
        Modifier
            .aspectRatio(1f)
            .background(color)
    }

    Box(modifier = modifier)
}
