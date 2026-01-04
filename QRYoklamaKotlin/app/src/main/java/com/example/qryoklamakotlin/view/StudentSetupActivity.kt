package com.example.qryoklamakotlin.view


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.qryoklamakotlin.data.Prefs
import com.example.qryoklamakotlin.databinding.ActivityStudentSetupBinding

class StudentSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentSetupBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentSetupBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        prefs = Prefs(this)

        val savedNo = prefs.getStudentNo()
        if (!savedNo.isNullOrEmpty()) {
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
            return
        }

        binding.btnSave.setOnClickListener {
            val no = binding.etNo.text.toString().trim()
            if (no.isEmpty()) {
                binding.etNo.error = "Öğrenci numarası gerekli"
                return@setOnClickListener
            }

            prefs.setStudentNo(no)
            startActivity(Intent(this, ScanActivity::class.java))
            finish()
        }
    }
}
