package com.example.eticaretkotlin.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.eticaretkotlin.MainActivity
import com.example.eticaretkotlin.R
import com.example.eticaretkotlin.databinding.FragmentLoginBinding
import com.example.eticaretkotlin.repo.AuthVMFactory
import com.example.eticaretkotlin.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels { AuthVMFactory(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val pass = binding.etPassword.text?.toString().orEmpty()

            var ok = true
            if (!email.contains("@")) {
                binding.tilEmail.error = "Geçerli e-posta girin"
                ok = false
            } else binding.tilEmail.error = null

            if (pass.length < 6) {
                binding.tilPassword.error = "En az 6 karakter"
                ok = false
            } else binding.tilPassword.error = null

            if (!ok) return@setOnClickListener

            vm.login(email, pass)
        }

        binding.tvGoRegister.setOnClickListener {
            findNavController().navigate(R.id.toRegister)
        }

        collectState()
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    binding.progress.isVisible = st.inFlight

                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        vm.clearError()
                    }

                    if (st.loggedIn) {
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }


                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
