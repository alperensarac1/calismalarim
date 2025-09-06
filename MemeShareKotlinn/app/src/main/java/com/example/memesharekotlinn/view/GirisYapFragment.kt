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
import com.example.memesharekotlinn.databinding.FragmentGirisYapBinding
import com.example.memesharekotlinn.viewmodel.LoginViewModel


class GirisYapFragment : Fragment() {

    private var _binding: FragmentGirisYapBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGirisYapBinding.inflate(inflater, container, false)
        loginViewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        binding.btnGirisYap.setOnClickListener {
            val username = binding.etKullaniciAdi.text.toString().trim()
            val password = binding.etSifre.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.loginUser(username, password)
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvKayitOl.setOnClickListener {
            findNavController().navigate(R.id.kayitOlFragment)
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner) { response ->
            if (response.success) {
                Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()

                // userId'yi al ve anasayfaya gönder
                val bundle = Bundle().apply { putInt("userId", response.userId) }
                findNavController().navigate(
                    R.id.action_girisYapFragment_to_anasayfaFragment,
                    bundle
                )
            } else {
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