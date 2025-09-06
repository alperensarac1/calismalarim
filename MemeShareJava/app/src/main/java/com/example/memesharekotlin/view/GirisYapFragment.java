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
import com.example.memesharekotlin.databinding.FragmentGirisYapBinding;
import com.example.memesharekotlin.viewmodel.LoginViewModel;


public class GirisYapFragment extends Fragment {

    FragmentGirisYapBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGirisYapBinding.inflate(inflater, container, false);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        binding.btnGirisYap.setOnClickListener(v -> {
            String username = binding.etKullaniciAdi.getText().toString().trim();
            String password = binding.etSifre.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                loginViewModel.loginUser(username, password);
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurun", Toast.LENGTH_SHORT).show();
            }
        });
        binding.tvKayitOl.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.kayitOlFragment);
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), response -> {
            if (response.success) {
                Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show();

                // userId'yi al ve anasayfa fragmentine gönder
                Bundle bundle = new Bundle();
                bundle.putInt("userId", response.userId);

                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_girisYapFragment_to_anasayfaFragment, bundle);

            } else {
                Toast.makeText(requireContext(), "Hata: " + response.message, Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }
}
