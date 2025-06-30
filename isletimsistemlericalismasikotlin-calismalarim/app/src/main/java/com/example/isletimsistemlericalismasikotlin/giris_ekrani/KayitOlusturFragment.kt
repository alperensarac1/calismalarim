package com.example.isletimsistemlericalismasikotlin.giris_ekrani

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentKayitOlusturBinding
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri


class KayitOlusturFragment : Fragment() {

    var sifreGosterilsinMi = false
    lateinit var binding : FragmentKayitOlusturBinding
    lateinit var kullaniciBilgileri: KullaniciBilgileri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentKayitOlusturBinding.inflate(inflater,container,false)
        kullaniciBilgileri = KullaniciBilgileri(requireContext())

        binding.btnSifreKayit.setOnClickListener {
            var sifre = binding.etSifreKayit.text.toString()
            var sifreTekrar = binding.etSifreKayitTekrar.text.toString()

            if (!sifre.trim().equals("") && !sifreTekrar.trim().equals("")){
                if (sifreKontrol(sifre,sifreTekrar)){
                    kullaniciBilgileri.sifreKaydi(sifre)
                    Navigation.findNavController(it).navigate(R.id.toGuvenlikSorusuSecFragment);
                }else{
                    Toast.makeText(requireContext(),"Şifreleriniz uyumlu değil !",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(requireContext(),"Lütfen şifre alanlarını doldurunuz !",Toast.LENGTH_LONG).show()
            }
        }

        binding.imgSifreGoster.setOnClickListener {
            sifreGosterilsinMi = !sifreGosterilsinMi;
            if (sifreGosterilsinMi) {
                binding.etSifreKayit.setTransformationMethod(null);
                binding.etSifreKayitTekrar.setTransformationMethod(null);
            } else {
                binding.etSifreKayit.setTransformationMethod(PasswordTransformationMethod());
                binding.etSifreKayitTekrar.setTransformationMethod(PasswordTransformationMethod());
            }
            binding.etSifreKayit.setSelection(binding.etSifreKayit.getText().length);
            binding.etSifreKayitTekrar.setSelection(binding.etSifreKayitTekrar.getText().length);
        }
        binding.imgSifreGosterTekrar.setOnClickListener {
            sifreGosterilsinMi = !sifreGosterilsinMi;
            if (sifreGosterilsinMi) {
                binding.etSifreKayit.setTransformationMethod(null);
                binding.etSifreKayitTekrar.setTransformationMethod(null);
            } else {
                binding.etSifreKayit.setTransformationMethod(PasswordTransformationMethod());
                binding.etSifreKayitTekrar.setTransformationMethod(PasswordTransformationMethod());
            }
            binding.etSifreKayit.setSelection(binding.etSifreKayit.getText().length);
            binding.etSifreKayitTekrar.setSelection(binding.etSifreKayitTekrar.getText().length);
        }

        return binding.root
    }
    private fun sifreKontrol(sifre:String,sifreTekrar:String):Boolean{
        return sifre.trim().equals(sifreTekrar.trim());
    }
}
