package com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentRehberEkleBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi
import com.google.android.material.snackbar.Snackbar


class RehberEkleFragment : Fragment() {

    lateinit var binding : FragmentRehberEkleBinding
    lateinit var vt : RehberVeritabaniYardimcisi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRehberEkleBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        vt = RehberVeritabaniYardimcisi(requireContext())
        var dao = RehberDao(vt)

        binding.btnEkle.setOnClickListener {
            var kisi_isim = binding.etAd.text.toString()
            var kisi_numara = binding.etKisiNumara.text.toString()
            dao.kisiEkle(kisi_isim,kisi_numara)
            Snackbar.make(it,"Kişi eklendi",Snackbar.LENGTH_SHORT).show()
            Navigation.findNavController(it).popBackStack()
        }

        return binding.root
    }


}
