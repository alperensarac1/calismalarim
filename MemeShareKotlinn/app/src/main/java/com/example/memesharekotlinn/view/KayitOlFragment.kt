package com.example.memesharekotlinn.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.memesharekotlinn.R
import com.example.memesharekotlinn.databinding.FragmentKayitOlBinding
import com.example.memesharekotlinn.viewmodel.RegisterViewModel


class KayitOlFragment : Fragment() {

    private var _binding: FragmentKayitOlBinding? = null
    private val binding get() = _binding!!

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKayitOlBinding.inflate(inflater, container, false)
        registerViewModel = ViewModelProvider(requireActivity())[RegisterViewModel::class.java]

        binding.btnKayitOl.setOnClickListener {
            val username = binding.etKullaniciAdi.text.toString().trim()
            val password = binding.etSifre.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                registerViewModel.registerUser(username, password)
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvGirisYap.setOnClickListener {
            findNavController().navigate(R.id.girisYapFragment)
        }

        registerViewModel.registerResult.observe(viewLifecycleOwner) { response ->
            if (response.success) {
                Toast.makeText(requireContext(), "Kayıt başarılı! Giriş yapabilirsiniz", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_kayitOlFragment_to_girisYapFragment)
            } else {
                println(response.message)
                Toast.makeText(requireContext(), "Hata: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}