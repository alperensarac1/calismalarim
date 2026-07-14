package com.alperensarac.ebiletkotlin.ui.auth


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alperensarac.ebiletkotlin.data.api.ApiClient
import com.alperensarac.ebiletkotlin.data.model.ApiResponse
import com.alperensarac.ebiletkotlin.data.model.User
import com.alperensarac.ebiletkotlin.data.session.SessionManager
import com.alperensarac.ebiletkotlin.databinding.ActivityLoginBinding
import com.alperensarac.ebiletkotlin.ui.home.HomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        /*
            Kullanıcı daha önce giriş yaptıysa direkt ana ekrana geçer.
        */
        if (sessionManager.isLoggedIn()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()) {
            binding.tilEmail.error = "E-posta zorunludur"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Geçerli bir e-posta giriniz"
            return
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Şifre zorunludur"
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Şifre en az 6 karakter olmalıdır"
            return
        }

        setLoading(true)

        ApiClient.apiService.login(email, password)
            .enqueue(object : Callback<ApiResponse<User>> {

                override fun onResponse(
                    call: Call<ApiResponse<User>>,
                    response: Response<ApiResponse<User>>
                ) {
                    setLoading(false)

                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Sunucu hatası: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val apiResponse = response.body()

                    if (apiResponse == null) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Boş sunucu cevabı",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    if (!apiResponse.success) {
                        Toast.makeText(
                            this@LoginActivity,
                            apiResponse.message,
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val user = apiResponse.data

                    if (user == null) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Kullanıcı bilgisi alınamadı",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    sessionManager.saveUser(user)

                    Toast.makeText(
                        this@LoginActivity,
                        "Giriş başarılı. Hoş geldin ${user.fullName}",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                    setLoading(false)

                    Toast.makeText(
                        this@LoginActivity,
                        "Bağlantı hatası: ${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.tvGoRegister.isEnabled = !isLoading

        binding.tvLoading.visibility = if (isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}