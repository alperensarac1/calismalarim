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
import com.example.sozlukkotlin.databinding.FragmentGirisBinding
import com.example.sozlukkotlin.util.SessionManager
import com.example.sozlukkotlin.viewmodel.GirisViewModel


class GirisFragment : Fragment() {
    private var _binding: FragmentGirisBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GirisViewModel
    private lateinit var session: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGirisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        session = SessionManager(requireContext())

        // Oturum açık mı?
        if (session.isLoggedIn()) {
            findNavController().navigate(R.id.action_girisFragment_to_anaSayfaFragment)
            return
        }

        viewModel = ViewModelProvider(this)[GirisViewModel::class.java]

        binding.btnGiris.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            viewModel.login(username, password)
        }

        viewModel.loginResult.observe(viewLifecycleOwner) {
            if (it.success) {
                session.saveUserSession(it.user_id ?: -1, binding.etUsername.text.toString())
                findNavController().navigate(R.id.action_girisFragment_to_anaSayfaFragment)
            } else {
                Toast.makeText(requireContext(), it.message ?: "Giriş başarısız", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnKayitSayfasinaGit.setOnClickListener {
            findNavController().navigate(R.id.action_girisFragment_to_kayitFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
