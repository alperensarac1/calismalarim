package com.example.onlinetaksi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.onlinetaksi.data.local.SessionManager
import com.example.onlinetaksi.data.remote.api.ApiClient
import com.example.onlinetaksi.data.repository.AuthRepository
import com.example.onlinetaksi.databinding.ActivityLoginBinding
import com.example.onlinetaksi.ui.home.CustomerHomeActivity
import com.example.onlinetaksi.util.Resource

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            repository = AuthRepository(ApiClient.create(this)),
            sessionManager = SessionManager(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (phone.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Telefon ve şifre zorunlu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(phone, password)
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeData()
    }

    private fun observeData() {
        viewModel.loginState.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Giriş yapılıyor..."
                }
                is Resource.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Giriş Yap"

                    Toast.makeText(this, result.data, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, CustomerHomeActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Giriş Yap"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}