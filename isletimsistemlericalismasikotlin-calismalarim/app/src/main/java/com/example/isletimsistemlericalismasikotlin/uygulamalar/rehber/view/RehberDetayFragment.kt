package com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.view

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentRehberDetayBinding
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi
import com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.model.RehberKisiler


class RehberDetayFragment : Fragment() {

    lateinit var binding : FragmentRehberDetayBinding
    lateinit var vt : RehberVeritabaniYardimcisi
    lateinit var gelenKisi:RehberKisiler

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentRehberDetayBinding.inflate(inflater,container,false)
        gelenKisi  = arguments?.getSerializable("kisi",RehberKisiler::class.java)!!
        vt = RehberVeritabaniYardimcisi(requireContext())
        var dao = RehberDao(vt)
        binding.etAd.setText(gelenKisi.kisi_isim)
        binding.etKisiNumara.setText(gelenKisi.kisi_numara)

        binding.btnKaydet.setOnClickListener {
            var kisi_id = gelenKisi.kisi_id
            var kisi_isim = binding.etAd.text.toString()
            var kisi_numara = binding.etKisiNumara.text.toString()
            dao.kisiGuncelle(kisi_id,kisi_isim,kisi_numara)
        }

        return binding.root
    }


}
