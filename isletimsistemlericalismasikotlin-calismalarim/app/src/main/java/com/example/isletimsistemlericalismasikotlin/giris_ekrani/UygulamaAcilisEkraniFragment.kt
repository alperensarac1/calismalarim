package com.example.isletimsistemlericalismasikotlin.giris_ekrani

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentUygulamaAcilisEkraniBinding
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri

class UygulamaAcilisEkraniFragment : Fragment() {

    lateinit var binding: FragmentUygulamaAcilisEkraniBinding
    lateinit var kullaniciBilgileri: KullaniciBilgileri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUygulamaAcilisEkraniBinding.inflate(inflater,container,false)
        kullaniciBilgileri = KullaniciBilgileri(requireContext())

        var ct = object : CountDownTimer(2000,1000){
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if (kullaniciBilgileri.sifreKaydiOlusturulmusMu()){
                    Navigation.findNavController(binding.imageView2).navigate(R.id.girisYapFragment);
                }else{
                    Navigation.findNavController(binding.imageView2).navigate(R.id.kayitOlusturFragment);
                }
            }

        }
        ct.start()
        return binding.root
    }

}
