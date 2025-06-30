package com.example.isletimsistemlericalismasikotlin.giris_ekrani

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.anaekran.UygulamaEkraniActivity
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentGirisYapBinding
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri


class GirisYapFragment : Fragment() {

    lateinit var  binding:FragmentGirisYapBinding
    lateinit var kullaniciBilgileri: KullaniciBilgileri
    var sifreGosterilsinMi = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentGirisYapBinding.inflate(inflater,container,false)
        kullaniciBilgileri = KullaniciBilgileri(requireContext())
        binding.btnSifreGiris.setOnClickListener {
            var girilenSifre = binding.etSifreGiris.text.toString()
            var sonuc = kullaniciBilgileri.sifreSorgulama(girilenSifre)
            if (sonuc){
                Toast.makeText(requireContext(),"Şifre Doğru",Toast.LENGTH_SHORT).show()
                //TODO UYGULAMA EKRANI GEÇİŞİ

               Toast.makeText(requireContext(),"Şifre Doğru",Toast.LENGTH_LONG).show();
                var intent = Intent(requireActivity(), UygulamaEkraniActivity::class.java)
                startActivity(intent);
                requireActivity().finish();

            }else{
                Toast.makeText(requireContext(),"Şifreniz Yanlış",Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSifremiUnuttum.setOnClickListener {

            //TODO fragment geçişi Navigation.findNavController(it).navigate(R.id.)
        }
        binding.imgSifreGoster.setOnClickListener {
            sifreGosterilsinMi = !sifreGosterilsinMi;
            if (sifreGosterilsinMi) {
                binding.etSifreGiris.setTransformationMethod(null);
            } else {
                binding.etSifreGiris.setTransformationMethod(PasswordTransformationMethod());
            }
            binding.etSifreGiris.setSelection(binding.etSifreGiris.getText().length);
        }
        return binding.root
    }

}