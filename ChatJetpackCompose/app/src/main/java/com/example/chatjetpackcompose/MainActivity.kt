package com.example.chatjetpackcompose

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
import com.example.chatjetpackcompose.ui.theme.ChatJetpackComposeTheme
import com.example.chatjetpackcompose.view.ChatAppContent
import com.example.chatjetpackcompose.viewmodel.MesajlarViewModel
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.util.PrefManager

class MainActivity : ComponentActivity() {

    private lateinit var pref: PrefManager
    private lateinit var mesajlarVM:MesajlarViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mesajlarVM = MesajlarViewModel()
        pref = PrefManager(this)

        // Kullanıcı ID'yi yükle
        if (pref.kullaniciVarMi()) {
            AppConfig.kullaniciId = pref.getirKullaniciId()
        }

        setContent {
            ChatAppContent(pref = pref, mesajlarVM = mesajlarVM)
        }
    }
}


