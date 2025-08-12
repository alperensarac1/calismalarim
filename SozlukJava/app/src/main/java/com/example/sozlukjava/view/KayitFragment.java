package com.example.sozlukjava.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sozlukjava.R;
import com.example.sozlukjava.databinding.FragmentKayitBinding;
import com.example.sozlukjava.model.SimpleResponse;
import com.example.sozlukjava.viewmodel.KayitViewModel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class KayitFragment extends Fragment {

    private FragmentKayitBinding binding;
    private KayitViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentKayitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(KayitViewModel.class);

        binding.btnKayitOl.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();
            viewModel.register(username, password, email);
        });

        viewModel.getRegisterResult().observe(getViewLifecycleOwner(), new Observer<SimpleResponse>() {
            @Override
            public void onChanged(SimpleResponse it) {
                if (it == null) return;

                if (it.isSuccess()) {
                    Toast.makeText(requireContext(),
                            "Kayıt başarılı. Giriş yapabilirsiniz.", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(KayitFragment.this)
                            .navigate(R.id.action_kayitFragment_to_girisFragment);
                } else {
                    String msg = (it.getMessage() != null) ? it.getMessage() : "Kayıt başarısız";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnGirisSayfasinaGit.setOnClickListener(v ->
                NavHostFragment.findNavController(KayitFragment.this)
                        .navigate(R.id.action_kayitFragment_to_girisFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}