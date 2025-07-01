package com.example.adisyonuygulamakotlin

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout

import androidx.appcompat.app.AppCompatActivity
import com.example.adisyonuygulamakotlin.services.retrofit.AdisyonServiceInterface
import com.example.adisyonuygulamakotlin.view.anaekran.MainFragment

import com.example.adisyonuygulamakotlin.view.anaekran.MainLayout

import com.example.qrkodolusturucu.services.ServicesImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout
    private val containerId = View.generateViewId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        // Fragmentlar için container View oluştur
        container = FrameLayout(this).apply {
            id = containerId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Container'ı ekle
        setContentView(container)

        // İlk fragmenti başlat (MainLayout gösteren fragment)
        if (savedInstanceState == null) {
            val servis = ServicesImpl.getInstance()


            CoroutineScope(Dispatchers.Main).launch {
                val masalar = servis.masalariGetir()
                supportFragmentManager.beginTransaction()
                    .replace(containerId, MainFragment.newInstance(masalar))
                    .commit()
            }
        }
    }

}
