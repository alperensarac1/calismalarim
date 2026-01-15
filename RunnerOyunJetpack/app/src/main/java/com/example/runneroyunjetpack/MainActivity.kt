package com.example.runneroyunjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.runneroyunjetpack.ui.theme.RunnerOyunJetpackTheme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.runneroyunjetpack.game_engine.GameView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GameScreen(modifier = Modifier.fillMaxSize()) }
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val gameView = remember {
        GameView(context, null)
    }

    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> gameView.resume()
                Lifecycle.Event.ON_PAUSE -> gameView.pause()
                Lifecycle.Event.ON_STOP -> gameView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            gameView.pause()
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { gameView }
        )
    }
}
