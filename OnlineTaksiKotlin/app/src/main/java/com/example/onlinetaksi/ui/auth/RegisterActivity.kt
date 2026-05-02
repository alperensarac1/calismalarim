package com.example.onlinetaksi.ui.auth


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.onlinetaksi.data.local.SessionManager
import com.example.onlinetaksi.data.remote.api.ApiClient
import com.example.onlinetaksi.data.repository.AuthRepository
import com.example.onlinetaksi.databinding.ActivityRegisterBinding
import com.example.onlinetaksi.ui.home.CustomerHomeActivity
import com.example.onlinetaksi.util.Resource

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            repository = AuthRepository(ApiClient.create(this)),
            sessionManager = SessionManager(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim().ifBlank { null }
            val password = binding.etPassword.text.toString().trim()

            if (fullName.isBlank() || phone.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Ad soyad, telefon ve şifre zorunlu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(
                fullName = fullName,
                phone = phone,
                email = email,
                password = password
            )
        }

        binding.tvGoLogin.setOnClickListener {
            finish()
        }

        observeData()
    }

    private fun observeData() {
        viewModel.registerState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Kayıt yapılıyor..."
                }
                is Resource.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Kayıt Ol"

                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CustomerHomeActivity::class.java))
                    finishAffinity()
                }
                is Resource.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Kayıt Ol"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}