package com.example.canliyayinjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.canliyayinjetpack.ui.theme.CanliYayinJetpackTheme


import androidx.activity.compose.setContent
import androidx.compose.runtime.*

sealed class AppScreen {
    data object Home : AppScreen()
    data object RoomList : AppScreen()
    data object Broadcaster : AppScreen()

    data class Viewer(
        val roomId: String,
        val roomTitle: String
    ) : AppScreen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember {
                mutableStateOf<AppScreen>(AppScreen.Home)
            }

            when (val screen = currentScreen) {

                AppScreen.Home -> {
                    HomeScreen(
                        onStartBroadcastClick = {
                            currentScreen = AppScreen.Broadcaster
                        },
                        onWatchBroadcastsClick = {
                            currentScreen = AppScreen.RoomList
                        }
                    )
                }

                AppScreen.RoomList -> {
                    RoomListScreen(
                        onBackClick = {
                            currentScreen = AppScreen.Home
                        },
                        onRoomClick = { room ->
                            currentScreen = AppScreen.Viewer(
                                roomId = room.roomId,
                                roomTitle = room.title
                            )
                        }
                    )
                }

                AppScreen.Broadcaster -> {
                    BroadcasterScreen(
                        onBackClick = {
                            currentScreen = AppScreen.Home
                        }
                    )
                }

                is AppScreen.Viewer -> {
                    ViewerScreen(
                        roomId = screen.roomId,
                        roomTitle = screen.roomTitle,
                        onBackClick = {
                            currentScreen = AppScreen.RoomList
                        }
                    )
                }
            }
        }
    }
}