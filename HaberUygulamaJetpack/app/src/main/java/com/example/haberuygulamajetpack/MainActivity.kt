package com.example.haberuygulamajetpack

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
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.navigation.NavGraph
import com.example.haberuygulamajetpack.servis.ApiClient
import com.example.haberuygulamajetpack.ui.theme.HaberUygulamaJetpackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val haberDao = HaberDao(ApiClient.retrofit)
        setContent {
            HaberUygulamaJetpackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        NavGraph(haberDao = haberDao)
                    }

                }
            }
        }
    }
}

