package com.example.qrkodolusturucu

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.qrkodolusturucu.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucu.databinding.ActivityMainBinding
import com.example.qrkodolusturucu.services.retrofitservice.KahveHTTPServisDao
import com.example.qrkodolusturucu.viewmodel.MainVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        binding.btnQRKod.setOnClickListener {
            binding.btnQRKod.setBackgroundColor(resources.getColor(R.color.btnSeciliBackground, theme))
            binding.btnUrunlerimiz.setBackgroundColor(Color.TRANSPARENT)
            toQRKodFragment()
        }

        binding.btnUrunlerimiz.setOnClickListener {
            binding.btnUrunlerimiz.setBackgroundColor(resources.getColor(R.color.btnSeciliBackground, theme))
            binding.btnQRKod.setBackgroundColor(Color.TRANSPARENT)
            toUrunlerFragment()
        }

        observeViewModel()
        if (!viewModel.checkTelefonNumarasi()){
            showInputDialog()
        }


        viewModel.telefonNumarasiLiveData.observe(this) { numara ->
            Log.e("MainVMActivity", "Gelen Telefon Numarası: $numara")  // Burada numarayı logluyoruz
            if (numara == null) {
                showInputDialog()
            } else {
                viewModel.kullaniciEkle(numara)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.hediyeKahveLiveData.observe(this) { hediyeKahve ->
            binding.tvHediyeKahve.text = "Hediye Kahve: $hediyeKahve"
        }

        viewModel.kahveSayisiLiveData.observe(this) { kahveSayisi ->
            binding.tvKullanimlar.text = "Kullanımlar: $kahveSayisi/5"
        }
    }

    private fun showInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Bilgi Girişi")

        val input = EditText(this)
        input.setHint("Lütfen telefon numaranızı giriniz")
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("Tamam") { dialog, _ ->
            val userInput = input.text.toString().trim()
            if (userInput.isNotEmpty()) {
                viewModel.numarayiKaydet(userInput)
                Toast.makeText(this, "Numara kaydedildi!", Toast.LENGTH_SHORT).show()
                toQRKodFragment()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            finish()
            dialog.cancel()
        }

        builder.show()
    }

    private fun toQRKodFragment() {
        val navController = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)?.findNavController()
        navController?.navigate(R.id.QRKodOlusturFragment)
    }

    private fun toUrunlerFragment() {
        val navController = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)?.findNavController()
        navController?.navigate(R.id.urunlerimizFragment2)
    }
}
