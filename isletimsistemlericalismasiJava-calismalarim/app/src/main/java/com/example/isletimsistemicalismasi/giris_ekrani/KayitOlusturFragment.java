package com.example.isletimsistemicalismasi.giris_ekrani;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.isletimsistemicalismasi.R;
import com.example.isletimsistemicalismasi.databinding.FragmentKayitOlusturBinding;
import com.example.isletimsistemicalismasi.giris_ekrani.repository.KullaniciBilgileri;


public class KayitOlusturFragment extends Fragment {
    boolean sifreGosterilsinMi = false;
    FragmentKayitOlusturBinding binding;
    KullaniciBilgileri kullaniciBilgileri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentKayitOlusturBinding.inflate(inflater,container,false);
        kullaniciBilgileri = new KullaniciBilgileri(requireContext());

        binding.btnSifreKayit.setOnClickListener(view->{

            String sifre = binding.etSifreKayit.getText().toString();
            String sifreTekrar = binding.etSifreKayitTekrar.getText().toString();

            if (!sifre.trim().equals("") && !sifreTekrar.trim().equals("")){
                if (sifreKontrol(sifre,sifreTekrar)){
                    kullaniciBilgileri.sifreKaydi(sifre);
                    Navigation.findNavController(view).navigate(R.id.toGuvenlikSorusuSecFragment);
                }else{
                    Toast.makeText(requireContext(),"Şifreleriniz uyumlu değil !",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(requireContext(),"Lütfen şifre alanlarını doldurunuz !",Toast.LENGTH_LONG).show();
            }

        });
        binding.imgSifreGosterTekrar.setOnClickListener(view->{
            sifreGosterilsinMi = !sifreGosterilsinMi;
            if (sifreGosterilsinMi) {
                binding.etSifreKayit.setTransformationMethod(null);
                binding.etSifreKayitTekrar.setTransformationMethod(null);
            } else {
                binding.etSifreKayit.setTransformationMethod(new PasswordTransformationMethod());
                binding.etSifreKayitTekrar.setTransformationMethod(new PasswordTransformationMethod());
            }
            binding.etSifreKayit.setSelection(binding.etSifreKayit.getText().length());
            binding.etSifreKayitTekrar.setSelection(binding.etSifreKayitTekrar.getText().length());
        });
        binding.imgSifreGoster.setOnClickListener(view->{
            sifreGosterilsinMi = !sifreGosterilsinMi;
            if (sifreGosterilsinMi) {
                binding.etSifreKayit.setTransformationMethod(null);
                binding.etSifreKayitTekrar.setTransformationMethod(null);
            } else {
                binding.etSifreKayit.setTransformationMethod(new PasswordTransformationMethod());
                binding.etSifreKayitTekrar.setTransformationMethod(new PasswordTransformationMethod());
            }
            binding.etSifreKayit.setSelection(binding.etSifreKayit.getText().length());
            binding.etSifreKayitTekrar.setSelection(binding.etSifreKayitTekrar.getText().length());
        });

        return binding.getRoot();
    }

    private boolean sifreKontrol(String sifre,String sifreTekrar){
        return sifre.trim().equals(sifreTekrar.trim());
    }

}