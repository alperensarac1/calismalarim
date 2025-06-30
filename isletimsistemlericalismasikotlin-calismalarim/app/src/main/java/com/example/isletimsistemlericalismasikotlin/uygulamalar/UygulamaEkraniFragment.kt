package com.example.isletimsistemlericalismasikotlin.uygulamalar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.isletimsistemlericalismasikotlin.anaekran.adapter.AnaekranRVAdapter
import com.example.isletimsistemlericalismasikotlin.anaekran.entity.Const
import com.example.isletimsistemlericalismasikotlin.databinding.FragmentUygulamaEkraniBinding

class UygulamaEkraniFragment : Fragment() {

    private lateinit var binding: FragmentUygulamaEkraniBinding
    private lateinit var uygulamalar: Const
    private lateinit var layoutManager: GridLayoutManager
    private lateinit var adapter: AnaekranRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUygulamaEkraniBinding.inflate(inflater, container, false)

        adapter = AnaekranRVAdapter(requireContext(), uygulamalar)
        layoutManager = GridLayoutManager(requireContext(), 3)

        binding.uygulamalarRV.setHasFixedSize(true)
        binding.uygulamalarRV.layoutManager = layoutManager
        binding.uygulamalarRV.adapter = adapter

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uygulamalar = Const(requireActivity())
    }
}