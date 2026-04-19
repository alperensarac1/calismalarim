package com.example.amiralbattijetpack.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.amiralbattijetpack.ui.game.GameScreen
import com.example.amiralbattijetpack.ui.lobby.LobbyScreen
import com.example.amiralbattijetpack.ui.lobby.LobbyViewModel
import com.example.amiralbattijetpack.ui.placement.PlacementScreen
import com.example.amiralbattijetpack.ui.placement.PlacementViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOBBY
    ) {
        composable(Routes.LOBBY) {
            val vm: LobbyViewModel = viewModel()
            LobbyScreen(
                uiState = vm.uiState,
                onPlayerNameChange = vm::updatePlayerName,
                onRoomCodeChange = vm::updateRoomCode,
                onConnectClick = vm::connectToServer,
                onCreateRoomClick = vm::createRoom,
                onJoinRoomClick = vm::joinRoom,
                onNavigateToPlacement = { roomCode, playerId, playerName ->
                    navController.navigate(
                        Routes.placementRoute(roomCode, playerId, playerName)
                    )
                },
                onPlacementNavigationConsumed = vm::consumePlacementNavigation
            )
        }

        composable(
            route = Routes.PLACEMENT,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType },
                navArgument("playerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode").orEmpty()
            val playerId = backStackEntry.arguments?.getString("playerId").orEmpty()
            val playerName = backStackEntry.arguments?.getString("playerName").orEmpty()

            PlacementScreen(
                roomCode = roomCode,
                playerId = playerId,
                playerName = playerName,
                onNavigateToGame = { navRoomCode, navPlayerId, navPlayerName, firstTurnPlayerId, ownBoardJson ->
                    navController.navigate(
                        Routes.gameRoute(
                            navRoomCode,
                            navPlayerId,
                            navPlayerName,
                            firstTurnPlayerId,
                            ownBoardJson
                        )
                    )
                }
            )
        }

        composable(
            route = Routes.GAME,
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("playerId") { type = NavType.StringType },
                navArgument("playerName") { type = NavType.StringType },
                navArgument("firstTurnPlayerId") { type = NavType.StringType },
                navArgument("ownBoardJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            GameScreen(
                roomCode = backStackEntry.arguments?.getString("roomCode").orEmpty(),
                playerId = backStackEntry.arguments?.getString("playerId").orEmpty(),
                playerName = backStackEntry.arguments?.getString("playerName").orEmpty(),
                firstTurnPlayerId = backStackEntry.arguments?.getString("firstTurnPlayerId").orEmpty(),
                ownBoardJson = backStackEntry.arguments?.getString("ownBoardJson").orEmpty(),
                onNavigateToPlacement = { navRoomCode, navPlayerId, navPlayerName ->
                    navController.navigate(
                        Routes.placementRoute(
                            navRoomCode,
                            navPlayerId,
                            navPlayerName
                        )
                    )
                },
                onNavigateToLobby = {
                    navController.popBackStack(Routes.LOBBY, inclusive = false)
                }
            )

        }
    }
}

