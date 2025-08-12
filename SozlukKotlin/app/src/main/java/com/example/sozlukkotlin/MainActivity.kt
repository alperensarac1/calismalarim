package com.example.sozlukkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.example.sozlukkotlin.util.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.navHostFragment)
        val session = SessionManager(this)

        val currentDestination = navController.currentDestination?.id

        // Uygulama ilk açıldığında oturum kontrolü
        if (savedInstanceState == null) {
            if (session.isLoggedIn()) {
                navController.navigate(R.id.anaSayfaFragment)
            } else {
                navController.navigate(R.id.girisFragment)
            }
        }
    }
}
