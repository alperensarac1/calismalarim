package com.example.chatkotlin.view

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatkotlin.R
import com.example.chatkotlin.adapter.MesajlarRVAdapter
import com.example.chatkotlin.databinding.FragmentSingleChatBinding
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.viewmodel.MesajlarViewModel


class SingleChatFragment : Fragment() {

    lateinit var binding:FragmentSingleChatBinding
    private lateinit var viewModel: MesajlarViewModel
    private lateinit var adapter: MesajlarRVAdapter
    private var aliciId: Int = -1
    private var aliciAd: String = ""



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentSingleChatBinding.inflate(inflater,container,false)

        viewModel = ViewModelProvider(this)[MesajlarViewModel::class.java]


        arguments?.let {
            aliciId = it.getInt("alici_id")
            aliciAd = it.getString("alici_ad") ?: ""
        }

        requireActivity().title = aliciAd

        adapter = MesajlarRVAdapter(emptyList()) {}
        binding.rvMesajlar.adapter = adapter
        binding.rvMesajlar.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()
        viewModel.mesajlariYuklePeriyodik(AppConfig.kullaniciId, aliciId)

        binding.btnResimSec.setOnClickListener {
            resimSecLauncher.launch("image/*")
        }

        binding.btnGonder.setOnClickListener {
            val mesajText = binding.etMesaj.text.toString().trim()

            if (mesajText.isEmpty() && secilenResimBase64 == null) {
                Toast.makeText(requireContext(), "Boş mesaj gönderilemez", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.mesajGonder(
                AppConfig.kullaniciId,
                aliciId,
                mesajText,
                secilenResimBase64
            )

            binding.etMesaj.text.clear()
            binding.ivOnizleme.setImageDrawable(null)
            binding.ivOnizleme.visibility = View.GONE
            secilenResimBase64 = null
        }


        return binding.root
    }

    private var secilenResimBase64: String? = null
    private val resimSecLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.ivOnizleme.setImageURI(uri)
            binding.ivOnizleme.visibility = View.VISIBLE
            secilenResimBase64 = uriToBase64(uri)
        }
    }
    private fun observeViewModel() {
        viewModel.mesajlar.observe(viewLifecycleOwner) { liste ->
            adapter = MesajlarRVAdapter(liste) {}
            binding.rvMesajlar.adapter = adapter
            binding.rvMesajlar.scrollToPosition(liste.size - 1)
        }

        viewModel.hataMesaji.observe(viewLifecycleOwner) {
            it?.let { hata ->
                Toast.makeText(requireContext(), hata, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}