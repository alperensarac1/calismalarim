package com.example.isletimsistemicalismasijetpackcompose.uygulamalar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.compose.rememberNavController
import com.example.isletimsistemicalismasijetpackcompose.R
import com.example.isletimsistemicalismasijetpackcompose.anaekran.UygulamaEkraniScreen
import com.example.isletimsistemicalismasijetpackcompose.ui.theme.IsletimsistemicalismasiJetpackComposeTheme

class UygulamaEkraniActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val navController = rememberNavController()
                        UygulamalarGraph()
                    }
                }
        }
    }
}