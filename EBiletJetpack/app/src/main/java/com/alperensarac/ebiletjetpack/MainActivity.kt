package com.alperensarac.ebiletjetpack

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
import com.alperensarac.ebiletjetpack.ui.theme.EBiletJetpackTheme
import com.alperensarac.ebiletjetpack.navigation.AppNavGraph
import com.alperensarac.ebiletjetpack.ui.theme.EventTicketComposeTheme

/*
    MainActivity

    Compose uygulamada genelde tek Activity vardır.
    Tüm ekran geçişlerini Navigation Compose ile yaparız.

    setContent {
        Compose UI burada başlar.
    }
*/
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EventTicketComposeTheme {
                AppNavGraph()
            }
        }
    }
}