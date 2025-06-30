package com.example.isletimsistemicalismasi.giris_ekrani;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentUygulamaAcilisEkraniBinding;
import com.example.isletimsistemicalismasi.giris_ekrani.repository.KullaniciBilgileri;


public class UygulamaAcilisEkraniFragment extends Fragment {

    FragmentUygulamaAcilisEkraniBinding binding;
    KullaniciBilgileri kullaniciBilgileri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUygulamaAcilisEkraniBinding.inflate(inflater,container,false);
        kullaniciBilgileri = new KullaniciBilgileri(requireContext());

        CountDownTimer ct = new CountDownTimer(2000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (kullaniciBilgileri.sifreKaydiOlusturulmusMu()){
                    Navigation.findNavController(binding.imageView2).navigate(R.id.girisYapFragment);
                }else{
                    Navigation.findNavController(binding.imageView2).navigate(R.id.kayitOlusturFragment);
                }
            }
        };
        ct.start();



        return binding.getRoot();
    }

}