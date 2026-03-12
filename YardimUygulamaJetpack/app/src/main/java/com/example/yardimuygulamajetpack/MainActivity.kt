package com.example.yardimuygulamajetpack

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
import com.example.yardimuygulamajetpack.entity.Session
import com.example.yardimuygulamajetpack.navigation.AppNav
import com.example.yardimuygulamajetpack.navigation.Route
import com.example.yardimuygulamajetpack.ui.theme.YardimUygulamaJetpackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val start = if (Session.isLoggedIn(this)) {
            if (Session.role(this) == "YARDIMCI") Route.HelperOpen.r else Route.Patient.r
        } else Route.Login.r

        setContent { AppNav(start) }
    }
}
