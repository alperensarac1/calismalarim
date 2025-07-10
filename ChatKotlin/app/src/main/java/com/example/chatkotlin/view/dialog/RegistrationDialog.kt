package com.example.chatkotlin.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.chatkotlin.databinding.DialogRegisterBinding
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.service.response.SimpleResponse
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.util.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationDialog : DialogFragment() {

    private lateinit var binding: DialogRegisterBinding
    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogRegisterBinding.inflate(layoutInflater)
        pref = PrefManager(requireContext())

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)

        binding.btnKayitOl.setOnClickListener {
            val ad      = binding.etAd.text.toString().trim()
            val numara  = binding.etNumara.text.toString().trim()

            if (ad.isEmpty() || numara.isEmpty()) {
                Toast.makeText(requireContext(), "Boş alan bırakma!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Retrofit →  kayıt isteği
            RetrofitClient.apiService.kullaniciKayit(ad, numara)
                .enqueue(object : Callback<SimpleResponse> {
                    override fun onResponse(
                        call: Call<SimpleResponse>,
                        response: Response<SimpleResponse>
                    ) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true && body.id != null) {
                            AppConfig.kullaniciId = body.id!!
                            pref.kaydetKullaniciId(body.id!!)
                            Toast.makeText(requireContext(), "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                            dismiss()   // Diyalog kapanır, artık giriş yapılmış
                        } else {
                            Toast.makeText(requireContext(), body?.error ?: "Kayıt başarısız", Toast.LENGTH_SHORT).show()
                            println(body?.error)
                        }
                    }

                    override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                        println(t.message)
                    }
                })
        }

        return builder.create()
    }
}
