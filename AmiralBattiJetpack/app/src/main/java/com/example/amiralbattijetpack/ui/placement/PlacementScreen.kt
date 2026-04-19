package com.example.amiralbattijetpack.ui.placement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.amiralbattijetpack.model.ShipOrientation

@Composable
fun PlacementScreen(
    roomCode: String,
    playerId: String,
    playerName: String,
    viewModel: PlacementViewModel = viewModel(),
    onNavigateToGame: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomCode, playerId, playerName) {
        viewModel.initialize(roomCode, playerId, playerName)
    }

    if (state.navigateToGame) {
        LaunchedEffect(state.navigateToGame) {
            onNavigateToGame(
                state.roomCode,
                state.playerId,
                state.playerName,
                state.firstTurnPlayerId,
                state.ownBoardJson
            )
            viewModel.consumeGameNavigation()
        }
    }

    PlacementScreenContent(
        state = state,
        onRotateClick = viewModel::rotateShip,
        onResetClick = viewModel::resetBoard,
        onReadyClick = viewModel::sendBoardToServer,
        onCellClick = viewModel::onCellClick
    )
}

@Composable
private fun PlacementScreenContent(
    state: PlacementUiState,
    onRotateClick: () -> Unit,
    onResetClick: () -> Unit,
    onReadyClick: () -> Unit,
    onCellClick: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Gemi Yerleştirme",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Oda: ${state.roomCode}")
                Text(text = "Oyuncu: ${state.playerName}", modifier = Modifier.padding(top = 6.dp))
                Text(text = state.currentShipSizeText, modifier = Modifier.padding(top = 6.dp))
                Text(
                    text = if (state.orientation == ShipOrientation.HORIZONTAL) {
                        "Yön: Yatay"
                    } else {
                        "Yön: Dikey"
                    },
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Button(
            onClick = onRotateClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Yönü Değiştir")
        }

        Button(
            onClick = onResetClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Tahtayı Sıfırla")
        }

        BoardGrid(
            cells = state.boardCells,
            onCellClick = onCellClick
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.statusText,
                modifier = Modifier.padding(16.dp)
            )
        }

        Button(
            onClick = onReadyClick,
            enabled = state.readyEnabled,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            Text("Hazırım")
        }
    }
}

@Composable
private fun BoardGrid(
    cells: List<BoardCell>,
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
            BoardCellItem(
                cell = cell,
                onClick = { onCellClick(cell.row, cell.col) }
            )
        }
    }
}

@Composable
private fun BoardCellItem(
    cell: BoardCell,
    onClick: () -> Unit
) {
    val color = when (cell.state) {
        CellState.EMPTY -> Color(0xFFD9EAF7)
        CellState.SHIP -> Color(0xFF5B7C99)
        CellState.HIT -> Color.Red
        CellState.MISS -> Color.White
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(color)
            .clickable { onClick() }
    )
}
