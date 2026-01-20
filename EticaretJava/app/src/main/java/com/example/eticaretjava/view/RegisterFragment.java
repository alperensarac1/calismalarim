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
import com.example.eticaretjava.databinding.FragmentRegisterBinding;
import com.example.eticaretjava.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText() != null
                    ? binding.etName.getText().toString().trim() : "";
            String email = binding.etEmail.getText() != null
                    ? binding.etEmail.getText().toString().trim() : "";
            String pass = binding.etPassword.getText() != null
                    ? binding.etPassword.getText().toString() : "";

            // Basit validasyon
            if (TextUtils.isEmpty(name)) {
                if (binding.tilName != null) binding.tilName.setError("Ad soyad boş olamaz");
                return;
            } else if (binding.tilName != null) {
                binding.tilName.setError(null);
            }

            if (TextUtils.isEmpty(email)) {
                if (binding.tilEmail != null) binding.tilEmail.setError("E-posta boş olamaz");
                return;
            } else if (binding.tilEmail != null) {
                binding.tilEmail.setError(null);
            }

            if (TextUtils.isEmpty(pass)) {
                if (binding.tilPassword != null) binding.tilPassword.setError("Şifre boş olamaz");
                return;
            } else if (binding.tilPassword != null) {
                binding.tilPassword.setError(null);
            }

            viewModel.register(name, email, pass);
        });


        observeState();
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {

            // ProgressBar id'n "progress" ise
            if (binding.progress != null) {
                binding.progress.setVisibility(state.inFlight ? View.VISIBLE : View.GONE);
            }

            if (state.error != null) {
                Toast.makeText(getContext(), state.error, Toast.LENGTH_SHORT).show();
                // varsa: viewModel.clearError();
            }

            if (state.loggedIn) {
                Toast.makeText(getContext(), "Kayıt başarılı", Toast.LENGTH_SHORT).show();

                // İstersen main'e geç
                // NavHostFragment.findNavController(this)
                //        .navigate(R.id.action_registerFragment_to_mainFragment);

                // Ya da login ekranına geri dön
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
