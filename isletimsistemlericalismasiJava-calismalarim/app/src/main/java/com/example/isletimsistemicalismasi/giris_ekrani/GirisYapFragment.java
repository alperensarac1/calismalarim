package com.example.isletimsistemicalismasi.giris_ekrani;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.anaekran.UygulamaEkraniActivity;
import com.example.isletimsistemicalismasi.databinding.FragmentGirisYapBinding;
import com.example.isletimsistemicalismasi.giris_ekrani.repository.KullaniciBilgileri;

import java.lang.reflect.Type;


public class GirisYapFragment extends Fragment {

    FragmentGirisYapBinding binding;
    KullaniciBilgileri kullaniciBilgileri;
    boolean sifreGosterilsinMi = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGirisYapBinding.inflate(inflater,container,false);
        kullaniciBilgileri = new KullaniciBilgileri(requireContext());
        binding.btnSifreGiris.setOnClickListener(view->{
            String girilenSifre = binding.etSifreGiris.getText().toString();
            boolean sonuc = kullaniciBilgileri.sifreSorgulama(girilenSifre);
            if (sonuc){
                Toast.makeText(requireContext(),"Şifre Doğru",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(requireActivity(), UygulamaEkraniActivity.class);
                startActivity(intent);
                requireActivity().finish();

            }else{
                Toast.makeText(requireContext(),"Şifreniz yanlış",Toast.LENGTH_LONG).show();
            }
        });

        binding.imgSifreGoster.setOnClickListener(view->{
            sifreGosterilsinMi = !sifreGosterilsinMi;
            if (sifreGosterilsinMi) {
                binding.etSifreGiris.setTransformationMethod(null);
            } else {
                binding.etSifreGiris.setTransformationMethod(new PasswordTransformationMethod());
            }
            binding.etSifreGiris.setSelection(binding.etSifreGiris.getText().length());
        });


        binding.btnSifremiUnuttum.setOnClickListener(view->{
            Navigation.findNavController(view).navigate(R.id.sifremiUnuttumFragment2);
        });

        return binding.getRoot();
    }
}