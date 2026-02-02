package com.example.kargopaylasimkotlin.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kargopaylasimkotlin.R
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.kargopaylasimkotlin.di.AppContainer
import com.example.kargopaylasimkotlin.factory.AuthVmFactory
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.viewmodel.AuthViewModel


class LoginActivity : ComponentActivity() {

    private lateinit var vm: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val container = AppContainer(applicationContext)

        if (container.tokenStore.isLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        vm = ViewModelProvider(this, AuthVmFactory(container.repo, container.tokenStore))
            .get(AuthViewModel::class.java)

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnLogin)
        val prog = findViewById<ProgressBar>(R.id.progress)
        val tvReg = findViewById<TextView>(R.id.tvRegister)

        btn.setOnClickListener {
            vm.login(etPhone.text.toString().trim(), etPass.text.toString())
        }

        tvReg.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        vm.loginState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is UiState.Error -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, st.message, Toast.LENGTH_LONG).show()
                }
                else -> prog.visibility = View.GONE
            }
        }
    }
}
