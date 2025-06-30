package com.example.isletimsistemicalismasijetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.isletimsistemicalismasijetpackcompose.anaekran.UygulamaEkraniScreen
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.GirisEkraniGraph
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.GirisYapScreen
import com.example.isletimsistemicalismasijetpackcompose.ui.theme.IsletimsistemicalismasiJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IsletimsistemicalismasiJetpackComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        val navController = rememberNavController()
                        GirisEkraniGraph()
                    }
                }
            }
        }
    }
}

