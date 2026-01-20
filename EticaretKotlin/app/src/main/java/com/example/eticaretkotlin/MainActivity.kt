package com.example.eticaretkotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = supportFragmentManager
            .findFragmentById(R.id.mainNavHost)
            .let { it as NavHostFragment }
            .navController

        findViewById<BottomNavigationView>(R.id.bottomNav)
            .setupWithNavController(navController)
    }
}
