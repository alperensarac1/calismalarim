package com.example.eticaretkotlin.view

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
import com.example.eticaretkotlin.R
import com.example.eticaretkotlin.databinding.FragmentRegisterBinding
import com.example.eticaretkotlin.repo.AuthVMFactory
import com.example.eticaretkotlin.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ui/auth/RegisterFragment.kt
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels {
        AuthVMFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        binding.btnRegister.setOnClickListener {
            val name  = binding.etName.text?.toString().orEmpty()
            val email = binding.etEmail.text?.toString().orEmpty()
            val pass  = binding.etPassword.text?.toString().orEmpty()

            var ok = true
            if (name.length < 2) { binding.tilName.error = "Ad en az 2 karakter"; ok = false } else binding.tilName.error = null
            if (!email.contains("@")) { binding.tilEmail.error = "Geçerli e-posta"; ok = false } else binding.tilEmail.error = null
            if (pass.length < 6) { binding.tilPassword.error = "En az 6 karakter"; ok = false } else binding.tilPassword.error = null
            if (!ok) return@setOnClickListener

            vm.register(name, email, pass)
        }

        // StateFlow'u lifecycle güvenli şekilde topla
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { st ->
                    binding.progress.isVisible = st.inFlight

                    st.error?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                        vm.clearError()
                    }

                    if (st.registered) {
                        vm.clearRegistered()
                        findNavController().navigate(R.id.toLogin)
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
