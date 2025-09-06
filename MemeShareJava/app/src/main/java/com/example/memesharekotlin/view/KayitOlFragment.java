package com.example.memesharekotlin.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.memesharekotlin.R;
import com.example.memesharekotlin.databinding.FragmentKayitOlBinding;
import com.example.memesharekotlin.viewmodel.RegisterViewModel;


public class KayitOlFragment extends Fragment {

    FragmentKayitOlBinding binding;
    private RegisterViewModel registerViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKayitOlBinding.inflate(inflater, container, false);
        registerViewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);

        binding.btnKayitOl.setOnClickListener(v -> {
            String username = binding.etKullaniciAdi.getText().toString().trim();
            String password = binding.etSifre.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                registerViewModel.registerUser(username, password);
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            }
        });
        binding.tvGirisYap.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.girisYapFragment);
        });

        registerViewModel.getRegisterResult().observe(getViewLifecycleOwner(), response -> {
            if (response.success) {
                Toast.makeText(requireContext(), "Kayıt başarılı! Giriş yapabilirsiniz", Toast.LENGTH_SHORT).show();
                // Kayıttan sonra login ekranına yönlendir
                NavHostFragment.findNavController(this).navigate(R.id.action_kayitOlFragment_to_girisYapFragment);
            } else {

                System.out.println(response.message);
                Toast.makeText(requireContext(), "Hata: " + response.message, Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }
}
