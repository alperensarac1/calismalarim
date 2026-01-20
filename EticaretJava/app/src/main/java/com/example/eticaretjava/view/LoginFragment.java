package com.example.eticaretjava.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eticaretjava.R;
import com.example.eticaretjava.databinding.FragmentLoginBinding;
import com.example.eticaretjava.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot(); // ✅ mutlaka root döndür
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null
                    ? binding.etEmail.getText().toString().trim() : "";
            String pass = binding.etPassword.getText() != null
                    ? binding.etPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email)) {
                binding.tilEmail.setError("E-posta boş olamaz");
                return;
            } else {
                binding.tilEmail.setError(null);
            }

            if (TextUtils.isEmpty(pass)) {
                binding.tilPassword.setError("Şifre boş olamaz");
                return;
            } else {
                binding.tilPassword.setError(null);
            }

            viewModel.login(email, pass);
        });

        // Register'a git
        binding.tvGoRegister.setOnClickListener(v -> {
            // NavGraph’ta action id’n farklıysa değiştir
            NavHostFragment.findNavController(this)
                    .navigate(R.id.toRegister);
        });

        observeState();
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {

            // ✅ loading -> progress
            binding.progress.setVisibility(state.loggedIn ? View.VISIBLE : View.GONE);

            // ✅ hata
            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();

                // AuthViewModel'de clearError varsa aç:
                // viewModel.clearError();
            }

            // ✅ başarılı giriş
            if (state.loggedIn) {
                Toast.makeText(getContext(), "Giriş başarılı", Toast.LENGTH_SHORT).show();

                // İstersen ana sayfaya yönlendir:
                // NavHostFragment.findNavController(this)
                //         .navigate(R.id.action_loginFragment_to_mainFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // ✅ leak önlemi
    }
}
