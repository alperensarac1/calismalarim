package com.example.isletimsistemlericalismasikotlin.anaekran

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.ActivityUygulamaEkraniBinding

class UygulamaEkraniActivity : AppCompatActivity() {

    lateinit var binding : ActivityUygulamaEkraniBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityUygulamaEkraniBinding.inflate(LayoutInflater.from(this))
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

    }
}