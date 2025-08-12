package com.example.sozlukkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sozlukkotlin.R
import com.example.sozlukkotlin.databinding.FragmentKayitBinding
import com.example.sozlukkotlin.viewmodel.KayitViewModel

class KayitFragment : Fragment() {
    private var _binding: FragmentKayitBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: KayitViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKayitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[KayitViewModel::class.java]

        binding.btnKayitOl.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            viewModel.register(username, password, email)
        }

        viewModel.registerResult.observe(viewLifecycleOwner) {
            if (it.success) {
                Toast.makeText(requireContext(), "Kayıt başarılı. Giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_kayitFragment_to_girisFragment)
            } else {
                Toast.makeText(requireContext(), it.message ?: "Kayıt başarısız", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGirisSayfasinaGit.setOnClickListener {
            findNavController().navigate(R.id.action_kayitFragment_to_girisFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
