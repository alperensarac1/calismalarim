package com.example.qrkodolusturucu.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qrkodolusturucu.adapter.UrunAdapter
import com.example.qrkodolusturucu.databinding.FragmentUrunlerimizBinding
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucu.services.ServicesImpl
import com.example.qrkodolusturucu.model.UrunKategori
import com.example.qrkodolusturucu.viewmodel.QRKodOlusturVM
import com.example.qrkodolusturucu.viewmodel.UrunlerimizVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class UrunlerimizFragment : Fragment() {

    private val urunlerimizVM: UrunlerimizVM by viewModels()
    lateinit var binding:FragmentUrunlerimizBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUrunlerimizBinding.inflate(inflater,container,false)



        urunlerimizVM.recyclerviewAyarla(UrunKategori.ICECEKLER,binding.rvIcecekler,requireActivity())
        urunlerimizVM.recyclerviewAyarla(UrunKategori.ATISTIRMALIKLAR,binding.rvAtistirmaliklar,requireActivity())
        urunlerimizVM.indirimliRecyclerAyarla(binding.rvKampanyalar,requireActivity())




        return binding.root
    }



}