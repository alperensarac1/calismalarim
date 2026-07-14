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
import com.alperensarac.ebiletkotlin.databinding.ActivityRegisterBinding
import com.alperensarac.ebiletkotlin.ui.home.HomeActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvGoLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPhone.error = null
        binding.tilPassword.error = null

        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Ad soyad zorunludur"
            return
        }

        if (fullName.length < 3) {
            binding.tilFullName.error = "Ad soyad en az 3 karakter olmalıdır"
            return
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "E-posta zorunludur"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Geçerli bir e-posta giriniz"
            return
        }

        if (phone.isNotEmpty() && phone.length < 10) {
            binding.tilPhone.error = "Telefon numarası eksik görünüyor"
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

        ApiClient.apiService.register(
            fullName = fullName,
            email = email,
            phone = phone,
            password = password
        ).enqueue(object : Callback<ApiResponse<User>> {

            override fun onResponse(
                call: Call<ApiResponse<User>>,
                response: Response<ApiResponse<User>>
            ) {
                setLoading(false)

                if (!response.isSuccessful) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Sunucu hatası: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val apiResponse = response.body()

                if (apiResponse == null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Boş sunucu cevabı",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                if (!apiResponse.success) {
                    Toast.makeText(
                        this@RegisterActivity,
                        apiResponse.message,
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val user = apiResponse.data

                if (user == null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Kullanıcı bilgisi alınamadı",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                sessionManager.saveUser(user)

                Toast.makeText(
                    this@RegisterActivity,
                    "Kayıt başarılı. Hoş geldin ${user.fullName}",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

            override fun onFailure(call: Call<ApiResponse<User>>, t: Throwable) {
                setLoading(false)

                Toast.makeText(
                    this@RegisterActivity,
                    "Bağlantı hatası: ${t.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnRegister.isEnabled = !isLoading
        binding.tvGoLogin.isEnabled = !isLoading

        binding.tvLoading.visibility = if (isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}