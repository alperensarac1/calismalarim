package com.example.onlineradiojetpack

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.onlineradiojetpack.ui.RadioPlayerScreen
import com.example.onlineradiojetpack.ui.RoomListScreen

@Composable
fun OnlineRadioApp() {
    val navController = rememberNavController()

    NavHost(
    navController = navController,
    startDestination = "rooms"
    ) {
        composable("rooms") {
            RoomListScreen(
                onRoomClick = { room ->
                    navController.navigate(
                        "player/${room.id}/${room.roomName}"
                    )
                }
            )
        }

        composable("player/{roomId}/{roomName}") { backStackEntry ->
            val roomId = backStackEntry.arguments
                ?.getString("roomId")
                ?.toIntOrNull() ?: -1

            val roomName = backStackEntry.arguments
                ?.getString("roomName") ?: "Oda"

            RadioPlayerScreen(
                roomId = roomId,
                roomName = roomName
            )
        }
    }
}