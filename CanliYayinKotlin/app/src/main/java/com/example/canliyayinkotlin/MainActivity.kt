package com.example.canliyayinkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import com.example.canliyayinkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartBroadcast.setOnClickListener {
            startActivity(Intent(this, BroadcasterActivity::class.java))
        }

        binding.btnWatchBroadcasts.setOnClickListener {
            startActivity(Intent(this, RoomListActivity::class.java))
        }
    }
}