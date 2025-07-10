package com.example.chatkotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.service.response.SimpleResponse
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.util.PrefManager
import com.example.chatkotlin.view.dialog.RegistrationDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        pref = PrefManager(this)

        if (!pref.kullaniciVarMi()) {
            RegistrationDialog().show(supportFragmentManager, "register")
        } else {
            AppConfig.kullaniciId = pref.getirKullaniciId()
            Toast.makeText(this, "ID yüklendi: ${AppConfig.kullaniciId}", Toast.LENGTH_SHORT).show()
        }

        RetrofitClient.apiService.testConnection().enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(call: Call<SimpleResponse>, response: Response<SimpleResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Bağlantı başarılı: ${response.body()?.message}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Sunucu bağlantısı başarısız", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Bağlantı hatası: ${t.message}", Toast.LENGTH_LONG).show()
                println(t.message)
            }
        })


    }
}