package com.example.isletimsistemlericalismasikotlin.giris_ekrani

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentSifremiUnuttumBinding
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri


class SifremiUnuttumFragment : Fragment() {

    lateinit var binding :FragmentSifremiUnuttumBinding
    lateinit var kullaniciBilgileri: KullaniciBilgileri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSifremiUnuttumBinding.inflate(inflater,container,false)
        kullaniciBilgileri = KullaniciBilgileri(requireContext())
        binding.tvGuvenlikSorusu.setText(kullaniciBilgileri.guvenlikSorusuGetir())
        binding.btnOnayla.setOnClickListener {
            guvenlikSorusuDogrula(binding.etGuvenlikSorusuCevap.getText().toString(),binding.btnOnayla)
        }
        return binding.root
    }
    fun guvenlikSorusuDogrula(cevap:String,view:View){
        var sonuc = kullaniciBilgileri.guvenlikSorusuDogrula(cevap)
        if (sonuc){
           Navigation.findNavController(view).navigate(R.id.kayitOlusturFragment)
        }else{
           Toast.makeText(requireContext(),"Yanlış Cevap Girdiniz",Toast.LENGTH_SHORT).show()
        }
    }
}
