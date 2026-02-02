package com.example.kargopaylasimkotlin.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kargopaylasimkotlin.R
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.kargopaylasimkotlin.di.AppContainer
import com.example.kargopaylasimkotlin.dto.AddressCreateReq
import com.example.kargopaylasimkotlin.dto.RegisterReq
import com.example.kargopaylasimkotlin.factory.AuthVmFactory
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.viewmodel.AuthViewModel

class RegisterActivity : ComponentActivity() {

    private lateinit var vm: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val container = AppContainer(applicationContext)
        vm = ViewModelProvider(this, AuthVmFactory(container.repo, container.tokenStore))
            .get(AuthViewModel::class.java)

        val etFirst = findViewById<EditText>(R.id.etFirst)
        val etLast = findViewById<EditText>(R.id.etLast)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etTc = findViewById<EditText>(R.id.etTc)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnRegister)
        val prog = findViewById<ProgressBar>(R.id.progress)
        val tvBack = findViewById<TextView>(R.id.tvBackLogin)

        tvBack.setOnClickListener { finish() }

        btn.setOnClickListener {
            val first = etFirst.text.toString().trim()
            val last = etLast.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val tc = etTc.text.toString().trim()
            val pass = etPass.text.toString()

            val titleAddr = findViewById<EditText>(R.id.etAddressTitle).text.toString().trim()
            val city = findViewById<EditText>(R.id.etCity).text.toString().trim()
            val district = findViewById<EditText>(R.id.etDistrict).text.toString().trim()
            val neigh = findViewById<EditText>(R.id.etNeighborhood).text.toString().trim()
            val line = findViewById<EditText>(R.id.etAddressLine).text.toString().trim()
            val postal = findViewById<EditText>(R.id.etPostal).text.toString().trim()

            if (first.isBlank() || last.isBlank() || phone.isBlank() || tc.length != 11 || pass.length < 4) {
                Toast.makeText(this, "Bilgileri kontrol et (TC 11 hane, şifre min 4).", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // neighborhood opsiyonelse burada zorunlu yapma
            if (titleAddr.isBlank() || city.isBlank() || district.isBlank() || line.isBlank()) {
                Toast.makeText(this, "Adres bilgileri eksik.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val regReq = RegisterReq(
                first_name = first,
                last_name = last,
                phone = phone,
                tc_no = tc,
                password = pass,
                address_title = titleAddr,
                city = city,
                district = district,
                neighborhood = neigh.takeIf { it.isNotBlank() },
                address_line = line,
                postal_code = postal.takeIf { it.isNotBlank() }
            )

            val addrReq = AddressCreateReq(
                title = titleAddr,
                city = city,
                district = district,
                neighborhood = neigh.takeIf { it.isNotBlank() },
                address_line = line,
                postal_code = postal.takeIf { it.isNotBlank() }
            )

            vm.registerAndSetup(regReq, addrReq)
        }


        vm.registerState.observe(this) { st ->
            when (st) {
                is UiState.Loading -> prog.visibility = View.VISIBLE
                is UiState.Success -> {
                    prog.visibility = View.GONE
                    Toast.makeText(this, "Kayıt başarılı. Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show()
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
