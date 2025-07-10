package com.example.chatkotlin.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatkotlin.R
import com.example.chatkotlin.adapter.KonusulanKisilerAdapter
import com.example.chatkotlin.databinding.FragmentMesajlarBinding
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.service.response.KullaniciListResponse
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.util.PrefManager
import com.example.chatkotlin.viewmodel.SohbetListesiViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MesajlarFragment : Fragment() {

    private lateinit var binding: FragmentMesajlarBinding
    private lateinit var adapter: KonusulanKisilerAdapter
    private lateinit var viewModel: SohbetListesiViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMesajlarBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[SohbetListesiViewModel::class.java]

        // Boş adapterle başla
        adapter = KonusulanKisilerAdapter(emptyList()) { secilenKisi ->
            // Tıklanınca yapılacak işlem (örneğin Chat ekranına geçiş)
            Toast.makeText(requireContext(), "${secilenKisi.ad} seçildi", Toast.LENGTH_SHORT).show()
            kisiyeGoreKonusmayaGit(secilenKisi.numara)
        }

        val kullaniciId = AppConfig.kullaniciId


        binding.rvKonusulanKisiler.adapter = adapter
        binding.rvKonusulanKisiler.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()
        viewModel.sohbetListesiniBaslat(kullaniciId = kullaniciId)


        binding.fabMesajOlustur.setOnClickListener {
            showYeniKisiDialog()
        }

        return binding.root
    }

    private fun observeViewModel() {
        viewModel.konusulanKisiler.observe(viewLifecycleOwner) { kisiler ->
            adapter = KonusulanKisilerAdapter(kisiler) { secilenKisi ->
                Toast.makeText(requireContext(), "${secilenKisi.ad} seçildi", Toast.LENGTH_SHORT).show()
                // Örneğin: findNavController().navigate(...)
                kisiyeGoreKonusmayaGit(secilenKisi.numara)
            }
            binding.rvKonusulanKisiler.adapter = adapter
        }

        viewModel.hataMesaji.observe(viewLifecycleOwner) { hata ->
            hata?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showYeniKisiDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Alıcı numarası"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setPadding(24, 24, 24, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Yeni Mesaj")
            .setMessage("Mesaj göndermek istediğiniz numarayı girin:")
            .setView(editText)
            .setPositiveButton("Gönder") { _, _ ->
                val numara = editText.text.toString().trim()
                if (numara.isNotEmpty()) {
                    kisiyeGoreKonusmayaGit(numara)
                } else {
                    Toast.makeText(requireContext(), "Numara girilmedi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
    private fun kisiyeGoreKonusmayaGit(numara: String) {
        RetrofitClient.apiService.kullanicilariGetir().enqueue(object : Callback<KullaniciListResponse> {
            override fun onResponse(call: Call<KullaniciListResponse>, response: Response<KullaniciListResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val kisi = response.body()?.kullanicilar?.find { it.numara == numara }
                    if (kisi != null) {
                        // Navigation ile single chat ekranına geç
                        val bundle = Bundle().apply {
                            putInt("alici_id", kisi.id)
                            putString("alici_ad", kisi.ad)
                        }
                        findNavController().navigate(R.id.action_mesajlarFragment_to_singleChatFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Bu numara kayıtlı değil", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<KullaniciListResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Sunucu hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
